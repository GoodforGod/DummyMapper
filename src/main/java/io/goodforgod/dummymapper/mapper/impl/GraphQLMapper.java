package io.goodforgod.dummymapper.mapper.impl;

import com.intellij.psi.PsiJavaFile;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.SchemaPrinter;
import io.goodforgod.dummymapper.external.JacksonValueMapperCustomFactory;
import io.goodforgod.dummymapper.mapper.IMapper;
import io.goodforgod.dummymapper.marker.Marker;
import io.goodforgod.dummymapper.marker.RawMarker;
import io.goodforgod.dummymapper.service.ClassFactory;
import io.goodforgod.dummymapper.service.PsiJavaFileScanner;
import io.goodforgod.dummymapper.ui.config.IConfig;
import io.leangen.graphql.GraphQLSchemaGenerator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

/**
 * Description in progress
 *
 * @author Anton Kurako (GoodforGod)
 * @since 13.6.2020
 */
public class GraphQLMapper implements IMapper {

    @Override
    public @NotNull String map(@NotNull PsiJavaFile file, @Nullable IConfig config) {
        final RawMarker marker = new PsiJavaFileScanner().scan(file);
        if (marker.isEmpty())
            return "";

        final Map<String, Marker> structure = marker.getStructure();
        final Class<?> target = ClassFactory.build(structure);

        final GraphQLSchema schema = new GraphQLSchemaGenerator()
                .withValueMapperFactory(new JacksonValueMapperCustomFactory())
                .withBasePackages("io.goodforgod.dummymapper")
                .withOperationsFromType(target)
                .generate();

        final SchemaPrinter.Options options = SchemaPrinter.Options.defaultOptions().includeDirectives(false);
        return new SchemaPrinter(options).print(schema);
    }
}
