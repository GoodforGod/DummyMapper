package io.goodforgod.dummymapper;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;

import java.net.URL;
import java.net.URLClassLoader;


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
}
