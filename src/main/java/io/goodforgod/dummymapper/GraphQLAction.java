package io.goodforgod.dummymapper;

import io.goodforgod.dummymapper.marker.MarkerMapper;
import io.goodforgod.dummymapper.marker.mapper.GraphQLMapper;
import io.goodforgod.dummymapper.ui.config.GraphQLConfig;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Entry-point for {@link GraphQLMapper}
 *
 * @author GoodforGod
 * @since 17.11.2019
 */
public class GraphQLAction extends MapperAction<GraphQLConfig> {

    private final GraphQLMapper mapper = new GraphQLMapper();
    private final GraphQLConfig config = new GraphQLConfig();

    @NotNull
    @Override
    public MarkerMapper<GraphQLConfig> getMapper() {
        return mapper;
    }

    @Nullable
    @Override
    protected GraphQLConfig getConfig() {
        return config;
    }

    @NotNull
    @Override
    protected String format() {
        return "GraphQL";
    }
}
