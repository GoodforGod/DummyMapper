package io.goodforgod.dummymapper.ui.config;

import io.goodforgod.dummymapper.ui.component.SpinnerComponent;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.Collection;
import java.util.Collections;

/**
 * @author Anton Kurako (GoodforGod)
 * @since 23.5.2020
 */
public class JsonArrayConfig extends AbstractConfig {

    private static final String AMOUNT_OPTION = "amount";

    @NotNull
    @Override
    public Collection<JComponent> getComponents() {
        return Collections.singletonList(new SpinnerComponent(AMOUNT_OPTION, 1000).build(this));
    }

    public int getAmount() {
        return Integer.parseInt(config.getOrDefault(AMOUNT_OPTION, "1"));
    }
}
