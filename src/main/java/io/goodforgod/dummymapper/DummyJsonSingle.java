package io.goodforgod.dummymapper;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.psi.PsiJavaFile;
import io.dummymaker.factory.impl.GenFactory;
import io.goodforgod.dummymapper.service.ClassFactory;
import io.goodforgod.dummymapper.service.GenFactoryBuilder;
import io.goodforgod.dummymapper.service.JavaFileScanner;
import io.goodforgod.dummymapper.util.IdeaUtils;

import java.util.Map;

/**
 * Entry-point fpr plugin
 *
 * @author GoodforGod
 * @since 17.11.2019
 */
public class DummyJsonSingle extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent event) {
        final Project currentProject = event.getProject();
        try {
            final PsiJavaFile file = IdeaUtils.getFileFromAction(event)
                    .orElseThrow(() -> new IllegalArgumentException("File is not Java File!"));

            final JavaFileScanner scanner = new JavaFileScanner();
            final Map<String, Object> scan = scanner.scan(file);

            final Class target = ClassFactory.build(scan)
                    .orElseThrow(() -> new IllegalArgumentException("Could not construct Java File from scanned data!"));

            final GenFactory factory = GenFactoryBuilder.build(target, scan);
            final Object o = factory.build(target);
            o.toString();
        } catch (Exception e) {
            e.printStackTrace();
            Messages.showMessageDialog(currentProject, e.getMessage(), "Problem Occurred", Messages.getInformationIcon());
        }
    }
}
