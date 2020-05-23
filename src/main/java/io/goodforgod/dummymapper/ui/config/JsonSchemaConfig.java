package io.goodforgod.dummymapper.ui.config;

import com.github.victools.jsonschema.generator.SchemaVersion;
import io.goodforgod.dummymapper.ui.options.ComboBoxOptions;

import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

/**
 * @author Anton Kurako (GoodforGod)
 * @since 23.5.2020
 */
public class JsonSchemaConfig implements IConfig {

    public static final String DRAFT_OPTION = "draft";

    @Override
    public Collection<ComboBoxOptions> comboBoxes() {
        return Arrays.asList(
                new ComboBoxOptions(DRAFT_OPTION,
                        SchemaVersion.DRAFT_2019_09.name(),
                        Arrays.stream(SchemaVersion.values())
                                .map(Enum::name)
                                .collect(Collectors.toSet()))
        );
    }
}
