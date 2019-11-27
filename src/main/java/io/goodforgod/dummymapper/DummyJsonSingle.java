package io.goodforgod.dummymapper;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;


/**
 * ! NO DESCRIPTION !
 *
 * @author GoodforGod
 * @since 17.11.2019
 */
public class DummyJsonSingle extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent e) {
        final Editor editor = e.getData(CommonDataKeys.EDITOR);
        final PsiFile psiFile = e.getData(CommonDataKeys.PSI_FILE);
        final Project project = e.getProject();
        final PsiElement elementAt = psiFile.findElementAt(editor.getCaretModel().getOffset());
        try {
            final PsiDirectory directory = elementAt.getContainingFile().getContainingDirectory();
            PsiJavaFileScanner javaFileScanner = new PsiJavaFileScanner();
            Map<String, Object> scan = javaFileScanner.scan((PsiJavaFile) psiFile);
            final String dirPath = directory.toString().replace("PsiDirectory:", "file:/");
            final String targetName = elementAt.getContainingFile().getVirtualFile().getName();
            final String target = elementAt.getContainingFile().getVirtualFile().getUrl();
            final URL url = new URL(dirPath);
            final URLClassLoader loader = new URLClassLoader(new URL[]{url});
            Class<?> aClass = loader.loadClass(targetName);
            aClass.toString();
            //TODO dynamically find, compile, load class (and his parents)
        } catch (Exception ex) {
            return;
        }
    }

    private void scanParent(PsiFile psiFile) {
        PsiClassType type = ((PsiJavaFile) psiFile).getClasses()[0].getSuperTypes()[0];
        GlobalSearchScope scope = type.getResolveScope();
        Project p = scope.getProject();
        PsiFile[] filesByName = FilenameIndex.getFilesByName(p,
                type.getClassName() + ".class",
                GlobalSearchScope.allScope(p));

        PsiJavaFileScanner psiJavaFileScanner = new PsiJavaFileScanner();
        psiJavaFileScanner.scan((PsiJavaFile) filesByName[0]);
    }

    private Map<String, Object> getClassMap(PsiFile file) {
        final PsiClass[] classes = ((PsiJavaFile) file).getClasses();
        final Map<String, Object> map = new HashMap<>();
        for (PsiClass psiClass : classes) {
            for (PsiField field : psiClass.getAllFields()) {
                if(isFieldSimple(field)) {
                    map.put(field.getName(), field.getType().getCanonicalText());
                } else {
                    final VirtualFile subTypeFile = field.getType().getResolveScope().getProject().getProjectFile();

                }
            }
        }

        return Collections.emptyMap();
    }

    private boolean isFieldSimple(PsiField field) {
        return true;
    }
}
