package io.goodforgod.dummymapper.mapper.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.intellij.psi.PsiJavaFile;
import io.dummymaker.factory.impl.GenFactory;
import io.goodforgod.dummymapper.error.ClassBuildException;
import io.goodforgod.dummymapper.error.MapperException;
import io.goodforgod.dummymapper.error.ScanException;
import io.goodforgod.dummymapper.mapper.IMapper;
import io.goodforgod.dummymapper.marker.Marker;
import io.goodforgod.dummymapper.marker.RawMarker;
import io.goodforgod.dummymapper.service.ClassFactory;
import io.goodforgod.dummymapper.service.GenFactoryProvider;
import io.goodforgod.dummymapper.service.PsiJavaFileScanner;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * Maps instance of {@link PsiJavaFile} to JSON format
 *
 * @author Anton Kurako (GoodforGod)
 * @since 28.4.2020
 */
public class JsonMapper implements IMapper {

    private final ObjectMapper mapper = new ObjectMapper();

    @NotNull
    @Override
    public String map(@NotNull PsiJavaFile file) {
        try {
            final RawMarker scan = new PsiJavaFileScanner().scan(file);
            if (scan.isEmpty())
                return "";

            final Map<String, Marker> structure = scan.getStructure();
            final Class target = ClassFactory.build(structure);

            final GenFactory factory = GenFactoryProvider.get(structure);
            final Object instance = factory.build(target);

            return mapper.writeValueAsString(instance);
        } catch (JsonProcessingException | ScanException | ClassBuildException e) {
            throw new MapperException(e);
        }
    }
}
