package io.goodforgod.dummymapper.marker.filter;

import io.goodforgod.dummymapper.marker.AnnotationMarker;
import io.goodforgod.dummymapper.marker.CollectionMarker;
import io.goodforgod.dummymapper.marker.Marker;
import io.goodforgod.dummymapper.marker.MarkerFilter;
import io.goodforgod.dummymapper.marker.RawMarker;
import io.goodforgod.dummymapper.util.MarkerUtils;
import java.util.Map;
import java.util.function.Predicate;
import org.jetbrains.annotations.NotNull;

/**
 * Allow recursion filter for all {@link RawMarker} or {@link CollectionMarker} with such raw
 * markers
 *
 * @author Anton Kurako (GoodforGod)
 * @since 1.5.2020
 */
public abstract class BaseFilter implements MarkerFilter {

    private final String visited = "_filter_" + getClass().getSimpleName().toLowerCase() + "_visited";
    private final Predicate<RawMarker> isVisited = m -> m.getAnnotations().stream()
            .filter(AnnotationMarker::isInternal)
            .anyMatch(a -> a.getName().equals(visited));

    @NotNull
    public RawMarker filterRecursive(@NotNull RawMarker marker) {
        if (isVisited.test(marker))
            return marker;

        marker.addAnnotation(AnnotationMarker.builder().ofInternal().withName(visited).build());
        final Map<String, Marker> structure = marker.getStructure();

        MarkerUtils.streamRawMarkers(structure)
                .filter(m -> !isVisited.test(m))
                .forEach(this::filter);

        MarkerUtils.streamCollectionRawMarkers(structure)
                .filter(m -> !isVisited.test((RawMarker) m.getErasure()))
                .forEach(m -> filter((RawMarker) m.getErasure()));

        MarkerUtils.streamMapRawMarkers(structure).forEach(m -> {
            if (m.getKeyErasure() instanceof RawMarker && !isVisited.test(((RawMarker) m.getKeyErasure())))
                filter((RawMarker) m.getKeyErasure());
            if (m.getValueErasure() instanceof RawMarker && !isVisited.test(((RawMarker) m.getValueErasure())))
                filter((RawMarker) m.getValueErasure());
        });

        return marker;
    }
}
