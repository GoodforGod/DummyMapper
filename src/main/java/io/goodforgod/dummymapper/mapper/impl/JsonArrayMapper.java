package io.goodforgod.dummymapper.mapper.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.intellij.psi.PsiJavaFile;
import io.dummymaker.factory.impl.GenFactory;
import io.goodforgod.dummymapper.error.ParseException;
import io.goodforgod.dummymapper.filter.IFilter;
import io.goodforgod.dummymapper.filter.impl.SupportedAnnotationFilter;
import io.goodforgod.dummymapper.mapper.IMapper;
import io.goodforgod.dummymapper.marker.Marker;
import io.goodforgod.dummymapper.marker.RawMarker;
import io.goodforgod.dummymapper.service.ClassFactory;
import io.goodforgod.dummymapper.service.GenFactoryProvider;
import io.goodforgod.dummymapper.service.PsiJavaFileScanner;
import io.goodforgod.dummymapper.ui.config.JsonArrayConfig;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

/**
 * Maps instance of {@link PsiJavaFile} to JSON format as example
 *
 * @author Anton Kurako (GoodforGod)
 * @since 28.4.2020
 */
@SuppressWarnings("DuplicatedCode")
public class JsonArrayMapper implements IMapper<JsonArrayConfig> {

    private final ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);

    @NotNull
    @Override
    public String map(@NotNull PsiJavaFile file) {
        return map(file, null);
    }

    @NotNull
    @Override
    public String map(@NotNull PsiJavaFile file, @Nullable JsonArrayConfig config) {
        try {
            final RawMarker marker = new PsiJavaFileScanner().scan(file);
            if (marker.isEmpty())
                return "";

            final Map<String, Marker> structure = marker.getStructure();
            final Class<?> target = ClassFactory.build(structure);

            final GenFactory factory = GenFactoryProvider.get(structure);

            final int amount = (config == null) ? 1 : config.getAmount();
            final List<?> list = factory.build(target, amount);

            return mapper.writeValueAsString(list);
        } catch (JsonProcessingException e) {
            throw new ParseException(e.getMessage(), e);
        }
    }
}
