package io.goodforgod.dummymapper.mapper.impl;

import com.intellij.psi.PsiJavaFile;
import io.goodforgod.dummymapper.filter.IFilter;
import io.goodforgod.dummymapper.filter.impl.AvroFilter;
import io.goodforgod.dummymapper.filter.impl.EmptyMarkerFilter;
import io.goodforgod.dummymapper.filter.impl.ExcludeSetterAnnotationFilter;
import io.goodforgod.dummymapper.mapper.IMapper;
import io.goodforgod.dummymapper.marker.RawMarker;
import io.goodforgod.dummymapper.service.ClassFactory;
import io.goodforgod.dummymapper.ui.config.IConfig;
import org.apache.avro.Schema;
import org.apache.avro.reflect.ReflectData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/**
 * Maps instance of {@link PsiJavaFile} to apache {@link Schema} AVRO format
 *
 * @author Anton Kurako (GoodforGod)
 * @since 29.4.2020
 */
@SuppressWarnings("DuplicatedCode")
public class AvroApacheMapper implements IMapper {

    private final IFilter emptyFilter = new EmptyMarkerFilter();
    private final IFilter avroFilter = new AvroFilter();
    private final IFilter annotationFilter = new ExcludeSetterAnnotationFilter();

    @NotNull
    @Override
    public String map(@NotNull RawMarker marker, @Nullable IConfig config) {
        return map(marker);
    }

    @NotNull
    @Override
    public String map(@NotNull RawMarker marker) {
        final RawMarker filtered = Optional.of(marker)
                .map(avroFilter::filter)
                .map(annotationFilter::filter)
                .map(emptyFilter::filter)
                .orElse(RawMarker.EMPTY);

        if (filtered.isEmpty())
            return "";

        final Class<?> target = ClassFactory.build(filtered);
        final Schema schema = ReflectData.get().getSchema(target);

        final String schemaAsJson = schema.toString(true);
        return schemaAsJson.replaceAll("io\\.goodforgod\\.dummymapper\\.dummies_\\d+", "io.goodforgod.dummymapper");
    }
}
