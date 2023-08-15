package io.goodforgod.dummymapper.marker.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.intellij.psi.PsiJavaFile;
import io.goodforgod.dummymaker.GenFactory;
import io.goodforgod.dummymapper.error.ParseException;
import io.goodforgod.dummymapper.marker.MarkerFilter;
import io.goodforgod.dummymapper.marker.MarkerMapper;
import io.goodforgod.dummymapper.marker.RawMarker;
import io.goodforgod.dummymapper.marker.filter.EmptyMarkerFilter;
import io.goodforgod.dummymapper.marker.filter.ExcludeSetterAnnotationFilter;
import io.goodforgod.dummymapper.marker.filter.GenEnumAnnotationFilter;
import io.goodforgod.dummymapper.service.AssistClassFactory;
import io.goodforgod.dummymapper.service.GenFactoryProvider;
import io.goodforgod.dummymapper.ui.config.IConfig;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Maps instance of {@link PsiJavaFile} to JSON format as example
 *
 * @author Anton Kurako (GoodforGod)
 * @since 28.4.2020
 */
@SuppressWarnings({ "DuplicatedCode", "rawtypes" })
public class JsonMapper implements MarkerMapper {

    private final MarkerFilter emptyFilter = new GenEnumAnnotationFilter();
    private final MarkerFilter annotationFilter = new ExcludeSetterAnnotationFilter();
    private final MarkerFilter annotationEnumFilter = new EmptyMarkerFilter();

    private final ObjectMapper mapper = ObjectMapperUtils.getConfigured();

    @NotNull
    @Override
    public String map(@NotNull RawMarker marker) {
        try {
            final RawMarker filtered = Optional.of(marker)
                    .map(annotationFilter::filter)
                    .map(emptyFilter::filter)
                    .map(annotationEnumFilter::filter)
                    .orElseThrow(() -> new IllegalArgumentException("Marker is not present after filter!"));

            if (filtered.isEmpty())
                return "";

            final Class<?> target = AssistClassFactory.build(filtered);
            final GenFactory factory = GenFactoryProvider.get(filtered);

            final Object instance = factory.build(target);

            return mapper.writeValueAsString(instance);
        } catch (JsonProcessingException e) {
            throw new ParseException(e.getMessage());
        }
    }

    @NotNull
    @Override
    public String map(@NotNull RawMarker marker, @Nullable IConfig config) {
        return map(marker);
    }
}
