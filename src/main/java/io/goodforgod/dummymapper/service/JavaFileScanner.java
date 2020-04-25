package io.goodforgod.dummymapper.service;

import com.intellij.lang.jvm.types.JvmType;
import com.intellij.navigation.NavigationItem;
import com.intellij.openapi.util.Pair;
import com.intellij.psi.*;
import com.intellij.psi.impl.source.PsiClassReferenceType;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import io.goodforgod.dummymapper.model.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.goodforgod.dummymapper.util.ClassUtils.*;

/**
 * Scans java file and recreates its structure as map
 *
 * @author GoodforGod
 * @since 27.11.2019
 */
public class JavaFileScanner {

    private final Map<String, Map> scanned = new HashMap<>();

    public Map<String, Object> scan(@Nullable PsiJavaFile file) {
        return scanJavaFile(file, file);
    }

    private Map<String, Object> scanJavaFile(@Nullable PsiJavaFile root,
                                             @Nullable PsiJavaFile file) {
        try {
            if (file == null || root == null || file.getClasses().length < 1)
                return Collections.emptyMap();

            final PsiClass target = file.getClasses()[0];
            if (isTypeSimple(getFullName(target)) || isTypeEnum(getFullName(target)))
                return Collections.emptyMap();

            return scanJavaClass(root, target, Collections.emptyMap());
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyMap();
        }
    }

    private Map<String, Object> scanJavaClass(@NotNull PsiJavaFile root,
                                              @NotNull PsiClass target,
                                              @NotNull Map<String, PsiType> parentTypes) {
        final Map<String, Object> structure = new LinkedHashMap<>();

        final PsiClass superTarget = target.getSuperClass();
        if (superTarget != null && !isTypeSimple(superTarget.getQualifiedName())) { // SCAN PARENT CLASS
            final Map<String, PsiType> types = getTypeErasures(target);
            final Map<String, PsiType> unknownParentTypes = types.entrySet().stream()
                    .filter(e -> !isTypeSimple(e.getValue().getPresentableText()))
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

            unknownParentTypes.forEach((k, v) -> {
                final PsiType type = parentTypes.get(v.getPresentableText());
                if (type != null)
                    types.put(k, type);
            });

            final Map<String, Object> superScan = scanJavaClass(root, superTarget, types);
            structure.putAll(superScan);
        }

        final PsiField[] fields = target.getFields();
        final String source = getFullName(target);
        final String rootName = getFullName(root);
        scanned.put(source, structure);

        for (PsiField field : fields) {
            final PsiType type = field.getType();
            if (isTypeEnum(type)) {
                final EnumMarker marker = scanEnumMarker(source, rootName, type);
                structure.put(field.getName(), marker);

            } else if (isFieldValid(field)) {
                if (isTypeCollection(type)) {
                    final CollectionMarker marker = scanCollectionMarker(source, rootName, type);
                    structure.put(field.getName(), marker);

                } else if (isTypeMap(type)) {
                    final MapMarker marker = scanMapMarker(source, rootName, type);
                    structure.put(field.getName(), marker);

                } else if (isTypeSimple(type)) {
                    final TypedMarker marker = scanSimpleMarker(source, rootName, type);
                    structure.put(field.getName(), marker);

                } else if (parentTypes.containsKey(type.getPresentableText())) { // PARENT CLASS ERASURE
                    final PsiType parentType = parentTypes.get(type.getPresentableText());
                    if (parentType != null && isTypeSimple(parentType)) {
                        final TypedMarker marker = scanSimpleMarker(source, rootName, parentType);
                        structure.put(field.getName(), marker);
                    }

                } else { // COMPLEX CLASS SCAN
                    getResolvedJavaFile(type).ifPresent(f -> {
                        final String fieldJavaFullName = getFullName(f);
                        final Map map = scanned.get(fieldJavaFullName);
                        if (map == null) {
                            final Map<String, Object> scannedComplexField = scanJavaFile(f, f);
                            final Object values = scannedComplexField.get(type.getPresentableText());
                            if (values instanceof Collection) { // ENUM
                                structure.put(field.getName(), new EnumMarker(rootName, fieldJavaFullName, (Collection) values));
                            } else {
                                structure.put(field.getName(), scannedComplexField);
                            }
                        } else {
                            structure.put(field.getName(), map);
                        }
                    });
                }
            }
        }

        return structure;
    }

