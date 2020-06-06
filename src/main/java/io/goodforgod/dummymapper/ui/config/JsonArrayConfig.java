package io.goodforgod.dummymapper.ui.config;

import com.github.victools.jsonschema.generator.SchemaVersion;
import io.goodforgod.dummymapper.ui.options.ComboBoxOptions;
import io.goodforgod.dummymapper.ui.options.TextBoxOptions;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;

/**
 * @author Anton Kurako (GoodforGod)
 * @since 23.5.2020
 */
public class JsonArrayConfig extends AbstractConfig {

    public static final String AMOUNT_OPTION = "amount";

    @Override
    public Collection<TextBoxOptions> textBoxes() {
        return Collections.singletonList(
                new TextBoxOptions(AMOUNT_OPTION, "1")
        );
    }
}
