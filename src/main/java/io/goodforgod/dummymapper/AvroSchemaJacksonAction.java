package io.goodforgod.dummymapper;

import io.goodforgod.dummymapper.marker.MarkerMapper;
import io.goodforgod.dummymapper.marker.mapper.AvroJacksonMapper;
import io.goodforgod.dummymapper.ui.config.AvroJacksonConfig;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Entry-point for {@link AvroJacksonMapper}
 *
 * @author GoodforGod
 * @since 17.11.2019
 */
public class AvroSchemaJacksonAction extends MapperAction<AvroJacksonConfig> {

    private final AvroJacksonMapper mapper = new AvroJacksonMapper();
    private final AvroJacksonConfig config = new AvroJacksonConfig();

    @NotNull
    @Override
    public MarkerMapper<AvroJacksonConfig> getMapper() {
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
