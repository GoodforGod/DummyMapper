package io.goodforgod.dummymapper.ui.options;

import java.util.Collection;

/**
 * @author Anton Kurako (GoodforGod)
 * @since 23.5.2020
 */
public class ComboBoxOptions {

    private final String name;
    private final String defaultValue;
    private final Collection<String> values;

    public ComboBoxOptions(String name, String defaultValue, Collection<String> values) {
        this.name = name;
        this.defaultValue = defaultValue;
        this.values = values;
    }

    public String getName() {
        return name;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public Collection<String> getValues() {
        return values;
    }
}
