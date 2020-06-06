package io.goodforgod.dummymapper.ui.config;

import io.goodforgod.dummymapper.ui.options.CheckBoxOptions;
import io.goodforgod.dummymapper.ui.options.ComboBoxOptions;
import io.goodforgod.dummymapper.ui.options.TextBoxOptions;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Description in progress
 *
 * @author Anton Kurako (GoodforGod)
 * @since 6.6.2020
 */
public class AbstractConfig implements IConfig {

    private static final int INITIAL_SIZE = 2;

    private final Map<String, Boolean> checkBoxMap = new HashMap<>(INITIAL_SIZE);
    private final Map<String, String> comboBoxMap = new HashMap<>(INITIAL_SIZE);
    private final Map<String, String> textBoxMap = new HashMap<>(INITIAL_SIZE);

    public Map<String, Boolean> getCheckBoxSettings() {
        return checkBoxMap;
    }

    public Map<String, String> getComboBoxSettings() {
        return comboBoxMap;
    }

    public Map<String, String> getTextBoxSettings() {
        return textBoxMap;
    }
}
