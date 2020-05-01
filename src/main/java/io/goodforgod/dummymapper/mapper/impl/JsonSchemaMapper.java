package io.goodforgod.dummymapper.mapper.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.victools.jsonschema.generator.*;
import com.intellij.psi.PsiJavaFile;
import io.goodforgod.dummymapper.error.ClassBuildException;
import io.goodforgod.dummymapper.error.MapperException;
import io.goodforgod.dummymapper.error.ScanException;
import io.goodforgod.dummymapper.filter.IFilter;
import io.goodforgod.dummymapper.filter.impl.JacksonFilter;
import io.goodforgod.dummymapper.mapper.IMapper;
import io.goodforgod.dummymapper.marker.Marker;
import io.goodforgod.dummymapper.marker.RawMarker;
import io.goodforgod.dummymapper.service.ClassFactory;
import io.goodforgod.dummymapper.service.PsiJavaFileScanner;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * Maps instance of {@link PsiJavaFile} to {@link JsonNode} JSON Schema format
 *
 * @author Anton Kurako (GoodforGod)
 * @since 29.4.2020
 */
@SuppressWarnings("DuplicatedCode")
public class JsonSchemaMapper implements IMapper {

    private final IFilter filter;
    private final SchemaGenerator generator;

    public JsonSchemaMapper() {
        final SchemaGeneratorConfigBuilder configBuilder = new SchemaGeneratorConfigBuilder(SchemaVersion.DRAFT_2019_09,
                OptionPreset.PLAIN_JSON);
        final SchemaGeneratorConfig config = configBuilder.build();
        this.generator = new SchemaGenerator(config);
        this.filter = new JacksonFilter(true, false);
    }

    @NotNull
    @Override
    public String map(@NotNull PsiJavaFile file) {
        try {
            final RawMarker marker = new PsiJavaFileScanner().scan(file);
            final RawMarker filtered = this.filter.filter(marker);
            if (filtered.isEmpty())
                return "";

            final Map<String, Marker> structure = filtered.getStructure();
            final Class target = ClassFactory.build(structure);

            final JsonNode schema = generator.generateSchema(target);
            return schema.toPrettyString();
        } catch (ScanException | ClassBuildException e) {
            throw new MapperException(e);
        }
    }
}
