package io.goodforgod.dummymapper.ui.config;

import com.github.victools.jsonschema.generator.SchemaVersion;
import io.goodforgod.dummymapper.ui.options.CheckBoxOptions;
import io.goodforgod.dummymapper.ui.options.ComboBoxOptions;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;

/**
 * @author Anton Kurako (GoodforGod)
 * @since 23.5.2020
 */
public class AvroJacksonSchemaConfig implements IConfig {

    public static final String REQUIRED_BY_DEFAULT_OPTION = "Required By Default";
    public static final String USE_AVRO_ANNOTATIONS = "Use Avro Annotations";

    @Override
    public Collection<CheckBoxOptions> checkBoxes() {
        return Arrays.asList(
                new CheckBoxOptions(REQUIRED_BY_DEFAULT_OPTION, true),
                new CheckBoxOptions(USE_AVRO_ANNOTATIONS, false)
        );
    }
}
