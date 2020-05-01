package io.goodforgod.dummymapper.mapper.impl;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.avro.AvroFactory;
import com.fasterxml.jackson.dataformat.avro.AvroSchema;
import com.fasterxml.jackson.dataformat.avro.schema.AvroSchemaGenerator;
import com.intellij.psi.PsiJavaFile;
import io.goodforgod.dummymapper.error.ClassBuildException;
import io.goodforgod.dummymapper.error.MapperException;
import io.goodforgod.dummymapper.error.ScanException;
import io.goodforgod.dummymapper.filter.IFilter;
import io.goodforgod.dummymapper.filter.impl.AvroFilter;
import io.goodforgod.dummymapper.mapper.IMapper;
import io.goodforgod.dummymapper.marker.Marker;
import io.goodforgod.dummymapper.marker.RawMarker;
import io.goodforgod.dummymapper.service.ClassFactory;
import io.goodforgod.dummymapper.service.PsiJavaFileScanner;
import org.apache.avro.Schema;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * Maps instance of {@link PsiJavaFile} to Jackson {@link AvroSchema} AVRO format
 *
 * {@link JsonProperty with required true) results in field being mandatory in AVRO, when its absent make type optional
 *
 * @author Anton Kurako (GoodforGod)
 * @since 29.4.2020
 */
@SuppressWarnings("DuplicatedCode")
public class AvroJacksonMapper implements IMapper {

    private final IFilter filter;
    private final ObjectMapper mapper;

    public AvroJacksonMapper() {
        this.filter = new AvroFilter();
        this.mapper = new ObjectMapper(new AvroFactory());
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_ABSENT);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_DEFAULT);
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

            final AvroSchemaGenerator generator = new AvroSchemaGenerator();
            mapper.acceptJsonFormatVisitor(target, generator);
            final AvroSchema generatedSchema = generator.getGeneratedSchema();
            final Schema schema = generatedSchema.getAvroSchema();
            return schema.toString(true);
        } catch (ScanException | ClassBuildException | JsonMappingException e) {
            throw new MapperException(e);
        }
    }
}
