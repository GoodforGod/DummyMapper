package io.goodforgod.dummymapper.util;

import io.goodforgod.dummymapper.marker.CollectionMarker;
import io.goodforgod.dummymapper.marker.MapMarker;
import io.goodforgod.dummymapper.marker.Marker;
import io.goodforgod.dummymapper.marker.RawMarker;

import java.util.Map;
import java.util.stream.Stream;

/**
 * {@link Marker} utils
 *
 * @author Anton Kurako (GoodforGod)
 * @since 1.5.2020
 */
public class MarkerUtils {

    public static Stream<RawMarker> streamRawMarkers(Map<String, Marker> structure) {
        return structure.values().stream()
                .filter(m -> m instanceof RawMarker)
                .map(m -> ((RawMarker) m));
    }

    public static Stream<CollectionMarker> streamCollectionRawMarkers(Map<String, Marker> structure) {
        return structure.values().stream()
                .filter(m -> m instanceof CollectionMarker && ((CollectionMarker) m).isRaw())
                .map(m -> ((CollectionMarker) m));
    }

    public static Stream<MapMarker> streamMapRawMarkers(Map<String, Marker> structure) {
        return structure.values().stream()
                .filter(m -> m instanceof MapMarker && ((MapMarker) m).isRaw())
                .map(m -> ((MapMarker) m));
    }
}
