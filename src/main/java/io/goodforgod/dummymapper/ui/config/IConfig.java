package io.goodforgod.dummymapper.ui.config;

import io.goodforgod.dummymapper.ui.options.CheckBoxOptions;
import io.goodforgod.dummymapper.ui.options.ComboBoxOptions;

import java.util.Collection;
import java.util.Collections;

/**
 * @author Anton Kurako (GoodforGod)
 * @since 23.5.2020
 */
public interface IConfig {

    default Collection<CheckBoxOptions> checkBoxes() {
        return Collections.emptyList();
    }

    default Collection<ComboBoxOptions> comboBoxes() {
        return Collections.emptyList();
    }
}
