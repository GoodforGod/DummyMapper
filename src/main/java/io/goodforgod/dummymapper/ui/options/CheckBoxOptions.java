package io.goodforgod.dummymapper.ui.options;

/**
 * Description in progress
 *
 * @author Anton Kurako (GoodforGod)
 * @since 23.5.2020
 */
public class CheckBoxOptions {

    private final String name;
    private final boolean selected;

    public CheckBoxOptions(String name, boolean selected) {
        this.name = name;
        this.selected = selected;
    }

    public String getName() {
        return name;
    }

    public boolean isSelected() {
        return selected;
    }
}
