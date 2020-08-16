package io.goodforgod.dummymapper.filter.impl;

import io.goodforgod.dummymapper.filter.IFilter;
import io.goodforgod.dummymapper.marker.*;
import io.goodforgod.dummymapper.model.AnnotationMarker;
import io.goodforgod.dummymapper.model.AnnotationMarkerBuilder;
import io.goodforgod.dummymapper.util.MarkerUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.function.Predicate;

/**
 * Allow recursion filter for all {@link RawMarker} or {@link CollectionMarker} with such raw markers
 *
 * @author Anton Kurako (GoodforGod)
 * @since 1.5.2020
 */
public abstract class BaseFilter implements IFilter {

    private final String VISITED = "_filter" + getClass().getSimpleName().toLowerCase() + "_visited";
    private final Predicate<RawMarker> IS_VISITED = m -> m.getAnnotations().stream()
            .filter(AnnotationMarker::isInternal)
            .anyMatch(a -> a.getName().equals(VISITED));

    @NotNull
    public RawMarker filterRecursive(@NotNull RawMarker marker) {
        if (IS_VISITED.test(marker))
            return marker;

        marker.addAnnotation(AnnotationMarkerBuilder.get().ofInternal().withName(VISITED).build());
        final Map<String, Marker> structure = marker.getStructure();

        MarkerUtils.streamRawMarkers(structure).filter(m -> !IS_VISITED.test(m)).forEach(this::filter);
        MarkerUtils.streamCollectionRawMarkers(structure).filter(m -> !IS_VISITED.test((RawMarker) m.getErasure()))
                .forEach(m -> filter((RawMarker) m.getErasure()));
        MarkerUtils.streamMapRawMarkers(structure).forEach(m -> {
            if (m.getKeyErasure() instanceof RawMarker && !IS_VISITED.test(((RawMarker) m.getKeyErasure())))
                filter((RawMarker) m.getKeyErasure());
            if (m.getValueErasure() instanceof RawMarker && !IS_VISITED.test(((RawMarker) m.getValueErasure())))
                filter((RawMarker) m.getValueErasure());
        });

        return marker;
    }
}
