package io.goodforgod.dummymapper.service;

import com.intellij.lang.jvm.annotation.JvmAnnotationArrayValue;
import com.intellij.lang.jvm.annotation.JvmAnnotationAttributeValue;
import com.intellij.lang.jvm.annotation.JvmAnnotationConstantValue;
import com.intellij.lang.jvm.types.JvmType;
import com.intellij.navigation.NavigationItem;
import com.intellij.openapi.util.Pair;
import com.intellij.psi.*;
import com.intellij.psi.impl.source.PsiClassReferenceType;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import io.dummymaker.util.StringUtils;
import io.goodforgod.dummymapper.error.ScanException;
import io.goodforgod.dummymapper.marker.*;
import io.goodforgod.dummymapper.model.AnnotationMarker;
import io.goodforgod.dummymapper.model.AnnotationMarkerBuilder;
import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.goodforgod.dummymapper.util.PsiClassUtils.*;

/**
 * Scans java file and recreates its structure as map JetBrains class loader
 * (only methods and only portion of them)
 * https://intellij-support.jetbrains.com/hc/en-us/community/posts/360002746839-How-to-add-an-annotation-and-import-to-a-Java-class
 *
 * @author GoodforGod
 * @since 27.11.2019
 */
public class PsiJavaFileScanner {

    private final Map<String, Map<String, Marker>> scanned = new HashMap<>();

    public RawMarker scan(@Nullable PsiJavaFile file) {
        if (file == null || file.getClasses().length == 0)
            return RawMarker.EMPTY;

        final Map<String, Marker> scanned = scanJavaFile(file, file);
        if (scanned.isEmpty())
            return RawMarker.EMPTY;

        final PsiClass target = file.getClasses()[0];
        final String source = getFullName(target);
        final String root = getFullName(file);
        return new RawMarker(root, source, scanned);
    }

    private Map<String, Marker> scanJavaFile(@Nullable PsiJavaFile current,
                                             @Nullable PsiJavaFile file) {
        try {
            if (file == null || current == null || file.getClasses().length == 0)
                return Collections.emptyMap();

            final PsiClass target = file.getClasses()[0];
            if (isTypeSimple(getFullName(target)) || isTypeEnum(getFullName(target)))
                return Collections.emptyMap();

            return scanJavaClass(current, target, Collections.emptyMap());
        } catch (Exception e) {
            throw new ScanException(e);
        }
    }

    private Map<String, Marker> scanJavaClass(@NotNull PsiJavaFile rootFile,
                                              @NotNull PsiClass targetFile,
                                              @NotNull Map<String, PsiType> parentTypes) {
        final Map<String, Marker> structure = new LinkedHashMap<>();
        final PsiClass superTarget = targetFile.getSuperClass();

        if (superTarget != null && !isTypeSimple(superTarget.getQualifiedName())) { // SCAN PARENT CLASS
            final Map<String, PsiType> types = getTypeErasures(targetFile);
            final Map<String, PsiType> unknownParentTypes = types.entrySet().stream()
                    .filter(e -> !isTypeSimple(e.getValue().getPresentableText()))
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

            unknownParentTypes.forEach((k, v) -> {
                final PsiType type = parentTypes.get(v.getPresentableText());
                if (type != null)
                    types.put(k, type);
            });

            final Map<String, Marker> superScan = scanJavaClass(rootFile, superTarget, types);
            structure.putAll(superScan);
        }

        final PsiField[] fields = targetFile.getFields();
        final String source = getFullName(targetFile);
        final String root = getFullName(rootFile);
        scanned.put(source, structure);

        for (PsiField field : fields) {
            final Set<AnnotationMarker> annotations = scanAnnotations(field.getAnnotations())
                    .map(AnnotationMarkerBuilder::ofField)
                    .map(AnnotationMarkerBuilder::build)
                    .collect(Collectors.toSet());

            final PsiType type = field.getType();
            if (isTypeEnum(type)) {
                final EnumMarker marker = scanEnumMarker(source, root, type)
                        .setAnnotations(annotations);
                structure.put(field.getName(), marker);

            } else if (isFieldValid(field)) {
                if (isTypeCollection(type)) {
                    final CollectionMarker marker = scanCollectionMarker(source, root, type)
                            .setAnnotations(annotations);
                    structure.put(field.getName(), marker);

                } else if (isTypeMap(type)) {
                    final MapMarker marker = scanMapMarker(source, root, type)
                            .setAnnotations(annotations);
                    structure.put(field.getName(), marker);

                } else if (isTypeSimple(type)) {
                    final TypedMarker marker = scanSimpleMarker(source, root, type)
                            .setAnnotations(annotations);
                    structure.put(field.getName(), marker);

                } else if (parentTypes.containsKey(type.getPresentableText())) { // PARENT CLASS ERASURE
                    final PsiType parentType = parentTypes.get(type.getPresentableText());
                    if (parentType != null && isTypeSimple(parentType)) {
                        final TypedMarker marker = scanSimpleMarker(source, root, parentType)
                                .setAnnotations(annotations);
                        structure.put(field.getName(), marker);
                    }

                } else { // COMPLEX CLASS SCAN
                    final Optional<Marker> marker = scanJavaComplexMarker(root, type);
                    marker.map(m -> ((Marker) m.setAnnotations(annotations)))
                            .ifPresent(m -> structure.put(field.getName(), m));
                }
            }
        }

        final Map<String, Collection<AnnotationMarker>> methodAnnotations = new HashMap<>(structure.size());
        for (String field : structure.keySet()) {
            Arrays.stream(targetFile.getAllMethods())
                    .filter(m -> ArrayUtils.isNotEmpty(m.getAnnotations()))
                    .filter(m -> m.getName().length() > 3)
                    .filter(m -> field.equalsIgnoreCase(m.getName().substring(3)))
                    .forEach(m -> {
                        final Set<AnnotationMarker> annotations = scanAnnotations(m.getAnnotations())
                                .map(b -> m.getName().startsWith("set")
                                        ? b.ofSetter()
                                        : b.ofGetter())
                                .map(AnnotationMarkerBuilder::build)
                                .collect(Collectors.toSet());

                        if (!annotations.isEmpty())
                            methodAnnotations.put(field, annotations);
                    });
        }

        methodAnnotations.forEach((k, v) -> {
            final Marker marker = structure.get(k);
            final List<AnnotationMarker> mergedAnnotations = Stream.of(marker.getAnnotations(), v)
                    .flatMap(Collection::stream)
                    .collect(Collectors.toList());
            marker.setAnnotations(mergedAnnotations);
        });

        return structure;
    }

