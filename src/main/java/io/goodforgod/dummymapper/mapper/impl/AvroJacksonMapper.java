package io.goodforgod.dummymapper.mapper.impl;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.avro.AvroFactory;
import com.fasterxml.jackson.dataformat.avro.AvroSchema;
import com.fasterxml.jackson.dataformat.avro.schema.AvroSchemaGenerator;
import com.intellij.psi.PsiJavaFile;
import io.goodforgod.dummymapper.error.ClassBuildException;
import io.goodforgod.dummymapper.error.MapperException;
import io.goodforgod.dummymapper.error.ScanException;
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
 * @author Anton Kurako (GoodforGod)
 * @since 29.4.2020
 */
public class AvroJacksonMapper implements IMapper {

    private final ObjectMapper mapper = new ObjectMapper(new AvroFactory());

    @NotNull
    @Override
    public String map(@NotNull PsiJavaFile file) {
        try {
            final RawMarker marker = new PsiJavaFileScanner().scan(file);
            if (marker.isEmpty())
                return "";

            final Map<String, Marker> structure = marker.getStructure();
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
