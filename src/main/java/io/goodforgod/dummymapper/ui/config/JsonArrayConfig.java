package io.goodforgod.dummymapper.ui.config;

import io.goodforgod.dummymapper.ui.component.SpinnerComponent;
import java.util.Collection;
import java.util.Collections;
import javax.swing.*;
import org.jetbrains.annotations.NotNull;

/**
 * @author Anton Kurako (GoodforGod)
 * @since 23.5.2020
 */
public class JsonArrayConfig extends AbstractConfig {

    private static final int MAX = 1000;
    private static final String AMOUNT_OPTION = "amount";

    @NotNull
    @Override
    public Collection<JComponent> getComponents() {
        return Collections.singletonList(new SpinnerComponent(AMOUNT_OPTION, MAX).build(this));
    }

    public int getAmount() {
        return Integer.parseInt(config.getOrDefault(AMOUNT_OPTION, "10"));
    }
}
