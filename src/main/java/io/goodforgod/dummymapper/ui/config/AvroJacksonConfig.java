package io.goodforgod.dummymapper.ui.config;


import io.goodforgod.dummymapper.ui.component.CheckBoxComponent;
import java.util.Collection;
import java.util.Collections;
import javax.swing.*;
import org.jetbrains.annotations.NotNull;


/**
 * @author Anton Kurako (GoodforGod)
 * @since 23.5.2020
 */
public class AvroJacksonConfig extends AbstractConfig {

    private static final String REQUIRED_BY_DEFAULT_OPTION = "Required by Default";

    @NotNull
    @Override
    public Collection<JComponent> getComponents() {
        return Collections.singletonList(
                new CheckBoxComponent(REQUIRED_BY_DEFAULT_OPTION, false).build(this));
    }

    public boolean isRequiredByDefault() {
        return Boolean.parseBoolean(config.getOrDefault(REQUIRED_BY_DEFAULT_OPTION, "false"));
    }
}
