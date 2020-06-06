package io.goodforgod.dummymapper.mapper.impl;

import com.intellij.psi.PsiJavaFile;
import io.goodforgod.dummymapper.filter.IFilter;
import io.goodforgod.dummymapper.filter.impl.AvroFilter;
import io.goodforgod.dummymapper.mapper.IMapper;
import io.goodforgod.dummymapper.marker.Marker;
import io.goodforgod.dummymapper.marker.RawMarker;
import io.goodforgod.dummymapper.service.ClassFactory;
import io.goodforgod.dummymapper.service.PsiJavaFileScanner;
import io.goodforgod.dummymapper.ui.config.IConfig;
import org.apache.avro.Schema;
import org.apache.avro.reflect.ReflectData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

/**
 * Maps instance of {@link PsiJavaFile} to apache {@link Schema} AVRO format
 *
 * @author Anton Kurako (GoodforGod)
 * @since 29.4.2020
 */
@SuppressWarnings("DuplicatedCode")
public class AvroApacheMapper implements IMapper {

    private final IFilter filter = new AvroFilter();

    @NotNull
    @Override
    public String map(@NotNull PsiJavaFile file, @Nullable IConfig config) {
        return map(file);
    }

    // TODO fix class name with suffix
    @NotNull
    @Override
    public String map(@NotNull PsiJavaFile file) {
        final RawMarker marker = new PsiJavaFileScanner().scan(file);
        final RawMarker filtered = this.filter.filter(marker);
        if (filtered.isEmpty())
            return "";

        final Map<String, Marker> structure = filtered.getStructure();
        final Class target = ClassFactory.build(structure);

        final Schema schema = ReflectData.get().getSchema(target);
        return schema.toString(true);
    }
}
