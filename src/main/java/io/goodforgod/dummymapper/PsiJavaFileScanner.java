package io.goodforgod.dummymapper;

import com.intellij.lang.jvm.types.JvmType;
import com.intellij.psi.*;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static io.goodforgod.dummymapper.ClassUtils.*;

/**
 * ! NO DESCRIPTION !
 *
 * @author GoodforGod
 * @since 27.11.2019
 */
public class PsiJavaFileScanner {

    private final Map<String, Map> scanned = new HashMap<>();

    public Map<String, Object> scan(@Nullable PsiJavaFile file) {
        return scanJavaFile(file);
    }

    private Map<String, Object> scanJavaFile(@Nullable PsiJavaFile file) {
        if (file == null)
            return Collections.emptyMap();

        if (file.getClasses().length > 0) {
            final PsiClass target = file.getClasses()[0];
            final PsiClass superTarget = target.getSuperClass();
            if (superTarget != null && !isTypeSimple(superTarget.getQualifiedName())) {
                final Map<String, PsiType> types = getSuperTypes(target);
                final Map<String, Object> superScan = scanJavaClass(superTarget, types);
                final Map<String, Object> targetScan = scanJavaClass(target, Collections.emptyMap());
                superScan.putAll(targetScan);
                return superScan;
            } else {
                return scanJavaClass(target, Collections.emptyMap());
            }

        }

        return Collections.emptyMap();
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> scanJavaClass(PsiClass target, Map<String, PsiType> parentTypes) {
        final Map<String, Object> structure = new LinkedHashMap<>();
        final PsiField[] fields = target.getFields();
        scanned.put(getFullName(target), structure);

        for (PsiField field : fields) {
            if (isTypeEnum(field.getType())) {
                ((List) structure.computeIfAbsent(field.getType().getPresentableText(), k -> new ArrayList<String>()))
                        .add(field.getName());
            } else if (isFieldValid(field)) {
                if (isTypeSimple(field.getType())) {
                    structure.put(field.getName(), getTypeByName(field.getType().getCanonicalText()));
                } else if (parentTypes.containsKey(field.getType().getPresentableText())) {
                    final PsiType type = parentTypes.get(field.getType().getPresentableText());
                    if (type != null && isTypeSimple(type)) {
                        structure.put(field.getName(), getTypeByName(type.getCanonicalText()));
                    }
                } else {
                    getResolvedJavaFile(field.getType()).ifPresent(f -> {
                        final Map map = scanned.get(getFullName(f));
                        if (map == null) {
                            final Map<String, Object> scannedComplexField = scanJavaFile(f);
                            final Object values = scannedComplexField.get(field.getType().getPresentableText());
                            if (values instanceof Collection) {
                                structure.put(field.getName(), values);
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
        return Optional.ofNullable(psiClass.getSuperClassType())
                .filter(t -> psiClass.getSuperClass() != null)
                .map(t -> {
                    final Map<String, PsiType> types = new LinkedHashMap<>();
                    final Iterator<JvmType> iterator = t.typeArguments().iterator();
                    for (PsiTypeParameter parameter : psiClass.getSuperClass().getTypeParameters()) {
                        final JvmType a = iterator.next();
                        if (a instanceof PsiType) {
                            types.put(parameter.getName(), (PsiType) a);
                        }
                    }
                    return types;
                })
                .orElseGet(LinkedHashMap::new);
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
