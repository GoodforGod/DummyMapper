package io.goodforgod.dummymapper.service;

import static io.goodforgod.dummymapper.util.PsiClassUtils.*;

import com.intellij.lang.jvm.JvmClassKind;
import com.intellij.lang.jvm.annotation.JvmAnnotationArrayValue;
import com.intellij.lang.jvm.annotation.JvmAnnotationAttributeValue;
import com.intellij.lang.jvm.annotation.JvmAnnotationConstantValue;
import com.intellij.lang.jvm.types.JvmType;
import com.intellij.navigation.NavigationItem;
import com.intellij.openapi.util.Pair;
import com.intellij.psi.*;
import com.intellij.psi.impl.source.PsiClassReferenceType;
import com.intellij.psi.search.GlobalSearchScope;
import io.dummymaker.util.StringUtils;
import io.goodforgod.dummymapper.error.PsiKindException;
import io.goodforgod.dummymapper.error.ScanException;
import io.goodforgod.dummymapper.marker.*;
import io.goodforgod.dummymapper.marker.AnnotationMarker;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.kotlin.idea.KotlinLanguage;

/**
 * Scan PsiJavaFile structure of fields and their annotations (also getters and setters annotations
 * for such fields)
 * <p>
 * https://intellij-support.jetbrains.com/hc/en-us/community/posts/360002746839-How-to-add-an-annotation-and-import-to-a-Java-class
 * Scans class {@link PsiFile} and build tree structure of such class file
 *
 * @author GoodforGod
 * @since 27.11.2019
 */
@SuppressWarnings("UnstableApiUsage")
public class PsiClassScanner {

    private static class Target {

        private final String root;
        private final String source;

        public Target(String root, String source) {
            this.root = root;
            this.source = source;
        }

        public String getRoot() {
            return root;
        }

        public String getSource() {
            return source;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;
            Target target = (Target) o;
            return Objects.equals(root, target.root) && Objects.equals(source, target.source);
        }

        @Override
        public int hashCode() {
            return Objects.hash(root, source);
        }
    }

    private final Map<Target, Map<String, Marker>> scanned = new HashMap<>();

    public @NotNull RawMarker scan(@Nullable PsiClass target) {
        if (target == null)
            return RawMarker.EMPTY;

        if (JvmClassKind.ENUM.equals(target.getClassKind())
                || JvmClassKind.ANNOTATION.equals(target.getClassKind())
                || JvmClassKind.INTERFACE.equals(target.getClassKind()))
            throw new PsiKindException(target.getClassKind());

        final Map<String, Marker> scannedFile = scanPsiClass(target);
        if (scannedFile.isEmpty())
            return RawMarker.EMPTY;

        final String source = getFileFullName(target);
        final String root = getFileFullName(target);
        return new RawMarker(root, source, scannedFile);
    }

    private @NotNull Map<String, Marker> scanPsiClass(@Nullable PsiClass target) {
        try {
            if (target == null || isTypeSimple(getFileFullName(target)) || isTypeEnum(getFileFullName(target)))
                return Collections.emptyMap();

            return scanPsiClass(target, target, Collections.emptyMap());
        } catch (Exception e) {
            throw new ScanException(e);
        }
    }

