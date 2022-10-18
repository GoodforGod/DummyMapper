package io.goodforgod.dummymapper.marker.mapper;

import com.intellij.psi.PsiJavaFile;
import io.goodforgod.dummymapper.marker.MarkerFilter;
import io.goodforgod.dummymapper.marker.MarkerMapper;
import io.goodforgod.dummymapper.marker.RawMarker;
import io.goodforgod.dummymapper.marker.filter.AvroFilter;
import io.goodforgod.dummymapper.marker.filter.EmptyMarkerFilter;
import io.goodforgod.dummymapper.marker.filter.ExcludeSetterAnnotationFilter;
import io.goodforgod.dummymapper.service.AssistClassFactory;
import io.goodforgod.dummymapper.ui.config.IConfig;
import java.util.Optional;
import org.apache.avro.Schema;
import org.apache.avro.reflect.ReflectData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Maps instance of {@link PsiJavaFile} to apache {@link Schema} AVRO format
 *
 * @author Anton Kurako (GoodforGod)
 * @since 29.4.2020
 */
@SuppressWarnings("DuplicatedCode")
public class AvroApacheMapper implements MarkerMapper {

    private final MarkerFilter emptyFilter = new EmptyMarkerFilter();
    private final MarkerFilter avroFilter = new AvroFilter();
    private final MarkerFilter annotationFilter = new ExcludeSetterAnnotationFilter();

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
                .orElseThrow(() -> new IllegalArgumentException("Not filter present!"));

        if (filtered.isEmpty())
            return "";

        final Class<?> target = AssistClassFactory.build(filtered);
        final Schema schema = ReflectData.get().getSchema(target);

        final String schemaAsJson = schema.toString(true);
        final String markerPackage = marker.getSourcePackage();
        return schemaAsJson.replaceAll("io\\.goodforgod\\.dummymapper\\.dummies_\\d+", markerPackage);
    }
}
