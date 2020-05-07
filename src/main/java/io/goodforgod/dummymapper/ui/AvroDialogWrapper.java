package io.goodforgod.dummymapper.ui;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import org.jetbrains.annotations.NotNull;
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
        final JCheckBox allRequiredDefaultCheckBox = new JCheckBox();
        final JPanel dialogPanel = new JPanel(new BorderLayout());
        final JLabel label = new JLabel("AVRO Schema Options");

        label.setPreferredSize(new Dimension(100, 100));
        dialogPanel.add(label, BorderLayout.CENTER);

        return dialogPanel;
    }

    @Nullable
    @Override
    protected String getDimensionServiceKey() {
        return "avro-schema-dialog";
    }
}
