package io.goodforgod.dummymapper.filter.impl;

import com.pty4j.util.Pair;
import io.goodforgod.dummymapper.filter.IFilter;
import io.goodforgod.dummymapper.marker.CollectionMarker;
import io.goodforgod.dummymapper.marker.MapMarker;
import io.goodforgod.dummymapper.marker.Marker;
import io.goodforgod.dummymapper.marker.RawMarker;
import io.goodforgod.dummymapper.model.AnnotationMarker;
import io.goodforgod.dummymapper.util.MarkerUtils;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Filters out {@link AnnotationMarker} from {@link Marker} which are not qualified to {@link #predicate()}
 *
 * @author Anton Kurako (GoodforGod)
 * @since 1.5.2020
 */
public abstract class AnnotationFilter implements IFilter {

    /**
     * @return annotation classes that should be ignored
     */
    protected abstract Predicate<AnnotationMarker> predicate();

    @NotNull
    @Override
    public RawMarker filter(@NotNull RawMarker marker) {
        final Predicate<AnnotationMarker> predicate = predicate();

        final Map<String, Marker> structure = marker.getStructure().entrySet().stream()
                .map(m -> {
                    final List<AnnotationMarker> markers = m.getValue().getAnnotations().stream()
                            .filter(predicate)
                            .collect(Collectors.toList());

                    m.getValue().setAnnotations(markers);
                    return m;
                })
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        final Map<String, RawMarker> rawMarkers = MarkerUtils.streamRawPairs(structure)
                .collect(Collectors.toMap(p -> p.getFirst(), p -> filter(p.getSecond())));

        final Map<String, CollectionMarker> collectionRawMarkers = MarkerUtils.streamCollectionRawPairs(structure)
                .collect(Collectors.toMap(p -> p.getFirst(), p -> new CollectionMarker(p.getSecond().getRoot(),
                        p.getSecond().getSource(),
                        p.getSecond().getType(),
                        filter((RawMarker) p.getSecond().getErasure()))));

        final Map<String, MapMarker> mapRawMarkers = MarkerUtils.streamMapRawPairs(structure)
                .map(p -> {
                    final Marker m1 = p.getSecond().getKeyErasure() instanceof RawMarker
                            ? filter((RawMarker) p.getSecond().getKeyErasure())
                            : p.getSecond().getKeyErasure();

                    final Marker m2 = p.getSecond().getValueErasure() instanceof RawMarker
                            ? filter((RawMarker) p.getSecond().getValueErasure())
                            : p.getSecond().getValueErasure();

                    final MapMarker mapMarker = new MapMarker(p.getSecond().getRoot(), p.getSecond().getSource(),
                            p.getSecond().getType(), m1, m2);
                    return Pair.create(p.getFirst(), mapMarker);
                })
                .collect(Collectors.toMap(p -> p.getFirst(), p -> p.getSecond()));

        structure.putAll(rawMarkers);
        structure.putAll(collectionRawMarkers);
        structure.putAll(mapRawMarkers);

        return new RawMarker(marker.getRoot(), marker.getSource(), structure);
    }
}
