package io.goodforgod.dummymapper;

import com.intellij.lang.jvm.types.JvmType;
import com.intellij.navigation.NavigationItem;
import com.intellij.psi.*;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import io.goodforgod.dummymapper.model.EnumMarker;
import io.goodforgod.dummymapper.model.SimpleMarker;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.goodforgod.dummymapper.util.ClassUtils.*;

/**
 * ! NO DESCRIPTION !
 *
 * @author GoodforGod
 * @since 27.11.2019
 */
public class PsiJavaFileScanner {

    private final Map<String, Map> scanned = new HashMap<>();

    public Map<String, Object> scan(@Nullable PsiJavaFile file) {
        return scanJavaFile(file, file);
    }

    private Map<String, Object> scanJavaFile(@Nullable PsiJavaFile root,
                                             @Nullable PsiJavaFile file ) {
        try {
            if (file == null || file.getClasses().length < 1 ||  root == null) {
                return Collections.emptyMap();
            }

            final PsiClass target = file.getClasses()[0];
            if (isTypeSimple(getFullName(target)) || isTypeEnum(getFullName(target))) {
                return Collections.emptyMap();
            }

            final PsiClass superTarget = target.getSuperClass();
            if (superTarget != null && !isTypeSimple(superTarget.getQualifiedName())) {
                final Map<String, PsiType> types = getSuperTypes(target);
                final Map<String, Object> superScan = scanJavaClass(root, superTarget, types);
                final Map<String, Object> targetScan = scanJavaClass(root, target, Collections.emptyMap());
                superScan.putAll(targetScan);
                return superScan;
            } else {
                return scanJavaClass(root, target, Collections.emptyMap());
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyMap();
        }
    }

    private Map<String, Object> scanJavaClass(@NotNull PsiJavaFile root, PsiClass target, Map<String, PsiType> parentTypes) {
        final Map<String, Object> structure = new LinkedHashMap<>();
        final PsiField[] fields = target.getFields();

        final String source = getFullName(target);
        scanned.put(source, structure);

        for (PsiField field : fields) {
            if (isTypeEnum(field.getType())) {
                final List<String> enumValues = getResolvedJavaFile(field.getType())
                        .map(PsiClassOwner::getClasses)
                        .filter(c -> c.length > 0)
                        .map(c -> Arrays.stream(c[0].getFields()))
                        .orElse(Stream.empty())
                        .filter(f -> f instanceof PsiEnumConstant)
                        .map(NavigationItem::getName)
                        .collect(Collectors.toList());

                structure.put(field.getName(), new EnumMarker(source, enumValues));
            } else if (isFieldValid(field)) {
                if (isTypeSimple(field.getType())) {
                    structure.put(field.getName(), new SimpleMarker(source, getTypeByName(field.getType().getCanonicalText())));
                } else if (parentTypes.containsKey(field.getType().getPresentableText())) {
                    final PsiType type = parentTypes.get(field.getType().getPresentableText());
                    if (type != null && isTypeSimple(type)) {
                        structure.put(field.getName(), new SimpleMarker(source, getTypeByName(type.getCanonicalText())));
                    }
                } else {
                    getResolvedJavaFile(field.getType()).ifPresent(f -> {
                        final String fieldJavaFullName = getFullName(f);
                        final Map map = scanned.get(fieldJavaFullName);
                        if (map == null) {
                            final Map<String, Object> scannedComplexField = scanJavaFile(root, f);
                            final Object values = scannedComplexField.get(field.getType().getPresentableText());
                            if (values instanceof Collection) {
                                structure.put(field.getName(), new EnumMarker(fieldJavaFullName, (Collection) values));
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

    private static Map<String, PsiType> getSuperTypes(PsiClass psiClass) {
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
