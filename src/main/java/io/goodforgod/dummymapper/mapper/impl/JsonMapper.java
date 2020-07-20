package io.goodforgod.dummymapper.mapper.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.intellij.psi.PsiJavaFile;
import io.dummymaker.factory.impl.GenFactory;
import io.goodforgod.dummymapper.error.ParseException;
import io.goodforgod.dummymapper.filter.IFilter;
import io.goodforgod.dummymapper.filter.impl.ExcludeSetterAnnotationFilter;
import io.goodforgod.dummymapper.mapper.IMapper;
import io.goodforgod.dummymapper.marker.Marker;
import io.goodforgod.dummymapper.marker.RawMarker;
import io.goodforgod.dummymapper.service.ClassFactory;
import io.goodforgod.dummymapper.service.GenFactoryProvider;
import io.goodforgod.dummymapper.service.PsiJavaFileScanner;
import io.goodforgod.dummymapper.ui.config.IConfig;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

/**
 * Maps instance of {@link PsiJavaFile} to JSON format as example
 *
 * @author Anton Kurako (GoodforGod)
 * @since 28.4.2020
 */
@SuppressWarnings("DuplicatedCode")
public class JsonMapper implements IMapper {

    private final ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
    private final IFilter annotationFilter = new ExcludeSetterAnnotationFilter();

    @NotNull
    @Override
    public String map(@NotNull RawMarker marker) {
        try {
            if (marker.isEmpty())
                return "";

            final RawMarker filtered = annotationFilter.filter(marker);
            final Map<String, Marker> structure = filtered.getStructure();
            final Class<?> target = ClassFactory.build(structure);

            final GenFactory factory = GenFactoryProvider.get(structure);
            final Object instance = factory.build(target);

            return mapper.writeValueAsString(instance);
        } catch (JsonProcessingException e) {
            throw new ParseException(e.getMessage(), e);
        }
    }

    @NotNull
    @Override
    public String map(@NotNull RawMarker marker, @Nullable IConfig config) {
        return map(marker);
    }
}
