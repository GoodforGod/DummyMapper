package io.goodforgod.dummymapper.ui.component;

import io.goodforgod.dummymapper.ui.IComponent;
import io.goodforgod.dummymapper.ui.config.IConfig;
import javax.swing.*;

/**
 * {@link JTextField} component
 *
 * @author Anton Kurako (GoodforGod)
 * @since 12.6.2020
 */
public class TextBoxComponent implements IComponent {

    private final String name;
    private final String defaultValue;

    public TextBoxComponent(String name, String defaultValue) {
        this.name = name;
        this.defaultValue = defaultValue;
    }

    @Override
    public JComponent build(IConfig config) {
        final JTextField field = new JTextField();
        field.setText(defaultValue);
        field.setVisible(true);
        field.setEnabled(true);
        field.setName(name);

        config.set(name, defaultValue);
        field.addActionListener(e -> {
            if (e.getSource() instanceof JTextField) {
                final JTextField source = (JTextField) e.getSource();
                config.set(source.getName(), source.getText());
            }
        });

        return field;
    }
}
