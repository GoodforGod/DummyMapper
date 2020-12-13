package io.goodforgod.dummymapper.filter.impl;

import io.goodforgod.dummymapper.marker.CollectionMarker;
import io.goodforgod.dummymapper.marker.MapMarker;
import io.goodforgod.dummymapper.marker.Marker;
import io.goodforgod.dummymapper.marker.RawMarker;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

/**
 * Filters out empty {@link RawMarker} or if any {@link CollectionMarker} or {@link MapMarker} have empty {@link RawMarker}
 *
 * @author Anton Kurako (GoodforGod)
 * @since 13.8.2020
 */
public class EmptyMarkerFilter extends BaseFilter {

    @NotNull
    @Override
    public RawMarker filter(@NotNull RawMarker marker) {
        if (marker.isEmpty())
            return RawMarker.EMPTY;

        final RawMarker recursive = filterRecursive(marker);
        final HashMap<String, Marker> structure = new HashMap<>(recursive.getStructure());
        structure.forEach((k, v) -> {
            if (v.isEmpty())
                recursive.getStructure().remove(k);
        });

        return recursive;
    }
}
