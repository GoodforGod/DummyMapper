package io.goodforgod.dummymapper;

import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * ! NO DESCRIPTION !
 *
 * @author GoodforGod
 * @since 26.11.2019
 */
public class PsiFieldVisitor extends PsiRecursiveElementVisitor {

    private static final Set<String> SIMPLE_FIELDS;

    static {
        SIMPLE_FIELDS = Stream.of(
                Boolean.class,
                String.class,
                Character.class,
                Float.class,
                Double.class,
                Byte.class,
                Short.class,
                Integer.class,
                Long.class,
                byte.class,
                short.class,
                int.class,
                long.class,
                float.class,
                double.class,
                boolean.class,
                char.class,
                Object.class,
                BigInteger.class,
                BigDecimal.class,
                LocalTime.class,
                LocalDate.class,
                LocalDateTime.class,
                Date.class,
                java.sql.Date.class,
                Time.class,
                Timestamp.class
        ).map(Class::getSimpleName)
        .collect(Collectors.toSet());
    }

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
            } else if(field.getType().getResolveScope() != null) {
                final Project project = field.getType().getResolveScope().getProject();
                if (project != null) {
                    final PsiFile[] filesByName = FilenameIndex.getFilesByName(project,
                            field.getType().getCanonicalText() + ".class",
                            GlobalSearchScope.allScope(project));

                    final PsiFieldVisitor psiFieldVisitor = new PsiFieldVisitor();
                    filesByName[0].accept(psiFieldVisitor);
                    map.put(field.getName(), psiFieldVisitor.getMap());
                }
            }
        }

        super.visitElement(element);
    }

    private boolean isFieldSimple(PsiField field) {
        final String canonicalText = field.getType().getCanonicalText();
        return SIMPLE_FIELDS.contains(canonicalText);
    }
}
