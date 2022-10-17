package io.goodforgod.dummymapper;

import io.goodforgod.dummymapper.mapper.MarkerMapper;
import io.goodforgod.dummymapper.mapper.impl.JsonArrayMapper;
import io.goodforgod.dummymapper.ui.config.JsonArrayConfig;
import org.jetbrains.annotations.NotNull;

/**
 * Entry-point for {@link JsonArrayMapper}
 *
 * @author GoodforGod
 * @since 17.11.2019
 */
public class JsonArrayAction extends MapperAction<JsonArrayConfig> {

    private final JsonArrayMapper mapper = new JsonArrayMapper();
    private final JsonArrayConfig config = new JsonArrayConfig();

    @NotNull
    @Override
    public MarkerMapper<JsonArrayConfig> getMapper() {
        return mapper;
    }

    @Override
    protected JsonArrayConfig getConfig() {
        return config;
    }

    @NotNull
    @Override
    protected String format() {
        return "JSON Array";
    }
}
