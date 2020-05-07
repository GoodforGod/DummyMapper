package io.goodforgod.dummymapper.mapper.impl;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.avro.AvroFactory;
import com.fasterxml.jackson.dataformat.avro.AvroSchema;
import com.fasterxml.jackson.dataformat.avro.schema.AvroSchemaGenerator;
import com.intellij.psi.PsiJavaFile;
import io.goodforgod.dummymapper.error.MapperException;
import io.goodforgod.dummymapper.error.ParseException;
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
 * {@link com.fasterxml.jackson.annotation.JsonProperty with required true) results in field being mandatory in AVRO, when its absent make type optional
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
    }

    // TODO fix class name with suffix
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
        } catch (JsonMappingException e) {
            if (e.getMessage().startsWith("\"Any\" type (usually for `java.lang.Object`)"))
                throw new MapperException("POJO field with type 'java.lang.Object' can not be mapped to AVRO Schema");

            throw new ParseException(e.getMessage(), e);
        }
    }
}
