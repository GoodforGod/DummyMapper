package io.goodforgod.dummymapper.filter.impl;

import com.pty4j.util.Pair;
import io.goodforgod.dummymapper.filter.IFilter;
import io.goodforgod.dummymapper.marker.CollectionMarker;
import io.goodforgod.dummymapper.marker.MapMarker;
import io.goodforgod.dummymapper.marker.Marker;
import io.goodforgod.dummymapper.marker.RawMarker;
import io.goodforgod.dummymapper.util.MarkerUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Filters out fields which are marked provided via {@link #getIgnoreAnnotations()} method
 *
 * @author Anton Kurako (GoodforGod)
 * @since 1.5.2020
 */
public abstract class AnnotationFilter implements IFilter {

    /**
     * @return annotation classes that should be ignored
     */
    protected abstract Collection<Class<?>> getIgnoreAnnotations();

    protected boolean filterSetters() {
        return true;
    }

    protected boolean filterGetters() {
        return true;
    }

    protected boolean filterFields() {
        return true;
    }

    @NotNull
    @Override
    public RawMarker filter(@NotNull RawMarker marker) {
        final Set<String> ignore = getIgnoreAnnotations().stream()
                .map(Class::getName)
                .collect(Collectors.toSet());

        final Map<String, Marker> structure = marker.getStructure().entrySet().stream()
                .filter(m -> m.getValue().getAnnotations().stream()
                        .filter(a -> a.isFieldMarked() && filterFields()
                                || a.isGetterMarked() && filterGetters()
                                || a.isSetterMarked() && filterSetters())
                        .noneMatch(a -> ignore.contains(a.getName())))
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
