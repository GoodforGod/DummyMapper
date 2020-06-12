package io.goodforgod.dummymapper.marker;

import io.goodforgod.dummymapper.model.AnnotationMarker;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static io.dummymaker.util.CollectionUtils.isEmpty;

/**
 * Marker that contains information about class information
 *
 * @author Anton Kurako (GoodforGod)
 * @since 5.4.2020
 */
public abstract class Marker {

    /**
     * Root CLASS marker was extracted from (always child)
     */
    private final String root;

    /**
     * Source CLASS marker was extracted from (can be parent class if inheritance take place)
     */
    private final String source;

    /**
     * Annotations that field is annotated with
     */
    private Set<AnnotationMarker> annotations = Collections.emptySet();

    public Marker(@NotNull String root, @NotNull String source) {
        this.root = root;
        this.source = source;
    }

    @SuppressWarnings("unchecked")
    public <T extends Marker> T setAnnotations(@Nullable Collection<AnnotationMarker> annotations) {
        this.annotations = isEmpty(annotations) ? Collections.emptySet() : new HashSet<>(annotations);
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public <T extends Marker> T addAnnotation(@NotNull AnnotationMarker annotation) {
        if (isEmpty(annotations))
            this.annotations = new HashSet<>();
        this.annotations.add(annotation);
        return (T) this;
    }

    public @NotNull String getSource() {
        return source;
    }

    public @NotNull String getRoot() {
        return root;
    }

    public @NotNull Collection<AnnotationMarker> getAnnotations() {
        return annotations;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Marker marker = (Marker) o;
        return Objects.equals(root, marker.root) &&
                Objects.equals(source, marker.source) &&
                Objects.equals(annotations, marker.annotations);
    }

    @Override
    public int hashCode() {
        return Objects.hash(root, source, annotations);
    }
}
