package io.goodforgod.dummymapper.ui.config;


import com.github.victools.jsonschema.generator.SchemaVersion;
import io.goodforgod.dummymapper.ui.component.ComboBoxComponent;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;
import javax.swing.*;
import org.jetbrains.annotations.NotNull;


/**
 * @author Anton Kurako (GoodforGod)
 * @since 23.5.2020
 */
public class JsonSchemaConfig extends AbstractConfig {

    private static final String DRAFT_OPTION = "draft";

    @NotNull
    @Override
    public Collection<JComponent> getComponents() {
        return Collections.singletonList(
                new ComboBoxComponent(DRAFT_OPTION,
                        SchemaVersion.DRAFT_2019_09.name(),
                        Arrays.stream(SchemaVersion.values())
                                .map(Enum::name)
                                .collect(Collectors.toSet()))
                                        .build(this));
    }

    public SchemaVersion getSchemaVersion() {
        return SchemaVersion.valueOf(config.getOrDefault(DRAFT_OPTION, SchemaVersion.DRAFT_2019_09.name()));
    }
}
