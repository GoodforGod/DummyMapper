package io.goodforgod.dummymapper;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiJavaFile;
import javassist.ClassPool;
import javassist.CtClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Optional;

/**
 * ! NO DESCRIPTION !
 *
 * @author GoodforGod
 * @since 17.11.2019
 */
public class DummyJsonSingle extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent event) {
        final Editor editor = event.getData(CommonDataKeys.EDITOR);
        final PsiFile psiFile = event.getData(CommonDataKeys.PSI_FILE);
        try {
            final Project project = event.getProject();
            final PsiElement elementAt = psiFile.findElementAt(editor.getCaretModel().getOffset());
            final PsiDirectory directory = elementAt.getContainingFile().getContainingDirectory();

            final PsiJavaFileScanner javaFileScanner = new PsiJavaFileScanner();
            final Map<String, Object> scan = javaFileScanner.scan((PsiJavaFile) psiFile);

            final Optional<CtClass> build = ClassFactory.build(scan);

            final Class aClass = ClassPool.getDefault().toClass(build.get(), DummyJsonSingle.class.getClassLoader(), null);

            final String dirPath = directory.toString().replace("PsiDirectory:", "file:/");
            final String targetName = elementAt.getContainingFile().getVirtualFile().getName();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
