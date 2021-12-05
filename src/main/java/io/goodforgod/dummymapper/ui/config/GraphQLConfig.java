package io.goodforgod.dummymapper.ui.config;

import io.goodforgod.dummymapper.ui.component.CheckBoxComponent;
import java.util.Arrays;
import java.util.Collection;
import javax.swing.*;
import org.jetbrains.annotations.NotNull;

/**
 * GraphQL Mapper config
 *
 * @author Anton Kurako (GoodforGod)
 * @since 14.6.2020
 */
public class GraphQLConfig extends AbstractConfig {

    private static final String QUERY_BY_DEFAULT = "Visible by Default";
    private static final String NON_NULL_BY_DEFAULT = "Required by Default";

    @Override
    public @NotNull Collection<JComponent> getComponents() {
        return Arrays.asList(
                new CheckBoxComponent(QUERY_BY_DEFAULT, true).build(this),
                new CheckBoxComponent(NON_NULL_BY_DEFAULT, false).build(this));
    }

    public boolean isQueryByDefault() {
        return Boolean.parseBoolean(config.getOrDefault(QUERY_BY_DEFAULT, "false"));
    }

    public boolean isQueryNonNullByDefault() {
        return Boolean.parseBoolean(config.getOrDefault(NON_NULL_BY_DEFAULT, "false"));
    }
}
