package io.goodforgod.dummymapper.marker.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.victools.jsonschema.generator.*;
import com.intellij.psi.PsiJavaFile;
import io.goodforgod.dummymapper.error.ExternalException;
import io.goodforgod.dummymapper.marker.MarkerFilter;
import io.goodforgod.dummymapper.marker.MarkerMapper;
import io.goodforgod.dummymapper.marker.RawMarker;
import io.goodforgod.dummymapper.marker.filter.EmptyMarkerFilter;
import io.goodforgod.dummymapper.service.AssistClassFactory;
import io.goodforgod.dummymapper.ui.config.JsonSchemaConfig;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;

/**
 * Maps instance of {@link PsiJavaFile} to {@link JsonNode} JSON Schema format
 *
 * @author Anton Kurako (GoodforGod)
 * @since 29.4.2020
 */
@SuppressWarnings("DuplicatedCode")
public class JsonSchemaMapper implements MarkerMapper<JsonSchemaConfig> {

    private final MarkerFilter emptyFilter = new EmptyMarkerFilter();

    @NotNull
    @Override
    public String map(@NotNull RawMarker marker, JsonSchemaConfig config) {
        try {
            final RawMarker filtered = Optional.of(marker)
                    .map(emptyFilter::filter)
                    .orElseThrow(() -> new IllegalArgumentException("Not filter present!"));

            if (filtered.isEmpty())
                return "";

            final Class<?> target = AssistClassFactory.build(filtered);

            final SchemaVersion version = config.getSchemaVersion();
            final SchemaGeneratorConfig generatorConfig = new SchemaGeneratorConfigBuilder(version, OptionPreset.PLAIN_JSON)
                    .build();

            final SchemaGenerator generator = new SchemaGenerator(generatorConfig);

            final JsonNode schema = generator.generateSchema(target);
            return schema.toPrettyString();
        } catch (Exception e) {
            throw new ExternalException(e);
        }
    }
}