    private Map<String, Marker> scanPsiClass(@NotNull PsiClass rootClass,
                                             @NotNull PsiClass targetClass,
                                             @NotNull Map<String, PsiType> parentTypes) {
        if (isTypeSimple(getFileFullName(targetClass)) || isTypeEnum(getFileFullName(targetClass)))
            return Collections.emptyMap();

        final String root = getFileFullName(rootClass);
        final String source = getFileFullName(targetClass);

        final PsiClass superTarget = targetClass.getSuperClass();
        final Map<String, Marker> structure = new LinkedHashMap<>();

        if (superTarget != null && Enum.class.getName().equals(superTarget.getQualifiedName())) {
            final List<String> enumValues = Arrays.stream(targetClass.getFields())
                    .filter(f -> f instanceof PsiEnumConstant)
                    .map(PsiField::getName)
                    .collect(Collectors.toList());

            final EnumMarker marker = new EnumMarker(root, source, enumValues);
            structure.put(targetClass.getName(), marker);
            scanned.put(new Target(root, source), structure);
            return structure;
        }

        if (superTarget != null && !isTypeSimple(superTarget.getQualifiedName())) { // SCAN PARENT CLASS
            final Map<String, PsiType> types = getTypeErasures(targetClass);
            final Map<String, PsiType> unknownParentTypes = types.entrySet().stream()
                    .filter(e -> !isTypeSimple(e.getValue().getPresentableText()))
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

            int valueCounter = 0;
            for (Map.Entry<String, PsiType> entry : unknownParentTypes.entrySet()) {
                final String key = entry.getKey();
                final PsiType v = entry.getValue();
                final int index = valueCounter;

                final PsiType type = Optional.ofNullable(parentTypes.get(v.getPresentableText()))
                        .orElseGet(() -> (v.getSuperTypes().length > index)
                                ? v.getSuperTypes()[index]
                                : null);
                if (type != null)
                    types.put(key, type);

                valueCounter++;
            }

            final Map<String, Marker> superScan = scanPsiClass(rootClass, superTarget, types);
            structure.putAll(superScan);
        }

        scanned.put(new Target(root, source), structure);
        final PsiField[] fields = targetClass.getFields();

        for (PsiField field : fields) {
            final String fieldName = field.getName();
            final PsiType type = field.getType();
            final String typeName = type.getPresentableText();

            if (isTypeEnum(type)) {
                final EnumMarker marker = scanEnumMarker(source, root, rootClass, type);
                structure.put(fieldName, marker);
            } else if (isFieldValid(field)) {
                if (isTypeArray(type)) {
                    final ArrayMarker marker = scanArrayMarker(source, root, targetClass, type);
                    structure.put(fieldName, marker);
                } else if (isTypeCollection(type)) {
                    final CollectionMarker marker = scanCollectionMarker(source, root, targetClass, type);
                    structure.put(fieldName, marker);
                } else if (isTypeMap(type)) {
                    final MapMarker marker = scanMapMarker(source, root, targetClass, type);
                    structure.put(fieldName, marker);
                } else if (isTypeSimple(type)) {
                    final TypedMarker marker = scanSimpleMarker(source, root, type);
                    structure.put(fieldName, marker);
                } else if (parentTypes.containsKey(typeName)) { // PARENT CLASS ERASURE
                    final PsiType parentType = parentTypes.get(typeName);
                    if (parentType != null && isTypeSimple(parentType)) {
                        final TypedMarker marker = scanSimpleMarker(source, root, parentType);
                        structure.put(fieldName, marker);
                    }
                } else if (!isTypeForbidden(type)) { // COMPLEX CLASS SCAN IF NOT FORBIDDEN ONE
                    final Optional<Marker> marker = scanJavaFileClass(targetClass, type);
                    if (marker.isPresent()) {
                        structure.put(fieldName, marker.get());
                    } else {
                        scanJavaInnerClass(targetClass, type).ifPresent(m -> structure.put(fieldName, m));
                    }
                }
            }

            structure.computeIfPresent(fieldName, (k, v) -> {
                final Collection<AnnotationMarker> annotations = scanMarkerAnnotations(targetClass, field);
                return v.setAnnotations(annotations);
            });
        }

        return structure;
    }

    private Collection<AnnotationMarker> scanMarkerAnnotations(@NotNull PsiClass targetClass,
                                                               @NotNull PsiField field) {
        final String fieldName = field.getName();
        final List<AnnotationMarker> annotations = scanAnnotations(field.getAnnotations())
                .map(f -> f.ofField().build())
                .collect(Collectors.toList());

        final List<AnnotationMarker> methodAnnotations = Arrays.stream(targetClass.getAllMethods())
                .filter(m -> ArrayUtils.isNotEmpty(m.getAnnotations()))
                .filter(m -> m.getName().length() > 3)
                .filter(m -> fieldName.equalsIgnoreCase(m.getName().substring(3)))
                .flatMap(m -> scanAnnotations(m.getAnnotations())
                        .map(b -> m.getName().startsWith("set")
                                ? b.ofSetter()
                                : b.ofGetter())
                        .map(AnnotationMarker.Builder::build))
                .collect(Collectors.toList());

        return Stream.of(annotations, methodAnnotations)
                .flatMap(List::stream)
                .collect(Collectors.toSet());
    }

    private Optional<Marker> scanJavaInnerClass(@NotNull PsiClass rootClass,
                                                @NotNull PsiType type) {
        final String root = getFileFullName(rootClass);
        return getPsiJavaParentClass(type)
                .flatMap(psiClass -> Arrays.stream(psiClass.getAllInnerClasses())
                        .filter(c -> type.getCanonicalText().equals(c.getQualifiedName()))
                        .findFirst()
                        .map(sourceClass -> {
                            final String source = getFileFullName(sourceClass);
                            final Map<String, Marker> cached = scanned.get(new Target(root, source));
                            final Map<String, Marker> structure = (cached == null)
                                    ? scanPsiClass(sourceClass, sourceClass, new HashMap<>())
                                    : cached;

                            final Marker marker = structure.get(type.getPresentableText());
                            if (marker instanceof EnumMarker) { // ENUM
                                return new EnumMarker(root, marker.getSource(), ((EnumMarker) marker).getValues());
                            } else {
                                return new RawMarker(root, source, structure);
                            }
                        }));
    }

