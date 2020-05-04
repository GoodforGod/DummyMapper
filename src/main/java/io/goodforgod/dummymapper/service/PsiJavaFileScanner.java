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
public class PsiJavaFileScanner {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final Map<String, Map<String, Marker>> scanned = new HashMap<>();

    public RawMarker scan(@Nullable PsiJavaFile file) {
        if (file == null || file.getClasses().length == 0)
            return RawMarker.EMPTY;

        final Map<String, Marker> scanned = scanJavaFile(file);
        if (scanned.isEmpty())
            return RawMarker.EMPTY;

        final PsiClass target = file.getClasses()[0];
        final String source = getFullName(target);
        final String root = getFullName(file);
        return new RawMarker(root, source, scanned);
    }

    private Map<String, Marker> scanJavaFile(@Nullable PsiJavaFile file) {
        try {
            if (file == null || file.getClasses().length == 0)
                return Collections.emptyMap();

            final PsiClass target = file.getClasses()[0];
            if (isTypeSimple(getFullName(target)) || isTypeEnum(getFullName(target)))
                return Collections.emptyMap();

            return scanJavaClass(target, target, Collections.emptyMap());
        } catch (Exception e) {
            throw new ScanException(e);
        }
    }

    private Map<String, Marker> scanJavaClass(@NotNull PsiClass rootClass,
                                              @NotNull PsiClass targetClass,
                                              @NotNull Map<String, PsiType> parentTypes) {
        final Map<String, Marker> structure = new LinkedHashMap<>();
        final PsiClass superTarget = targetClass.getSuperClass();

        if (superTarget != null && !isTypeSimple(superTarget.getQualifiedName())) { // SCAN PARENT CLASS
            final Map<String, PsiType> types = getTypeErasures(targetClass);
            final Map<String, PsiType> unknownParentTypes = types.entrySet().stream()
                    .filter(e -> !isTypeSimple(e.getValue().getPresentableText()))
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

            unknownParentTypes.forEach((k, v) -> {
                final PsiType type = parentTypes.get(v.getPresentableText());
                if (type != null)
                    types.put(k, type);
            });

            final Map<String, Marker> superScan = scanJavaClass(rootClass, superTarget, types);
            structure.putAll(superScan);
        }

        final PsiField[] fields = targetClass.getFields();
        final String source = getFullName(targetClass);
        final String root = getFullName(rootClass);

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

        for (PsiField field : fields) {
            final String fieldName = field.getName();
            final PsiType type = field.getType();
            final String typeName = type.getPresentableText();
            final Set<AnnotationMarker> annotations = scanAnnotations(field.getAnnotations())
                    .map(AnnotationMarkerBuilder::ofField)
                    .map(AnnotationMarkerBuilder::build)
                    .collect(Collectors.toSet());

            logger.debug("Scanned '{}' annotations for field '{}'", annotations.size(), fieldName);

            if (isTypeEnum(type)) {
                final EnumMarker marker = scanEnumMarker(source, root, type)
                        .setAnnotations(annotations);
                structure.put(fieldName, marker);
                logger.debug("Field '{}' is ENUM type with '{}' values",
                        fieldName, marker.getValues().size());

            } else if (isFieldValid(field)) {
                if (isTypeCollection(type)) {
                    final CollectionMarker marker = scanCollectionMarker(source, root, type)
                            .setAnnotations(annotations);
                    structure.put(fieldName, marker);
                    logger.debug("Field '{}' is '{}' COLLECTION type",
                            fieldName, marker.getType().getSimpleName());

                } else if (isTypeMap(type)) {
                    final MapMarker marker = scanMapMarker(source, root, type)
                            .setAnnotations(annotations);
                    structure.put(fieldName, marker);
                    logger.debug("Field '{}' is '{}' MAP type",
                            fieldName, marker.getType().getSimpleName());

                } else if (isTypeSimple(type)) {
                    final TypedMarker marker = scanSimpleMarker(source, root, type)
                            .setAnnotations(annotations);
                    structure.put(fieldName, marker);
                    logger.debug("Field '{}' is '{}' KNOWN type",
                            fieldName, marker.getType().getSimpleName());

                } else if (parentTypes.containsKey(typeName)) { // PARENT CLASS ERASURE
                    final PsiType parentType = parentTypes.get(typeName);
                    if (parentType != null && isTypeSimple(parentType)) {
                        final TypedMarker marker = scanSimpleMarker(source, root, parentType)
                                .setAnnotations(annotations);
                        structure.put(fieldName, marker);
                        logger.debug("Field '{}' is had PARENT ERASURE '{}' type",
                                fieldName, marker.getType().getSimpleName());
                    }

                } else { // COMPLEX CLASS SCAN
                    logger.debug("Scanning field '{}' for Java Class '{}'", fieldName, typeName);
                    final Optional<Marker> marker = scanJavaFileClass(root, type);
                    if (marker.isPresent()) {
                        structure.put(fieldName, marker.get().setAnnotations(annotations));
                        logger.debug("Scanned field '{}' as Java Class", fieldName);
                    } else {
                        logger.debug("Scanning field '{}' for Java Inner Class '{}'", fieldName, typeName);
                        scanJavaInnerClass(targetClass, type).ifPresent(m -> {
                            structure.put(fieldName, m.setAnnotations(annotations));
                            logger.debug("Scanned field '{}' as Java Inner Class", fieldName);
                        });
                    }
                }
            }
        }

        final Map<String, Collection<AnnotationMarker>> methodAnnotations = new HashMap<>(structure.size());
        for (String field : structure.keySet()) {
            Arrays.stream(targetClass.getAllMethods())
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

                        if (!annotations.isEmpty()) {
                            logger.debug("Found '{}' annotations for method '{}'", annotations.size(), m.getName());
                            methodAnnotations.put(field, annotations);
                        }
                    });
        }

        methodAnnotations.forEach((k, v) -> {
            final Marker marker = structure.get(k);
            final List<AnnotationMarker> mergedAnnotations = Stream.of(marker.getAnnotations(), v)
                    .flatMap(Collection::stream)
                    .collect(Collectors.toList());
            marker.setAnnotations(mergedAnnotations);
        });

        logger.debug("SCANNED structure with '{}' fields for source '{}' and root '{}'", structure.size(), source, root);
        return structure;
    }

    private Optional<Marker> scanJavaInnerClass(@NotNull PsiClass rootClass,
                                                @NotNull PsiType type) {
        final PsiFile file = rootClass.getContainingFile();
        if(file instanceof PsiJavaFile) {
            final String rootName = getFullName((PsiJavaFile) file);
            return Arrays.stream(((PsiJavaFile) file).getClasses()[0].getAllInnerClasses())
                    .filter(c -> type.getCanonicalText().equals(c.getQualifiedName()))
                    .findFirst()
                    .map(c -> {
                        final String fullName = getFullName(c);
                        final Map<String, Marker> cached = scanned.get(fullName);
                        final Map<String, Marker> structure = (cached == null) ? scanJavaClass(c, c, new HashMap<>()) : cached;
                        return new RawMarker(rootName, fullName, structure);
                    });
        }

        return Optional.empty();
    }

    private Optional<Marker> scanJavaFileClass(@NotNull String rootName,
                                               @NotNull PsiType type) {
        return getResolvedJavaFile(type).map(f -> {
            final String fullName = getFullName(f);
            final Map<String, Marker> cached = scanned.get(fullName);
            final Map<String, Marker> structure = (cached == null) ? scanJavaFile(f) : cached;

            final Object values = structure.get(type.getPresentableText());
            if (values instanceof Collection) { // ENUM
                return new EnumMarker(rootName, fullName, (Collection) values);
            } else {
                return new RawMarker(rootName, fullName, structure);
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
                        .flatMap(p -> scanJavaFileClass(rootName, p[0]))
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
                        .map(p -> scanJavaFileClass(rootName, p[0])
                                .map(t1 -> scanJavaFileClass(rootName, p[1])
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
