package io.goodforgod.dummymapper.mapper.impl;

import com.intellij.psi.PsiJavaFile;
import io.goodforgod.dummymapper.error.ClassBuildException;
import io.goodforgod.dummymapper.error.MapperException;
import io.goodforgod.dummymapper.error.ScanException;
import io.goodforgod.dummymapper.mapper.IMapper;
import io.goodforgod.dummymapper.marker.Marker;
import io.goodforgod.dummymapper.marker.RawMarker;
import io.goodforgod.dummymapper.service.ClassFactory;
import io.goodforgod.dummymapper.service.PsiJavaFileScanner;
import org.apache.avro.Schema;
import org.apache.avro.reflect.ReflectData;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * Maps instance of {@link PsiJavaFile} to apache AVRO format
 *
 * @author Anton Kurako (GoodforGod)
 * @since 29.4.2020
 */
public class AvroApacheMapper implements IMapper {

    @NotNull
    @Override
    public String map(@NotNull PsiJavaFile file) {
        try {
            final RawMarker scan = new PsiJavaFileScanner().scan(file);
            if (scan.isEmpty())
                return "";

            final Map<String, Marker> structure = scan.getStructure();
            final Class target = ClassFactory.build(structure);

            final Schema schema = ReflectData.get().getSchema(target);
            return schema.toString(true);
        } catch (ScanException | ClassBuildException e) {
            throw new MapperException(e);
        }
    }
}
