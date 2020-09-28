package io.goodforgod.dummymapper.mapper.impl;

import graphql.schema.GraphQLSchema;
import graphql.schema.idl.SchemaPrinter;
import io.goodforgod.dummymapper.external.JacksonValueMapperCustomFactory;
import io.goodforgod.dummymapper.filter.IFilter;
import io.goodforgod.dummymapper.filter.impl.EmptyMarkerFilter;
import io.goodforgod.dummymapper.filter.impl.GraphQLNonNullFilter;
import io.goodforgod.dummymapper.filter.impl.GraphQLQueryFilter;
import io.goodforgod.dummymapper.mapper.IMapper;
import io.goodforgod.dummymapper.marker.RawMarker;
import io.goodforgod.dummymapper.service.ClassFactory;
import io.goodforgod.dummymapper.ui.config.GraphQLConfig;
import io.leangen.graphql.GraphQLSchemaGenerator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/**
 * @author Anton Kurako (GoodforGod)
 * @since 13.6.2020
 */
public class GraphQLMapper implements IMapper<GraphQLConfig> {

    private final IFilter emptyFilter = new EmptyMarkerFilter();
    private final GraphQLNonNullFilter nonNullFilter = new GraphQLNonNullFilter();
    private final GraphQLQueryFilter queryFilter = new GraphQLQueryFilter();

    @Override
    public @NotNull String map(@NotNull RawMarker marker, @Nullable GraphQLConfig config) {
        final RawMarker filtered = Optional.of(marker)
                .map(queryFilter::filter)
                .map(nonNullFilter::filter)
                .map(emptyFilter::filter)
                .orElseThrow(() -> new IllegalArgumentException("Not filter present!"));

        if (filtered.isEmpty())
            return "";

        final Class<?> target = ClassFactory.build(filtered);

        final GraphQLSchema schema = new GraphQLSchemaGenerator()
                .withValueMapperFactory(new JacksonValueMapperCustomFactory())
                .withBasePackages(marker.getSourcePackage())
                .withOperationsFromType(target)
                .generate();

        final SchemaPrinter.Options options = SchemaPrinter.Options.defaultOptions()
                .includeDirectives(false)
                .includeScalarTypes(true)
                .includeExtendedScalarTypes(true)
                .includeSchemaDefintion(true);

        return new SchemaPrinter(options).print(schema);
    }
}
