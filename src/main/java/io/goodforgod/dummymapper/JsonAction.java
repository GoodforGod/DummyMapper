package io.goodforgod.dummymapper;

import io.goodforgod.dummymapper.mapper.MarkerMapper;
import io.goodforgod.dummymapper.mapper.impl.JsonMapper;
import org.jetbrains.annotations.NotNull;

/**
 * Entry-point for {@link JsonMapper}
 *
 * @author GoodforGod
 * @since 17.11.2019
 */
public class JsonAction extends MapperAction {

    private final JsonMapper mapper = new JsonMapper();

    @NotNull
    @Override
    public MarkerMapper getMapper() {
        return mapper;
    }

    @NotNull
    @Override
    protected String format() {
        return "JSON";
    }
}
