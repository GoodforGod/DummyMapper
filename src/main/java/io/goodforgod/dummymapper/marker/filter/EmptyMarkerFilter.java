package io.goodforgod.dummymapper.marker.filter;

import io.goodforgod.dummymapper.marker.CollectionMarker;
import io.goodforgod.dummymapper.marker.MapMarker;
import io.goodforgod.dummymapper.marker.Marker;
import io.goodforgod.dummymapper.marker.RawMarker;
import java.util.Map;
import org.jetbrains.annotations.NotNull;

/**
 * Filters out empty {@link RawMarker} or if any {@link CollectionMarker} or {@link MapMarker} have
 * empty {@link RawMarker}
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
        final Map<String, Marker> structure = recursive.getStructure();

        structure.entrySet().stream()
                .filter(e -> e.getValue().isEmpty())
                .map(Map.Entry::getKey)
                .forEach(key -> marker.getStructure().remove(key));

        return recursive;
    }
}
