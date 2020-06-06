package io.goodforgod.dummymapper.mapper.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.victools.jsonschema.generator.*;
import com.intellij.psi.PsiJavaFile;
import io.goodforgod.dummymapper.filter.IFilter;
import io.goodforgod.dummymapper.filter.impl.SupportedAnnotationFilter;
import io.goodforgod.dummymapper.mapper.IMapper;
import io.goodforgod.dummymapper.marker.Marker;
import io.goodforgod.dummymapper.marker.RawMarker;
import io.goodforgod.dummymapper.service.ClassFactory;
import io.goodforgod.dummymapper.service.PsiJavaFileScanner;
import io.goodforgod.dummymapper.ui.config.IConfig;
import io.goodforgod.dummymapper.ui.config.JsonSchemaConfig;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

/**
 * Maps instance of {@link PsiJavaFile} to {@link JsonNode} JSON Schema format
 *
 * @author Anton Kurako (GoodforGod)
 * @since 29.4.2020
 */
@SuppressWarnings("DuplicatedCode")
public class JsonSchemaMapper implements IMapper<JsonSchemaConfig> {

    private final IFilter filter;

    public JsonSchemaMapper() {
        this.filter = new SupportedAnnotationFilter();
    }

    @NotNull
    @Override
    public String map(@NotNull PsiJavaFile file, @Nullable JsonSchemaConfig config) {
        final RawMarker marker = new PsiJavaFileScanner().scan(file);
        if (marker.isEmpty())
            return "";

        final Map<String, Marker> structure = marker.getStructure();
        final Class target = ClassFactory.build(structure);

        final SchemaGeneratorConfig generatorConfig = new SchemaGeneratorConfigBuilder(SchemaVersion.DRAFT_2019_09, OptionPreset.JAVA_OBJECT)
                .build();

        final SchemaGenerator generator = new SchemaGenerator(generatorConfig);

        final JsonNode schema = generator.generateSchema(target);
        return schema.toPrettyString();
    }
}
