package io.goodforgod.dummymapper;

import io.goodforgod.dummymapper.mapper.IMapper;
import io.goodforgod.dummymapper.mapper.impl.AvroJacksonMapper;
import io.goodforgod.dummymapper.ui.config.AvroJacksonConfig;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Entry-point for AVRO Schema Jackson Mapper
 *
 * @author GoodforGod
 * @since 17.11.2019
 */
public class AvroSchemaJacksonAction extends MapperAction<AvroJacksonConfig> {

    private final AvroJacksonMapper mapper = new AvroJacksonMapper();
    private final AvroJacksonConfig config = new AvroJacksonConfig();

    @NotNull
    @Override
    public IMapper<AvroJacksonConfig> getMapper() {
        return mapper;
    }

    @Nullable
    @Override
    protected AvroJacksonConfig getConfig() {
        return config;
    }

    @NotNull
    @Override
    protected String format() {
        return "AVRO Schema (Jackson)";
    }
}
