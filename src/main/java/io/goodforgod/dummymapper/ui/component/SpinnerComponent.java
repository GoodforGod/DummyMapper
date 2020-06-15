package io.goodforgod.dummymapper.ui.component;

import io.goodforgod.dummymapper.ui.IComponent;
import io.goodforgod.dummymapper.ui.config.IConfig;

import javax.swing.*;

/**
 * {@link JSpinner} component
 *
 * @author Anton Kurako (GoodforGod)
 * @since 12.6.2020
 */
public class SpinnerComponent implements IComponent {

    private final String name;
    private final int max;

    public SpinnerComponent(String name, int max) {
        this.name = name;
        this.max = max;
    }

    @Override
    public JComponent build(IConfig config) {
        final JSpinner spinner = new JSpinner(new SpinnerNumberModel(1, 1, max, 1));
        spinner.setName(name);
        spinner.setVisible(true);
        spinner.setEnabled(true);

        config.set(name, 1);
        spinner.addChangeListener(e -> {
            if (e.getSource() instanceof JSpinner) {
                final JSpinner source = (JSpinner) e.getSource();
                source.validate();
                if (source.isValid())
                    config.set(source.getName(), source.getValue());
            }
        });

        return spinner;
    }
}