    private Optional<Marker> scanJavaFileClass(@NotNull PsiClass rootClass,
                                               @NotNull PsiType type) {
        return getPsiJavaClass(type).map(psiClass -> {
            final String root = getFileFullName(rootClass);
            final String source = getFileFullName(psiClass);
            final Map<String, Marker> cached = scanned.get(new Target(source, source));
            final Map<String, Marker> structure = (cached == null)
                    ? scanPsiClass(psiClass)
                    : cached;

            final Marker marker = structure.get(type.getPresentableText());
            if (marker instanceof EnumMarker) { // ENUM
                return new EnumMarker(root, marker.getSource(), ((EnumMarker) marker).getValues());
            } else {
                return new RawMarker(root, source, structure);
            }
        });
    }

    private EnumMarker scanEnumMarker(@NotNull String source,
                                      @NotNull String rootName,
                                      @NotNull PsiClass rootClass,
                                      @NotNull PsiType type) {
        final String fullName = getFileFullName(type);
        return getPsiJavaClass(type)
                .map(psiClass -> Arrays.stream(psiClass.getFields())
                        .filter(f -> f instanceof PsiEnumConstant)
                        .map(NavigationItem::getName)
                        .collect(Collectors.toList()))
                .map(values -> new EnumMarker(rootName, fullName, values))
                .orElseGet(() -> scanJavaInnerClass(rootClass, type)
                        .filter(m -> m instanceof EnumMarker)
                        .map(EnumMarker.class::cast)
                        .orElseGet(() -> new EnumMarker(rootName, fullName, Collections.emptyList())));
    }

    private ArrayMarker scanArrayMarker(@NotNull String source,
                                        @NotNull String rootName,
                                        @NotNull PsiClass rootClass,
                                        @NotNull PsiType type) {
        final Marker marker = Optional.of(getArrayPsiType((PsiArrayType) type))
                .map(p -> getMarkerFromPsiType(source, rootName, rootClass, p))
                .orElseGet(() -> new TypedMarker(rootName, source, String.class));

        return new ArrayMarker(rootName, source, marker, type.getArrayDimensions());
    }

    private PsiType getArrayPsiType(PsiArrayType psiArrayType) {
        final PsiType componentType = psiArrayType.getComponentType();
        return (componentType instanceof PsiArrayType)
                ? getArrayPsiType(((PsiArrayType) componentType))
                : componentType;
    }

    private CollectionMarker scanCollectionMarker(@NotNull String source,
                                                  @NotNull String rootName,
                                                  @NotNull PsiClass rootClass,
                                                  @NotNull PsiType type) {
        final Marker marker = Optional.of(((PsiClassReferenceType) type).getParameters())
                .filter(p -> p.length == 1)
                .map(p -> getMarkerFromPsiType(source, rootName, rootClass, p[0]))
                .orElseGet(() -> new TypedMarker(rootName, source, String.class));

        // noinspection ConstantConditions
        return new CollectionMarker(rootName, source, getCollectionType(type), marker);
    }

    private MapMarker scanMapMarker(@NotNull String source,
                                    @NotNull String rootName,
                                    @NotNull PsiClass rootClass,
                                    @NotNull PsiType type) {
        final Pair<Marker, Marker> pair = Optional.of(((PsiClassReferenceType) type).getParameters())
                .filter(p -> p.length == 2)
                .map(p -> {
                    final Marker keyMarker = getMarkerFromPsiType(source, rootName, rootClass, p[0]);
                    final Marker valueMarker = getMarkerFromPsiType(source, rootName, rootClass, p[1]);
                    return Pair.create(keyMarker, valueMarker);
                })
                .filter(p -> p.getFirst() != null && p.getSecond() != null)
                .orElseGet(() -> Pair.create(new TypedMarker(rootName, source, String.class),
                        new TypedMarker(rootName, source, String.class)));

        // noinspection ConstantConditions
        return new MapMarker(rootName, source, getMapType(type), pair.getFirst(), pair.getSecond());
    }

    private Marker getMarkerFromPsiType(@NotNull String source,
                                        @NotNull String rootName,
                                        @NotNull PsiClass rootClass,
                                        @NotNull PsiType type) {
        return Optional.ofNullable(getSimpleTypeByName(type))
                .map(t -> ((Marker) new TypedMarker(source, rootName, t)))
                .orElseGet(() -> scanJavaFileClass(rootClass, type)
                        .orElseGet(() -> scanJavaInnerClass(rootClass, type)
                                .orElseGet(() -> new TypedMarker(rootName, source, String.class))));
    }

    @SuppressWarnings("ConstantConditions")
    private TypedMarker scanSimpleMarker(@NotNull String source,
                                         @NotNull String rootName,
                                         @NotNull PsiType type) {
        return new TypedMarker(rootName, source, getSimpleTypeByName(type.getCanonicalText()));
    }

