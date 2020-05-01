package io.goodforgod.dummymapper.util;

import com.intellij.openapi.util.Pair;
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

    public static Stream<Pair<String, RawMarker>> streamRawPairs(Map<String, Marker> structure) {
        return structure.entrySet().stream()
                .filter(e -> e.getValue() instanceof RawMarker)
                .map(e -> Pair.create(e.getKey(), ((RawMarker) e.getValue())));
    }

    public static Stream<Pair<String, CollectionMarker>> streamCollectionRawPairs(Map<String, Marker> structure) {
        return structure.entrySet().stream()
                .filter(e -> e.getValue() instanceof CollectionMarker && ((CollectionMarker) e.getValue()).isRaw())
                .map(e -> Pair.create(e.getKey(), ((CollectionMarker) e.getValue())));
    }

    public static Stream<Pair<String, MapMarker>> streamMapRawPairs(Map<String, Marker> structure) {
        return structure.entrySet().stream()
                .filter(e -> e.getValue() instanceof MapMarker && ((MapMarker) e.getValue()).isRaw())
                .map(e -> Pair.create(e.getKey(), ((MapMarker) e.getValue())));
    }
}
