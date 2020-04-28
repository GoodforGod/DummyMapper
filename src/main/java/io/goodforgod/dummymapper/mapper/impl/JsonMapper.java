package io.goodforgod.dummymapper.mapper.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.intellij.psi.PsiJavaFile;
import io.dummymaker.factory.impl.GenFactory;
import io.goodforgod.dummymapper.error.ClassBuildException;
import io.goodforgod.dummymapper.error.MapperException;
import io.goodforgod.dummymapper.error.ScanException;
import io.goodforgod.dummymapper.mapper.IMapper;
import io.goodforgod.dummymapper.marker.RawMarker;
import io.goodforgod.dummymapper.service.ClassFactory;
import io.goodforgod.dummymapper.service.GenFactoryBuilder;
import io.goodforgod.dummymapper.service.JavaFileScanner;
import org.jetbrains.annotations.NotNull;

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
            final RawMarker scan = new JavaFileScanner().scan(file);
            if (scan.isEmpty())
                return "";

            final Class target = ClassFactory.build(scan.getStructure());

            final GenFactory factory = GenFactoryBuilder.build(target, scan.getStructure());
            final Object o = factory.build(target);

            return mapper.writeValueAsString(factory.build(target));
        } catch (JsonProcessingException | ScanException | ClassBuildException e) {
            throw new MapperException(e);
        }
    }
}
