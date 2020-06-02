package io.goodforgod.dummymapper.ui.options;

/**
 * Description in progress
 *
 * @author Anton Kurako (GoodforGod)
 * @since 2.6.2020
 */
public class TextBoxOptions {

    private final String name;
    private final String defaultValue;

    public TextBoxOptions(String name, String defaultValue) {
        this.name = name;
        this.defaultValue = defaultValue;
    }

    public String getName() {
        return name;
    }

    public String getDefaultValue() {
        return defaultValue;
    }
}
