package io.goodforgod.dummymapper.filter.impl;

import io.goodforgod.dummymapper.filter.IFilter;
import io.goodforgod.dummymapper.marker.*;
import io.goodforgod.dummymapper.util.MarkerUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * Allow recursion filter for all {@link RawMarker} or {@link CollectionMarker} with such raw markers
 *
 * @author Anton Kurako (GoodforGod)
 * @since 1.5.2020
 */
public abstract class BaseFilter implements IFilter {

    @NotNull
    public RawMarker filterRecursive(@NotNull RawMarker marker) {
        final Map<String, Marker> structure = marker.getStructure();

        MarkerUtils.streamRawMarkers(structure).forEach(this::filter);
        MarkerUtils.streamCollectionRawMarkers(structure).forEach(m -> filter((RawMarker) m.getErasure()));
        MarkerUtils.streamMapRawMarkers(structure).forEach(m -> {
            if (m.getKeyErasure() instanceof RawMarker)
                filter((RawMarker) m.getKeyErasure());
            if (m.getValueErasure() instanceof RawMarker)
                filter((RawMarker) m.getValueErasure());
        });

        return marker;
    }
}
