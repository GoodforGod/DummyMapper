package io.goodforgod.dummymapper.mapper.impl;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.intellij.psi.PsiJavaFile;
import io.dummymaker.factory.impl.GenFactory;
import io.goodforgod.dummymapper.error.ParseException;
import io.goodforgod.dummymapper.mapper.IMapper;
import io.goodforgod.dummymapper.marker.RawMarker;
import io.goodforgod.dummymapper.service.ClassFactory;
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
public class JsonMapper extends AbstractJsonJacksonMapper implements IMapper {

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

            final Class<?> target = ClassFactory.build(filtered);
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
