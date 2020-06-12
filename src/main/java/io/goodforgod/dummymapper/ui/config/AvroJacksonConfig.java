package io.goodforgod.dummymapper.ui.config;

import io.goodforgod.dummymapper.ui.CheckBoxComponent;
import io.goodforgod.dummymapper.ui.options.CheckBoxOptions;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.Collection;
import java.util.Collections;

/**
 * @author Anton Kurako (GoodforGod)
 * @since 23.5.2020
 */
public class AvroJacksonConfig extends AbstractConfig{

    public static final String REQUIRED_BY_DEFAULT_OPTION = "Required By Default";

    @NotNull
    @Override
    public Collection<JComponent> getComponents() {
        return Collections.singletonList(
                new CheckBoxComponent(REQUIRED_BY_DEFAULT_OPTION, true).build(this)
        );
    }

    public boolean isRequiredByDefault() {
        return Boolean.parseBoolean(config.getOrDefault(REQUIRED_BY_DEFAULT_OPTION, "false"));
    }
}