    private Optional<Marker> scanJavaComplexMarker(@NotNull String rootName,
                                                   @NotNull PsiType type) {
        return getResolvedJavaFile(type).map(f -> {
            final String fieldJavaFullName = getFullName(f);
            final Map<String, Marker> map = scanned.get(fieldJavaFullName);
            if (map != null)
                return new RawMarker(rootName, fieldJavaFullName, map);

            final Map<String, Marker> scannedComplexField = scanJavaFile(f, f);
            final Object values = scannedComplexField.get(type.getPresentableText());
            if (values instanceof Collection) { // ENUM
                return new EnumMarker(rootName, fieldJavaFullName, (Collection) values);
            } else {
                return new RawMarker(rootName, fieldJavaFullName, scannedComplexField);
            }
        });
    }

    private EnumMarker scanEnumMarker(@NotNull String source,
                                      @NotNull String rootName,
                                      @NotNull PsiType type) {
        final List<String> enumValues = getResolvedJavaFile(type)
                .map(PsiClassOwner::getClasses)
                .filter(c -> c.length > 0)
                .map(c -> Arrays.stream(c[0].getFields()))
                .orElse(Stream.empty())
                .filter(f -> f instanceof PsiEnumConstant)
                .map(NavigationItem::getName)
                .collect(Collectors.toList());

        return new EnumMarker(rootName, source, enumValues);
    }

    private CollectionMarker scanCollectionMarker(@NotNull String source,
                                                  @NotNull String rootName,
                                                  @NotNull PsiType type) {
        final Marker marker = Optional.of(((PsiClassReferenceType) type).getParameters())
                .filter(p -> p.length == 1)
                .map(p -> getSimpleTypeByName(p[0]))
                .filter(Objects::nonNull)
                .map(t -> ((Marker) new TypedMarker(source, rootName, t)))
                .orElseGet(() -> Optional.of(((PsiClassReferenceType) type).getParameters())
                        .filter(p -> p.length == 1)
                        .flatMap(p -> scanJavaComplexMarker(rootName, p[0]))
                        .orElseGet(() -> new TypedMarker(rootName, source, String.class)));

        return new CollectionMarker(rootName, source, getCollectionType(type), marker);
    }

    private MapMarker scanMapMarker(@NotNull String source,
                                    @NotNull String rootName,
                                    @NotNull PsiType type) {
        final Pair<Marker, Marker> pair = Optional.of(((PsiClassReferenceType) type).getParameters())
                .filter(p -> p.length == 2)
                .map(p -> Pair.create(getSimpleTypeByName(p[0]), getSimpleTypeByName(p[1])))
                .filter(p -> p.getFirst() != null && p.getSecond() != null)
                .map(p -> Pair.create(((Marker) new TypedMarker(source, rootName, p.getFirst())),
                        (Marker) new TypedMarker(source, rootName, p.getSecond())))
                .orElseGet(() -> Optional.of(((PsiClassReferenceType) type).getParameters())
                        .filter(p -> p.length == 2)
                        .map(p -> scanJavaComplexMarker(rootName, p[0])
                                .map(t1 -> scanJavaComplexMarker(rootName, p[1])
                                        .map(t2 -> Pair.create(t1, t2)).orElseGet(Pair::empty))
                                .orElseGet(Pair::empty))
                        .filter(p -> p.getFirst() != null && p.getSecond() != null)
                        .orElseGet(() -> Pair.create(new TypedMarker(rootName, source, String.class),
                                new TypedMarker(rootName, source, String.class))));

        return new MapMarker(rootName, source, getMapType(type), pair.getFirst(), pair.getSecond());
    }

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

    private Optional<PsiJavaFile> getResolvedJavaFile(@NotNull PsiType type) {
        return Optional.ofNullable(type.getResolveScope())
                .map(GlobalSearchScope::getProject)
                .flatMap(p -> Arrays.stream(FilenameIndex.getFilesByName(p,
                        type.getPresentableText() + ".java",
                        GlobalSearchScope.allScope(p)))
                        .findFirst()
                        .filter(f -> f instanceof PsiJavaFile)
                        .map(f -> ((PsiJavaFile) f)));
    }

    private String getFullName(@NotNull PsiJavaFile file) {
        return file.getPackageName() + "." + file.getName();
    }

    private String getFullName(@NotNull PsiClass psiClass) {
        return psiClass.getQualifiedName() + ".java";
    }
}