    private Stream<AnnotationMarker.Builder> scanAnnotations(PsiAnnotation[] psiAnnotations) {
        return Arrays.stream(psiAnnotations)
                .filter(a -> StringUtils.isNotBlank(a.getQualifiedName()))
                .map(a -> {
                    final Map<String, Object> attrs = new HashMap<>(a.getAttributes().size());
                    a.getAttributes().forEach(attr -> {
                        final Object value = getAttributeValue(attr.getAttributeValue());
                        if (value != null)
                            attrs.put(attr.getAttributeName(), value);
                    });

                    return AnnotationMarker.builder()
                            .withName(a.getQualifiedName())
                            .withAttributes(attrs);
                });
    }

    private Object getAttributeValue(JvmAnnotationAttributeValue attribute) {
        if (attribute instanceof JvmAnnotationConstantValue) {
            return ((JvmAnnotationConstantValue) attribute).getConstantValue();
        } else if (attribute instanceof JvmAnnotationArrayValue) {
            return ((JvmAnnotationArrayValue) attribute).getValues().stream()
                    .map(this::getAttributeValue)
                    .collect(Collectors.toList());
        } else {
            return null;
        }
    }

    /**
     * @param psiClass of targeted class
     * @return map of super type erasure name and targeted class erasure psiType
     */
    private Map<String, PsiType> getTypeErasures(@Nullable PsiClass psiClass) {
        if (psiClass == null || psiClass.getSuperClass() == null || psiClass.getSuperClassType() == null)
            return Collections.emptyMap();

        final Map<String, PsiType> types = new LinkedHashMap<>();
        for (JvmType argument : psiClass.getSuperClassType().typeArguments()) {
            for (PsiTypeParameter parameter : psiClass.getSuperClass().getTypeParameters()) {
                if (argument instanceof PsiType) {
                    types.put(parameter.getName(), (PsiType) argument);
                }
            }
        }
        return types;
    }

    private Optional<PsiClass> getPsiJavaParentClass(@NotNull PsiType type) {
        final String fullName = getClassFullName(type);
        final int last = fullName.lastIndexOf('.');
        if (last == -1 || last == 0)
            return Optional.empty();

        final String parentName = fullName.substring(0, last);
        final Optional<PsiClass> fileClass = getPsiJavaClassByType(type);
        return (fileClass.isPresent())
                ? fileClass
                : getPsiJavaClass(parentName, type.getResolveScope());
    }

    private Optional<PsiClass> getPsiJavaClass(@NotNull PsiType type) {
        final Optional<PsiClass> fileClass = getPsiJavaClassByType(type);

        final String fullName = getClassFullName(type);
        return (fileClass.isPresent())
                ? fileClass
                : getPsiJavaClass(fullName, type.getResolveScope());
    }

    private Optional<PsiClass> getPsiJavaClassByType(@NotNull PsiType type) {
        return Optional.of(type)
                .filter(t -> t instanceof PsiClassReferenceType)
                .map(t -> ((PsiClassReferenceType) t).getPsiContext())
                .filter(context -> context.getContainingFile() instanceof PsiJavaFile)
                .map(context -> ((PsiJavaFile) context.getContainingFile()))
                .filter(psiJavaFile -> psiJavaFile.getClasses().length != 0)
                .flatMap(psiJavaFile -> {
                    final PsiClass rootClass = psiJavaFile.getClasses()[0];
                    return Stream.concat(Stream.of(rootClass), Arrays.stream(rootClass.getAllInnerClasses()))
                            .filter(psiClass -> type.getCanonicalText().equals(psiClass.getQualifiedName()))
                            .findFirst();
                });
    }

    private Optional<PsiClass> getPsiJavaClass(@NotNull String fileName,
                                               @Nullable GlobalSearchScope globalSearchScope) {
        return Optional.ofNullable(globalSearchScope)
                .map(GlobalSearchScope::getProject)
                .flatMap(
                        p -> Optional.ofNullable(JavaPsiFacade.getInstance(p).findClass(fileName, GlobalSearchScope.allScope(p))))
                .filter(psiClass -> psiClass.getContainingFile() instanceof PsiJavaFile);
    }

    private String getFileFullName(@NotNull PsiClass psiClass) {
        return (psiClass.getLanguage() instanceof KotlinLanguage)
                ? psiClass.getQualifiedName() + ".kt"
                : psiClass.getQualifiedName() + ".java";
    }

    private String getFileFullName(@NotNull PsiType type) {
        if (type instanceof PsiClassReferenceType) {
            final PsiClass resolve = ((PsiClassReferenceType) type).resolve();
            if (resolve != null && resolve.getLanguage() instanceof KotlinLanguage) {
                return getClassFullName(type) + ".kt";
            }
        }

        return getClassFullName(type) + ".java";
    }

    private String getClassFullName(@NotNull PsiType type) {
        return type.getCanonicalText();
    }
}
