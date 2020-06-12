package io.goodforgod.dummymapper.ui.component;

import com.intellij.openapi.ui.ComboBox;
import io.goodforgod.dummymapper.ui.IComponent;
import io.goodforgod.dummymapper.ui.config.IConfig;

import javax.swing.*;
import java.util.Collection;

/**
 * {@link ComboBox} component
 *
 * @author Anton Kurako (GoodforGod)
 * @since 12.6.2020
 */
public class ComboBoxComponent implements IComponent {

    private final String name;
    private final String defaultValue;
    private final Collection<String> values;

    public ComboBoxComponent(String name, String defaultValue, Collection<String> values) {
        this.name = name;
        this.defaultValue = defaultValue;
        this.values = values;
    }

    @Override
    public JComponent build(IConfig config) {
        final ComboBox<String> comboBox = new ComboBox<>(values.toArray(new String[0]));

        comboBox.setSelectedItem(defaultValue);
        comboBox.setVisible(true);
        comboBox.setEnabled(true);
        comboBox.setName(name);

        config.set(name, defaultValue);
        comboBox.addActionListener(e -> {
            if (e.getSource() instanceof ComboBox) {
                final ComboBox<?> source = (ComboBox<?>) e.getSource();
                if (source.getSelectedItem() != null)
                    config.set(source.getName(), source.getSelectedItem());
            }
        });

        return comboBox;
    }
}
