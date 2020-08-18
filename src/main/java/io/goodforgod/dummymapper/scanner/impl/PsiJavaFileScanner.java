package io.goodforgod.dummymapper.scanner.impl;

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
import io.goodforgod.dummymapper.error.JavaKindException;
import io.goodforgod.dummymapper.error.ScanException;
import io.goodforgod.dummymapper.marker.*;
import io.goodforgod.dummymapper.model.AnnotationMarker;
import io.goodforgod.dummymapper.model.AnnotationMarkerBuilder;
import io.goodforgod.dummymapper.scanner.IFileScanner;
import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.goodforgod.dummymapper.util.PsiClassUtils.*;

/**
 * Scan PsiJavaFile structure of fields and their annotations (also getters and setters annotations for such fields)
 * <p>
 * https://intellij-support.jetbrains.com/hc/en-us/community/posts/360002746839-How-to-add-an-annotation-and-import-to-a-Java-class
 *
 * @author GoodforGod
 * @since 27.11.2019
 */
@SuppressWarnings("UnstableApiUsage")
public class PsiJavaFileScanner implements IFileScanner {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final Map<String, Map<String, Marker>> scanned = new HashMap<>();

    public @NotNull RawMarker scan(@Nullable PsiFile file) {
        if (!(file instanceof PsiJavaFile)) {
            logger.debug("File is not PsiJavaFile type");
            return RawMarker.EMPTY;
        }

        final PsiJavaFile javaFile = (PsiJavaFile) file;
        if (javaFile.getClasses().length == 0)
            return RawMarker.EMPTY;

        final PsiClass target = javaFile.getClasses()[0];
        if (JvmClassKind.ENUM.equals(target.getClassKind())
                || JvmClassKind.ANNOTATION.equals(target.getClassKind())
                || JvmClassKind.INTERFACE.equals(target.getClassKind()))
            throw new JavaKindException(target.getClassKind());

        final Map<String, Marker> scannedFile = scanJavaFile(javaFile);
        if (scannedFile.isEmpty())
            return RawMarker.EMPTY;

        final String source = getFileFullName(target);
        final String root = getFileFullName(javaFile);
        return new RawMarker(root, source, scannedFile);
    }

    private @NotNull Map<String, Marker> scanJavaFile(@Nullable PsiJavaFile file) {
        try {
            if (file == null || file.getClasses().length == 0)
                return Collections.emptyMap();

            final PsiClass target = file.getClasses()[0];
            if (isTypeSimple(getFileFullName(target)) || isTypeEnum(getFileFullName(target)))
                return Collections.emptyMap();

            return scanJavaClass(target, target, Collections.emptyMap());
        } catch (Exception e) {
            throw new ScanException(e);
        }
    }

    private Map<String, Marker> scanJavaClass(@NotNull PsiClass rootClass,
                                              @NotNull PsiClass targetClass,
                                              @NotNull Map<String, PsiType> parentTypes) {
        final String source = getFileFullName(targetClass);
        final String root = getFileFullName(rootClass);

        final PsiClass superTarget = targetClass.getSuperClass();
        final Map<String, Marker> structure = new LinkedHashMap<>();

        if (superTarget != null && Enum.class.getName().equals(superTarget.getQualifiedName())) {
            final List<String> enumValues = Arrays.stream(targetClass.getFields())
                    .filter(f -> f instanceof PsiEnumConstant)
                    .map(PsiField::getName)
                    .collect(Collectors.toList());
            structure.put(targetClass.getName(), new EnumMarker(root, source, enumValues));
            scanned.put(source, structure);
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
                        .orElseGet(() -> (v.getSuperTypes().length > index) ? v.getSuperTypes()[index] : null);
                if (type != null)
                    types.put(key, type);

                valueCounter++;
            }

            final Map<String, Marker> superScan = scanJavaClass(rootClass, superTarget, types);
            structure.putAll(superScan);
        }

        // TODO restructure code so that it will be more obvious to use cache for structure if possible

        // Get cached structure if file was not changed since last scan
        // final long modificationStamp = target.getContainingFile().getModificationStamp();
        // if (scannedModified.getOrDefault(source, -1L).equals(modificationStamp)) {
        // logger.debug("Retrieving CACHED structure for '{}' with modifyStamp '{}'", source, modificationStamp);
        // final Map<String, Marker> cached = scanned.get(source);
        // cached.putAll(structure); // put all parent scanned fields in case they changed
        // return cached;
        // }

        scanned.put(source, structure);
        final PsiField[] fields = targetClass.getFields();

