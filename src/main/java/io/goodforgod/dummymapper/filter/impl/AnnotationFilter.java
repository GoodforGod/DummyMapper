package io.goodforgod.dummymapper.filter.impl;


import io.goodforgod.dummymapper.marker.Marker;
import io.goodforgod.dummymapper.marker.RawMarker;
import io.goodforgod.dummymapper.model.AnnotationMarker;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;


/**
 * Filters out {@link AnnotationMarker} from {@link Marker} which are not qualified to
 * {@link #allowed()}
 *
 * @author Anton Kurako (GoodforGod)
 * @since 1.5.2020
 */
public abstract class AnnotationFilter extends BaseFilter {

    /**
     * @return predicate for annotations that will be allowed
     */
    protected abstract Predicate<AnnotationMarker> allowed();

    @NotNull
    @Override
    public RawMarker filter(@NotNull RawMarker marker) {
        final Predicate<AnnotationMarker> allowed = allowed();
        final Map<String, Marker> structure = marker.getStructure();

        structure.forEach((k, v) -> {
            final Set<AnnotationMarker> left = v.getAnnotations().stream()
                    .filter(a -> allowed.test(a) || a.isInternal())
                    .collect(Collectors.toSet());
            v.setAnnotations(left);
        });

        return filterRecursive(marker);
    }
}
