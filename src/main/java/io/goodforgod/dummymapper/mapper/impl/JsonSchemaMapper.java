package io.goodforgod.dummymapper.mapper.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.victools.jsonschema.generator.*;
import com.intellij.psi.PsiJavaFile;
import io.goodforgod.dummymapper.error.ExternalException;
import io.goodforgod.dummymapper.filter.IFilter;
import io.goodforgod.dummymapper.filter.impl.EmptyMarkerFilter;
import io.goodforgod.dummymapper.mapper.IMapper;
import io.goodforgod.dummymapper.marker.Marker;
import io.goodforgod.dummymapper.marker.RawMarker;
import io.goodforgod.dummymapper.service.ClassFactory;
import io.goodforgod.dummymapper.ui.config.JsonSchemaConfig;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Optional;

/**
 * Maps instance of {@link PsiJavaFile} to {@link JsonNode} JSON Schema format
 *
 * @author Anton Kurako (GoodforGod)
 * @since 29.4.2020
 */
@SuppressWarnings("DuplicatedCode")
public class JsonSchemaMapper implements IMapper<JsonSchemaConfig> {

    private final IFilter emptyFilter = new EmptyMarkerFilter();

    @NotNull
    @Override
    public String map(@NotNull RawMarker marker, @Nullable JsonSchemaConfig config) {
        try {
            final RawMarker filtered = Optional.of(marker)
                    .map(emptyFilter::filter)
                    .get();

            if (filtered.isEmpty())
                return "";

            final Class<?> target = ClassFactory.build(filtered);

            final SchemaVersion version = (config == null) ? SchemaVersion.DRAFT_2019_09 : config.getSchemaVersion();
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
