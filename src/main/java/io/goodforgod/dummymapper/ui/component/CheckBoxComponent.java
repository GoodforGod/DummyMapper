package io.goodforgod.dummymapper.ui.component;

import io.goodforgod.dummymapper.ui.IComponent;
import io.goodforgod.dummymapper.ui.config.IConfig;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * Description in progress
 *
 * @author Anton Kurako (GoodforGod)
 * @since 12.6.2020
 */
public class CheckBoxComponent implements IComponent {

    private final String text;
    private final boolean isSelected;

    public CheckBoxComponent(@NotNull String text, boolean isSelected) {
        this.text = text;
        this.isSelected = isSelected;
    }

    @Override
    public JComponent build(IConfig config) {
        final JCheckBox checkBox = new JCheckBox(text);
        checkBox.setVisible(true);
        checkBox.setEnabled(true);
        checkBox.setSelected(isSelected);

        config.set(text, isSelected);
        checkBox.addActionListener(e -> {
            if (e.getSource() instanceof JCheckBox) {
                final JCheckBox source = (JCheckBox) e.getSource();
                config.set(source.getText(), source.isSelected());
            }
        });

        return checkBox;
    }
}
