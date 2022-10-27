package io.goodforgod.dummymapper.marker.filter;

import io.goodforgod.dummymapper.marker.AnnotationMarker;
import io.goodforgod.dummymapper.marker.Marker;
import io.goodforgod.dummymapper.marker.RawMarker;
import java.util.Map;
import java.util.function.Predicate;
import org.jetbrains.annotations.NotNull;

/**
 * Filters out {@link Marker} from structure if any annotations is qualified by {@link #predicate()}
 *
 * @author Anton Kurako (GoodforGod)
 * @since 1.5.2020
 */
public abstract class MarkerAnnotationFilter extends BaseFilter {

    /**
     * @return predicate for fields that will be ignored if qualified
     */
    protected abstract Predicate<AnnotationMarker> predicate();

    @NotNull
    @Override
    public RawMarker filter(@NotNull RawMarker marker) {
        final Predicate<AnnotationMarker> predicate = predicate();
        final Map<String, Marker> structure = marker.getStructure();

        structure.entrySet().stream()
                .filter(e -> e.getValue().getAnnotations().stream().anyMatch(predicate))
                .map(Map.Entry::getKey)
                .forEach(f -> marker.getStructure().remove(f));

        return filterRecursive(marker);
    }
}
