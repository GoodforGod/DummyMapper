package io.goodforgod.dummymapper.ui;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.DialogWrapper;
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
public abstract class ConfigDialog extends DialogWrapper {

    private static final int INITIAL_SIZE = 2;

    private final Map<String, Boolean> checkBoxMap = new HashMap<>(INITIAL_SIZE);
    private final Map<String, String> comboBoxMap = new HashMap<>(INITIAL_SIZE);
    private final Map<String, String> textBoxMap = new HashMap<>(INITIAL_SIZE);

    private final Collection<JCheckBox> checkBoxes = new ArrayList<>(INITIAL_SIZE);
    private final Collection<ComboBox<String>> comboBoxes = new ArrayList<>(INITIAL_SIZE);
    private final Collection<JTextField> textBoxes = new ArrayList<>(INITIAL_SIZE);

    public ConfigDialog(@Nullable Project project) {
        this(project, "Options");
    }

    public ConfigDialog(@Nullable Project project, String title) {
        super(project, true, IdeModalityType.PROJECT);
        setTitle(title);
    }

    public ConfigDialog build() {
        init();
        return this;
    }

    public ConfigDialog addCheckBox(@NotNull CheckBoxOptions options) {
        final JCheckBox box = getDialogCheckBox(options.getName(), options.isSelected());
        this.checkBoxes.add(box);
        return this;
    }

    public ConfigDialog addComboBox(@NotNull ComboBoxOptions options) {
        final ComboBox<String> box = getDialogComboBox(options.getName(), options.getDefaultValue(), options.getValues());
        this.comboBoxes.add(box);
        return this;
    }

    public ConfigDialog addTextBox(@NotNull TextBoxOptions options) {
        final JTextField box = getDialogTextBox(options.getName(), options.getDefaultValue());
        this.textBoxes.add(box);
        return this;
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        final JPanel panel = new JPanel(new GridLayout(0, 1));
        panel.setPreferredSize(new Dimension(350, 250));

        comboBoxes.forEach(panel::add);
        checkBoxes.forEach(panel::add);

        return panel;
    }

    @NotNull
    private JCheckBox getDialogCheckBox(@NotNull String text, boolean isSelected) {
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

    @NotNull
    private ComboBox<String> getDialogComboBox(@NotNull String text,
                                              @NotNull String selected,
                                              @NotNull Collection<String> values) {
        final ComboBox<String> comboBox = new ComboBox<>(values.toArray(new String[0]));
        comboBox.setSelectedItem(selected);
        comboBox.setVisible(true);
        comboBox.setEnabled(true);
        comboBox.setName(text);

        comboBoxMap.put(text, selected);
        comboBox.addActionListener(e -> {
            if (e.getSource() instanceof ComboBox) {
                final ComboBox<?> source = (ComboBox<?>) e.getSource();
                comboBoxMap.put(source.getName(), String.valueOf(source.getSelectedItem()));
            }
        });

        return comboBox;
    }

    @NotNull
    private JTextField getDialogTextBox(@NotNull String text,
                                              @NotNull String defaultValue) {
        final JTextField field = new JTextField();
        field.setText(defaultValue);
        field.setVisible(true);
        field.setEnabled(true);
        field.setName(text);

        textBoxMap.put(text, defaultValue);
        field.addActionListener(e -> {
            if (e.getSource() instanceof JTextField) {
                final JTextField source = (JTextField) e.getSource();
                textBoxMap.put(source.getName(), String.valueOf(source.getText()));
            }
        });

        return field;
    }

    public Map<String, Boolean> getCheckBoxMap() {
        return checkBoxMap;
    }

    public Map<String, String> getComboBoxMap() {
        return comboBoxMap;
    }

    public Map<String, String> getTextBoxMap() {
        return textBoxMap;
    }

    @Nullable
    @Override
    protected String getDimensionServiceKey() {
        return "avro-schema-dialog";
    }
}
