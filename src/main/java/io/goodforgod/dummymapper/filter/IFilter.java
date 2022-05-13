package io.goodforgod.dummymapper.filter;

import io.goodforgod.dummymapper.marker.RawMarker;
import org.jetbrains.annotations.NotNull;

/**
 * Filters all undesired fields from markers structure
 *
 * @author Anton Kurako (GoodforGod)
 * @since 1.5.2020
 */
public interface IFilter {

    /**
     * @param marker to filter
     * @return marker without filtered fields (all inner RawMarkers should also be ignored)
     */
    @NotNull
    RawMarker filter(@NotNull RawMarker marker);
}
