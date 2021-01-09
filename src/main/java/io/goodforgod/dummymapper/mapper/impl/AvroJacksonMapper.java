package io.goodforgod.dummymapper.mapper.impl;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.avro.AvroFactory;
import com.fasterxml.jackson.dataformat.avro.AvroSchema;
import com.fasterxml.jackson.dataformat.avro.schema.AvroSchemaGenerator;
import com.intellij.psi.PsiJavaFile;
import io.goodforgod.dummymapper.error.ExternalException;
import io.goodforgod.dummymapper.error.MapperException;
import io.goodforgod.dummymapper.filter.IFilter;
import io.goodforgod.dummymapper.filter.impl.AvroFilter;
import io.goodforgod.dummymapper.filter.impl.EmptyMarkerFilter;
import io.goodforgod.dummymapper.filter.impl.ExcludeSetterAnnotationFilter;
import io.goodforgod.dummymapper.filter.impl.JacksonPropertyFilter;
import io.goodforgod.dummymapper.mapper.IMapper;
import io.goodforgod.dummymapper.marker.RawMarker;
import io.goodforgod.dummymapper.service.ClassFactory;
import io.goodforgod.dummymapper.ui.config.AvroJacksonConfig;
import org.apache.avro.Schema;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

/**
 * Maps instance of {@link PsiJavaFile} to Jackson {@link AvroSchema} AVRO format
 *
 * {@link com.fasterxml.jackson.annotation.JsonProperty with required true) results in field being mandatory in AVRO, when its absent make type optional
 *
 * @author Anton Kurako (GoodforGod)
 * @since 29.4.2020
 */
@SuppressWarnings("DuplicatedCode")
public class AvroJacksonMapper implements IMapper<AvroJacksonConfig> {

    private final IFilter emptyFilter = new EmptyMarkerFilter();
    private final IFilter avroFilter = new AvroFilter();
    private final IFilter requiredFieldFilter = new JacksonPropertyFilter();
    private final IFilter annotationFilter = new ExcludeSetterAnnotationFilter();
    private final ObjectMapper mapper = new ObjectMapper(new AvroFactory());

    @NotNull
    @Override
    public String map(@NotNull RawMarker marker, AvroJacksonConfig config) {
        try {
            final RawMarker filtered = Optional.of(marker)
                    .map(avroFilter::filter)
                    .map(annotationFilter::filter)
                    .map(r -> config.isRequiredByDefault() ? requiredFieldFilter.filter(r) : r)
                    .map(emptyFilter::filter)
                    .orElseThrow(() -> new IllegalArgumentException("Not filter present!"));

            if (filtered.isEmpty())
                return "";

            final Class<?> target = ClassFactory.build(filtered);

            final AvroSchemaGenerator generator = new AvroSchemaGenerator();
            mapper.acceptJsonFormatVisitor(target, generator);
            final AvroSchema generatedSchema = generator.getGeneratedSchema();
            final Schema schema = generatedSchema.getAvroSchema();
            final String schemaAsJson = schema.toString(true);

            final String markerPackage = marker.getSourcePackage();
            return schemaAsJson.replaceAll("io\\.goodforgod\\.dummymapper\\.dummies_\\d+", markerPackage);
        } catch (JsonMappingException e) {
            if (e.getMessage().startsWith("\"Any\" type (usually for `java.lang.Object`)"))
                throw new MapperException(
                        "Java Class field with type 'java.lang.Object' can not be mapped to AVRO Schema (Jackson) by this mapper");

            throw new ExternalException(e.getMessage());
        }
    }

    @NotNull
    @Override
    public String map(@NotNull RawMarker marker) {
        return map(marker, null);
    }
}
