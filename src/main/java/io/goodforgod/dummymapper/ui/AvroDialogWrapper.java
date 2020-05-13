package io.goodforgod.dummymapper.ui;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

/**
 * @author Anton Kurako (GoodforGod)
 * @since 7.5.2020
 */
public class AvroDialogWrapper extends DialogWrapper {

    public AvroDialogWrapper(@Nullable Project project) {
        super(project, true, IdeModalityType.PROJECT);
        init();
        setTitle("AVRO Schema Options");
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {

        final JPanel dialogPanel = new JPanel(new BorderLayout());
        dialogPanel.setPreferredSize(new Dimension(250, 250));

        final JLabel label = new JLabel("AVRO Schema Options");
        label.setPreferredSize(new Dimension(100, 100));
        dialogPanel.add(label, BorderLayout.CENTER);

        final JCheckBox allRequiredDefaultCheckBox = new JCheckBox("All Required By Default");
        dialogPanel.add(allRequiredDefaultCheckBox);

        final JCheckBox useJacksonAnnotations = new JCheckBox("All Required By Default");
        useJacksonAnnotations.add(allRequiredDefaultCheckBox);

        return dialogPanel;
    }

    @Nullable
    @Override
    protected String getDimensionServiceKey() {
        return "avro-schema-dialog";
    }
}
