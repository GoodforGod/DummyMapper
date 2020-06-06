package io.goodforgod.dummymapper.ui;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.DialogWrapper;
import io.goodforgod.dummymapper.ui.config.AbstractConfig;
import io.goodforgod.dummymapper.ui.options.CheckBoxOptions;
import io.goodforgod.dummymapper.ui.options.ComboBoxOptions;
import io.goodforgod.dummymapper.ui.options.TextBoxOptions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.util.*;

/**
 * @author Anton Kurako (GoodforGod)
 * @since 7.5.2020
 */
public class ConfigDialog extends DialogWrapper {

    private static final int INITIAL_SIZE = 2;

    private final Collection<JCheckBox> checkBoxes = new ArrayList<>(INITIAL_SIZE);
    private final Collection<ComboBox<String>> comboBoxes = new ArrayList<>(INITIAL_SIZE);
    private final Collection<JTextField> textBoxes = new ArrayList<>(INITIAL_SIZE);

    public ConfigDialog(@Nullable Project project, String title) {
        super(project, true, IdeModalityType.PROJECT);
        setTitle(title);
    }

    public ConfigDialog build(AbstractConfig config) {
        config.checkBoxes().forEach(c -> addCheckBox(c, config.getCheckBoxSettings()));
        config.comboBoxes().forEach(c -> addComboBox(c, config.getComboBoxSettings()));
        config.textBoxes().forEach(c -> addTextBox(c, config.getTextBoxSettings()));
        init();
        return this;
    }

    private void addCheckBox(@NotNull CheckBoxOptions options, Map<String, Boolean> settings) {
        final JCheckBox box = getDialogCheckBox(options.getName(), options.isSelected(), settings);
        this.checkBoxes.add(box);
    }

    private void addComboBox(@NotNull ComboBoxOptions options, Map<String, String> settings) {
        final ComboBox<String> box = getDialogComboBox(options.getName(), options.getDefaultValue(), options.getValues(), settings);
        this.comboBoxes.add(box);
    }

    private void addTextBox(@NotNull TextBoxOptions options, Map<String, String> settings) {
        final JTextField box = getDialogTextBox(options.getName(), options.getDefaultValue(), settings);
        this.textBoxes.add(box);
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        final JPanel panel = new JPanel(new GridLayout(0, 1));
        panel.setPreferredSize(new Dimension(350, 250));

        comboBoxes.forEach(panel::add);
        checkBoxes.forEach(panel::add);
        textBoxes.forEach(panel::add);

        return panel;
    }

    @NotNull
    private JCheckBox getDialogCheckBox(@NotNull String text, boolean isSelected, Map<String, Boolean> settings) {
        final JCheckBox checkBox = new JCheckBox(text);
        checkBox.setVisible(true);
        checkBox.setEnabled(true);
        checkBox.setSelected(isSelected);

        settings.put(text, isSelected);
        checkBox.addActionListener(e -> {
            if (e.getSource() instanceof JCheckBox) {
                final JCheckBox source = (JCheckBox) e.getSource();
                settings.put(source.getText(), source.isSelected());
            }
        });

        return checkBox;
    }

    @NotNull
    private ComboBox<String> getDialogComboBox(@NotNull String text,
                                              @NotNull String selected,
                                              @NotNull Collection<String> values,
                                               Map<String, String> settings) {
        final ComboBox<String> comboBox = new ComboBox<>(values.toArray(new String[0]));
        comboBox.setSelectedItem(selected);
        comboBox.setVisible(true);
        comboBox.setEnabled(true);
        comboBox.setName(text);

        settings.put(text, selected);
        comboBox.addActionListener(e -> {
            if (e.getSource() instanceof ComboBox) {
                final ComboBox<?> source = (ComboBox<?>) e.getSource();
                settings.put(source.getName(), String.valueOf(source.getSelectedItem()));
            }
        });

        return comboBox;
    }

    @NotNull
    private JTextField getDialogTextBox(@NotNull String text,
                                              @NotNull String defaultValue,
                                        Map<String, String> settings) {
        final JTextField field = new JTextField();
        field.setText(defaultValue);
        field.setVisible(true);
        field.setEnabled(true);
        field.setName(text);

        settings.put(text, defaultValue);
        field.addActionListener(e -> {
            if (e.getSource() instanceof JTextField) {
                final JTextField source = (JTextField) e.getSource();
                settings.put(source.getName(), String.valueOf(source.getText()));
            }
        });

        return field;
    }

    @Nullable
    @Override
    protected String getDimensionServiceKey() {
        return "avro-schema-dialog";
    }
}
