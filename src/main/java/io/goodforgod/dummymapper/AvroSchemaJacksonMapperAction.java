package io.goodforgod.dummymapper;

import io.goodforgod.dummymapper.mapper.IMapper;
import io.goodforgod.dummymapper.mapper.impl.AvroJacksonMapper;
import org.jetbrains.annotations.NotNull;

/**
 * Entry-point for AVRO Schema Jackson Mapper
 *
 * @author GoodforGod
 * @since 17.11.2019
 */
public class AvroSchemaJacksonMapperAction extends MapperAction {

    private final IMapper mapper = new AvroJacksonMapper();

    @NotNull
    @Override
    public IMapper getMapper() {
        return mapper;
    }

    @NotNull
    @Override
    protected String format() {
        return "AVRO Schema";
    }
}
