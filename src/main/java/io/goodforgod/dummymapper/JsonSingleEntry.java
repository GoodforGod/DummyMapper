package io.goodforgod.dummymapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ex.ClipboardUtil;
import com.intellij.openapi.ide.CopyPasteManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.psi.PsiJavaFile;
import io.dummymaker.factory.impl.GenFactory;
import io.goodforgod.dummymapper.service.ClassFactory;
import io.goodforgod.dummymapper.service.GenFactoryBuilder;
import io.goodforgod.dummymapper.service.JavaFileScanner;
import io.goodforgod.dummymapper.util.IdeaUtils;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.Map;

/**
 * Entry-point fpr plugin
 *
 * @author GoodforGod
 * @since 17.11.2019
 */
public class JsonSingleEntry extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent event) {
        final Project currentProject = event.getProject();
        try {
            final PsiJavaFile file = IdeaUtils.getFileFromAction(event)
                    .orElseThrow(() -> new IllegalArgumentException("File is not Java File!"));

            final JavaFileScanner scanner = new JavaFileScanner();
            final Map<String, Object> scan = scanner.scan(file);

            final Class target = ClassFactory.build(scan);
            final GenFactory factory = GenFactoryBuilder.build(target, scan);
            final Object o = factory.build(target);

            final String json = new ObjectMapper().writeValueAsString(o);
            IdeaUtils.copyToClipboard(json);
        } catch (Exception e) {
            e.printStackTrace();
            Messages.showMessageDialog(currentProject, e.getMessage(), "Problem Occurred", Messages.getInformationIcon());
        }
    }
}
