package io.goodforgod.dummymapper.ui;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Anton Kurako (GoodforGod)
 * @since 7.5.2020
 */
public class AvroDialogWrapper extends DialogWrapper {

    private final Map<String, Boolean> checkBoxMap = new HashMap<>(2);

    public AvroDialogWrapper(@Nullable Project project) {
        super(project, true, IdeModalityType.PROJECT);
        init();
        setTitle("AVRO Schema Options");
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        final JPanel panel = new JPanel(new GridLayout());
        panel.setPreferredSize(new Dimension(350, 250));

        final JCheckBox allRequiredDefaultCheckBox = getCheckBox("Required by default", true);
        final JCheckBox useJacksonAnnotations = getCheckBox("Use Jackson Annotations", true);

        panel.add(allRequiredDefaultCheckBox);
        panel.add(useJacksonAnnotations);

        return panel;
    }

    @NotNull
    private JCheckBox getCheckBox(@NotNull String text,
                                  boolean isSelected) {
        final JCheckBox checkBox = new JCheckBox(text);
        checkBox.setVisible(true);
        checkBox.setEnabled(true);
        checkBox.setSelected(isSelected);

        checkBoxMap.put(text, isSelected);
        checkBox.addActionListener(e -> {
            if (e.getSource() instanceof JCheckBox) {
                final JCheckBox source = (JCheckBox) e.getSource();
                checkBoxMap.put(source.getText(), source.isSelected());
            }
        });

        return checkBox;
    }

    public Map<String, Boolean> getCheckBoxMap() {
        return checkBoxMap;
    }

    @Nullable
    @Override
    protected String getDimensionServiceKey() {
        return "avro-schema-dialog";
    }
}
