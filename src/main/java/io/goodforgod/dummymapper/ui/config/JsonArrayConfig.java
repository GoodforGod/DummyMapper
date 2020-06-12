package io.goodforgod.dummymapper.ui.config;

import io.goodforgod.dummymapper.ui.component.TextBoxComponent;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.Collection;
import java.util.Collections;

/**
 * @author Anton Kurako (GoodforGod)
 * @since 23.5.2020
 */
public class JsonArrayConfig extends AbstractConfig {

    public static final String AMOUNT_OPTION = "amount";

    @NotNull
    @Override
    public Collection<JComponent> getComponents() {
        return Collections.singletonList(
                new TextBoxComponent(AMOUNT_OPTION, "1").build(this));
    }

    public int getAmount() {
        return Integer.parseInt(config.getOrDefault(AMOUNT_OPTION, "1"));
    }
}