        for (PsiField field : fields) {
            final String fieldName = field.getName();
            final PsiType type = field.getType();
            final String typeName = type.getPresentableText();

            if (isTypeEnum(type)) {
                final EnumMarker marker = scanEnumMarker(source, root, rootClass, type);
                structure.put(fieldName, marker);
                logger.debug("Field '{}' is ENUM type with '{}' values",
                        fieldName, marker.getValues().size());

            } else if (isFieldValid(field)) {
                if (isTypeArray(type)) {
                    final ArrayMarker marker = scanArrayMarker(source, root, targetClass, type);
                    structure.put(fieldName, marker);
                    logger.debug("Field '{}' is ARRAY type", fieldName);

                } else if (isTypeCollection(type)) {
                    final CollectionMarker marker = scanCollectionMarker(source, root, targetClass, type);
                    structure.put(fieldName, marker);
                    logger.debug("Field '{}' is '{}' COLLECTION type",
                            fieldName, marker.getType().getSimpleName());

                } else if (isTypeMap(type)) {
                    final MapMarker marker = scanMapMarker(source, root, targetClass, type);
                    structure.put(fieldName, marker);
                    logger.debug("Field '{}' is '{}' MAP type",
                            fieldName, marker.getType().getSimpleName());

                } else if (isTypeSimple(type)) {
                    final TypedMarker marker = scanSimpleMarker(source, root, type);
                    structure.put(fieldName, marker);
                    logger.debug("Field '{}' is '{}' KNOWN type",
                            fieldName, marker.getType().getSimpleName());

                } else if (parentTypes.containsKey(typeName)) { // PARENT CLASS ERASURE
                    final PsiType parentType = parentTypes.get(typeName);
                    if (parentType != null && isTypeSimple(parentType)) {
                        final TypedMarker marker = scanSimpleMarker(source, root, parentType);
                        structure.put(fieldName, marker);
                        logger.debug("Field '{}' is had PARENT ERASURE '{}' type",
                                fieldName, marker.getType().getSimpleName());
                    }

                } else if (!isTypeForbidden(type)) { // COMPLEX CLASS SCAN IF NOT FORBIDDEN ONE
                    logger.debug("Scanning field '{}' for Java Class '{}'", fieldName, typeName);
                    final Optional<Marker> marker = scanJavaFileClass(root, type);
                    if (marker.isPresent()) {
                        structure.put(fieldName, marker.get());
                        logger.debug("Scanned field '{}' as Java Class", fieldName);
                    } else {
                        logger.debug("Scanning field '{}' for Java Inner Class '{}'", fieldName, typeName);
                        scanJavaInnerClass(targetClass, type).ifPresent(m -> {
                            structure.put(fieldName, m);
                            logger.debug("Scanned field '{}' as Java Inner Class", fieldName);
                        });
                    }
                }
            }

            structure.computeIfPresent(fieldName, (k, v) -> {
                final Collection<AnnotationMarker> annotations = scanMarkerAnnotations(targetClass, field);
                return v.setAnnotations(annotations);
            });
        }