    private static EnumMarker scanEnumMarker(@NotNull String source,
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

    private static CollectionMarker scanCollectionMarker(@NotNull String source,
                                                         @NotNull String rootName,
                                                         @NotNull PsiType type) {
        final Class<?> simple = Optional.of(((PsiClassReferenceType) type).getParameters())
                .filter(p -> p.length == 1)
                .map(p -> getSimpleTypeByName(p[0]))
                .filter(Objects::nonNull)
                .orElse(null);

        if (simple != null) {
            return new TypedCollectionMarker(rootName, source, getCollectionType(type), simple);
        } else {
            final String complex = Optional.of(((PsiClassReferenceType) type).getParameters())
                    .filter(p -> p.length == 1)
                    .map(p -> p[0])
                    .map(PsiType::getCanonicalText)
                    .orElse(String.class.getCanonicalName());

            return new RawCollectionMarker(rootName, source, getCollectionType(type), complex);
        }
    }

    private static MapMarker scanMapMarker(@NotNull String source,
                                           @NotNull String rootName,
                                           @NotNull PsiType type) {
        final Pair<? extends Class<?>, ? extends Class<?>> pair = Optional.of(((PsiClassReferenceType) type).getParameters())
                .filter(p -> p.length == 2)
                .map(p -> Pair.create(getSimpleTypeByName(p[0]), getSimpleTypeByName(p[1])))
                .filter(p -> p.getFirst() != null && p.getSecond() != null)
                .orElse(null);

        if (pair != null) {
            return new TypedMapMarker(rootName, source, getCollectionType(type), pair.getFirst(), pair.getSecond());
        } else {
            final Pair<String, String> rawPair = Optional.of(((PsiClassReferenceType) type).getParameters())
                    .filter(p -> p.length == 2)
                    .map(p -> Pair.create(p[0].getCanonicalText(), p[1].getCanonicalText()))
                    .orElseGet(() -> Pair.create(String.class.getCanonicalName(), String.class.getCanonicalName()));

            return new RawMapMarker(rootName, source, getCollectionType(type), rawPair.getFirst(), rawPair.getSecond());
        }
    }

    private static TypedMarker scanSimpleMarker(@NotNull String source,
                                                @NotNull String rootName,
                                                @NotNull PsiType type) {
        return new TypedMarker(rootName, source, getSimpleTypeByName(type.getCanonicalText()));
    }

    /**
     * @param psiClass of targeted class
     * @return map of super type erasure name and targeted class erasure psiType
     */
    private static Map<String, PsiType> getTypeErasures(@Nullable PsiClass psiClass) {
        if (psiClass.getSuperClass() == null || psiClass.getSuperClassType() == null)
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

    private static Optional<PsiJavaFile> getResolvedJavaFile(@NotNull PsiType type) {
        return Optional.ofNullable(type.getResolveScope())
                .map(GlobalSearchScope::getProject)
                .flatMap(p -> Arrays.stream(FilenameIndex.getFilesByName(p,
                        type.getPresentableText() + ".java",
                        GlobalSearchScope.allScope(p)))
                        .findFirst()
                        .filter(f -> f instanceof PsiJavaFile)
                        .map(f -> ((PsiJavaFile) f)));
    }

    private static String getFullName(@NotNull PsiJavaFile file) {
        return file.getPackageName() + "." + file.getName();
    }

    private static String getFullName(@NotNull PsiClass psiClass) {
        return psiClass.getQualifiedName() + ".java";
    }
}
