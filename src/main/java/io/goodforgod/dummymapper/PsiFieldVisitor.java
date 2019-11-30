package io.goodforgod.dummymapper;

import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;

import java.util.*;

/**
 * ! NO DESCRIPTION !
 *
 * @author GoodforGod
 * @since 26.11.2019
 */
public class PsiFieldVisitor extends PsiRecursiveElementVisitor {

    private final Map<String, Object> map = new HashMap<>();

    public Map<String, Object> getMap() {
        return map;
    }

    @Override
    public void visitFile(PsiFile file) {
        super.visitFile(file);
    }

    @Override
    public void visitElement(PsiElement element) {
        if (element instanceof PsiField) {
            final PsiField field = (PsiField) element;
            if (isFieldSimple(field)) {
                map.put(field.getName(), field.getType().getCanonicalText());
            } else {
                Project project = field.getType().getResolveScope().getProject();
                if (project != null) {
                    PsiFile[] filesByName = FilenameIndex.getFilesByName(project,
                            field.getType().getCanonicalText() + ".class",
                            GlobalSearchScope.allScope(project));

                    PsiFieldVisitor psiFieldVisitor1 = new PsiFieldVisitor();
                    PsiField[] fields = ((PsiJavaFile) filesByName[0]).getClasses()[0].getAllFields();

                    if (filesByName.length > 0) {
                        PsiFieldVisitor psiFieldVisitor = new PsiFieldVisitor();
                        filesByName[0].accept(psiFieldVisitor);
                        map.put(field.getName(), psiFieldVisitor.getMap());
                    }
                }
            }
        }

        super.visitElement(element);
    }

    private boolean isFieldSimple(PsiField field) {
        final String canonicalText = field.getType().getCanonicalText();
        List<String> strings = Arrays.asList(
                "String",
                "Integer",
                "Byte",
                "Short",
                "Integer",
                "Long",
                "Float",
                "Double",
                "Boolean",
                "Character",
                "byte",
                "int",
                "long",
                "float",
                "double",
                "boolean",
                "char",
                "Object",
                "BigInteger",
                "BigNumber");

        HashSet<String> set = new HashSet<>(strings);
        return set.contains(canonicalText);
    }
}