        logger.debug("SCANNED structure with '{}' fields for source '{}' and root '{}'", structure.size(), source, root);
        return structure;
    }

    private Collection<AnnotationMarker> scanMarkerAnnotations(@NotNull PsiClass targetClass,
                                                               @NotNull PsiField field) {
        final String fieldName = field.getName();
        final List<AnnotationMarker> annotations = scanAnnotations(field.getAnnotations())
                .map(AnnotationMarkerBuilder::ofField)
                .map(AnnotationMarkerBuilder::build)
                .collect(Collectors.toList());

        logger.debug("Scanned '{}' annotations for field '{}'", annotations.size(), fieldName);

        final List<AnnotationMarker> methodAnnotations = Arrays.stream(targetClass.getAllMethods())
                .filter(m -> ArrayUtils.isNotEmpty(m.getAnnotations()))
                .filter(m -> m.getName().length() > 3)
                .filter(m -> fieldName.equalsIgnoreCase(m.getName().substring(3)))
                .flatMap(m -> scanAnnotations(m.getAnnotations())
                        .map(b -> m.getName().startsWith("set") ? b.ofSetter() : b.ofGetter())
                        .map(AnnotationMarkerBuilder::build))
                .collect(Collectors.toList());

        logger.debug("Found '{}' annotations from methods", annotations.size());

        return Stream.of(annotations, methodAnnotations)
                .flatMap(List::stream)
                .collect(Collectors.toSet());
    }

    private Optional<Marker> scanJavaInnerClass(@NotNull PsiClass rootClass,
                                                @NotNull PsiType type) {
        final String root = getFileFullName(rootClass);
        return getParentJavaFile(type)
                .filter(file -> file.getClasses().length == 1)
                .flatMap(file -> Arrays.stream(file.getClasses()[0].getAllInnerClasses())
                        .filter(c -> type.getCanonicalText().equals(c.getQualifiedName()))
                        .findFirst()
                        .map(c -> {
                            final String fullName = getFileFullName(c);
                            final Map<String, Marker> cached = scanned.get(fullName);
                            final Map<String, Marker> structure = (cached == null) ? scanJavaClass(c, c, new HashMap<>())
                                    : cached;

                            final Marker marker = structure.get(type.getPresentableText());
                            if (marker instanceof EnumMarker) { // ENUM
                                return new EnumMarker(root, marker.getSource(), ((EnumMarker) marker).getValues());
                            } else {
                                return new RawMarker(root, fullName, structure);
                            }
                        }));
    }

    private Optional<Marker> scanJavaFileClass(@NotNull String root,
                                               @NotNull PsiType type) {
        return getJavaFile(type).map(f -> {
            final String fullName = getClassFullName(f);
            final Map<String, Marker> cached = scanned.get(fullName);
            final Map<String, Marker> structure = (cached == null) ? scanJavaFile(f) : cached;

            final Marker marker = structure.get(type.getPresentableText());
            if (marker instanceof EnumMarker) { // ENUM
                return new EnumMarker(root, marker.getSource(), ((EnumMarker) marker).getValues());
            } else {
                return new RawMarker(root, fullName, structure);
            }
        });
    }

    private EnumMarker scanEnumMarker(@NotNull String source,
                                      @NotNull String rootName,
                                      @NotNull PsiClass rootClass,
                                      @NotNull PsiType type) {
        final String fullName = getFileFullName(type);
        return getJavaFile(type)
                .map(PsiClassOwner::getClasses)
                .filter(c -> c.length > 0)
                .map(c -> Arrays.stream(c[0].getFields())
                        .filter(f -> f instanceof PsiEnumConstant)
                        .map(NavigationItem::getName)
                        .collect(Collectors.toList()))
                .map(values -> new EnumMarker(rootName, fullName, values))
                .orElseGet(() -> scanJavaInnerClass(rootClass, type)
                        .filter(m -> m instanceof EnumMarker)
                        .map(m -> ((EnumMarker) m))
                        .orElseGet(() -> new EnumMarker(rootName, fullName, Collections.emptyList())));
    }

    private ArrayMarker scanArrayMarker(@NotNull String source,
                                        @NotNull String rootName,
                                        @NotNull PsiClass rootClass,
                                        @NotNull PsiType type) {
        final Marker marker = Optional.of(((PsiArrayType) type).getComponentType())
                .map(p -> getMarkerFromPsiType(source, rootName, rootClass, p))
                .orElseGet(() -> new TypedMarker(rootName, source, String.class));

        return new ArrayMarker(rootName, source, marker);
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
                .orElseGet(() -> scanJavaFileClass(rootName, type)
                        .orElseGet(() -> scanJavaInnerClass(rootClass, type)
                                .orElseGet(() -> new TypedMarker(rootName, source, String.class))));
    }

    @SuppressWarnings("ConstantConditions")
    private TypedMarker scanSimpleMarker(@NotNull String source,
                                         @NotNull String rootName,
                                         @NotNull PsiType type) {
        return new TypedMarker(rootName, source, getSimpleTypeByName(type.getCanonicalText()));
    }

    private Stream<AnnotationMarkerBuilder> scanAnnotations(PsiAnnotation[] psiAnnotations) {
        return Arrays.stream(psiAnnotations)
                .filter(a -> StringUtils.isNotBlank(a.getQualifiedName()))
                .map(a -> {
                    final Map<String, Object> attrs = new HashMap<>(a.getAttributes().size());
                    a.getAttributes().forEach(attr -> {
                        final Object value = getAttributeValue(attr.getAttributeValue());
                        if (value != null)
                            attrs.put(attr.getAttributeName(), value);
                    });

                    return AnnotationMarkerBuilder.get()
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

    private Optional<PsiJavaFile> getParentJavaFile(@NotNull PsiType type) {
        final String fullName = getClassFullName(type);
        final int last = fullName.lastIndexOf('.');
        if (last == -1 || last == 0)
            return Optional.empty();

        final String parentName = fullName.substring(0, last);
        return getJavaFile(parentName, type.getResolveScope());
    }

    private Optional<PsiJavaFile> getJavaFile(@NotNull PsiType type) {
        final String fullName = getClassFullName(type);
        return getJavaFile(fullName, type.getResolveScope());
    }

    private Optional<PsiJavaFile> getJavaFile(@NotNull String fileName,
                                              @Nullable GlobalSearchScope globalSearchScope) {
        return Optional.ofNullable(globalSearchScope)
                .map(GlobalSearchScope::getProject)
                .flatMap(
                        p -> Optional.ofNullable(JavaPsiFacade.getInstance(p).findClass(fileName, GlobalSearchScope.allScope(p))))
                .map(PsiElement::getContainingFile)
                .filter(f -> f instanceof PsiJavaFile)
                .map(f -> ((PsiJavaFile) f))
                .filter(f -> getFileFullName(f).startsWith(fileName));
    }

    private String getClassFullName(@NotNull PsiJavaFile file) {
        final String fileFullName = getFileFullName(file);
        if (file.getClasses().length == 0)
            throw new ScanException("Can not find class full name for file:" + fileFullName);

        final PsiClass target = file.getClasses()[0];
        return getFileFullName(target);
    }

    private String getFileFullName(@NotNull PsiJavaFile file) {
        return file.getPackageName() + "." + file.getName();
    }

    private String getFileFullName(@NotNull PsiClass psiClass) {
        return psiClass.getQualifiedName() + ".java";
    }

    private String getFileFullName(@NotNull PsiType type) {
        return getClassFullName(type) + ".java";
    }

    private String getClassFullName(@NotNull PsiType type) {
        return type.getCanonicalText();
    }
}
