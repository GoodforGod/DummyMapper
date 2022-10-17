package io.goodforgod.dummymapper.marker;

import io.dummymaker.util.CollectionUtils;
import io.dummymaker.util.StringUtils;
import io.goodforgod.dummymapper.model.AnnotationMarker;
import java.util.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
        this.annotations = CollectionUtils.isEmpty(annotations)
                ? Collections.emptySet()
                : new HashSet<>(annotations);
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public <T extends Marker> T addAnnotation(@NotNull AnnotationMarker annotation) {
        if (CollectionUtils.isEmpty(annotations))
            this.annotations = new HashSet<>();
        this.annotations.add(annotation);
        return (T) this;
    }

    public abstract boolean isEmpty();

    public @NotNull String getRoot() {
        return root;
    }

    public @NotNull String getRootPackage() {
        if (StringUtils.isEmpty(root))
            return "";

        final String cleanRoot = getCleanRoot();
        return cleanRoot.substring(0, cleanRoot.lastIndexOf('.'));
    }

    public @NotNull String getRootSimpleName() {
        if (StringUtils.isEmpty(source))
            return "";

        final String cleanRoot = getCleanRoot();
        return cleanRoot.substring(cleanRoot.lastIndexOf('.') + 1);
    }

    public @NotNull String getSource() {
        return source;
    }

    public @NotNull String getSourcePackage() {
        if (StringUtils.isEmpty(source))
            return "";

        final String cleanSource = getCleanSource();
        return cleanSource.substring(0, cleanSource.lastIndexOf('.'));
    }

    public @NotNull String getSourceSimpleName() {
        if (StringUtils.isEmpty(source))
            return "";

        final String cleanSource = getCleanSource();
        return cleanSource.substring(cleanSource.lastIndexOf('.') + 1);
    }

    private String getCleanRoot() {
        return root.replaceFirst("\\.java$", "")
                .replaceFirst("\\.kt$", "");
    }

    private String getCleanSource() {
        return source.replaceFirst("\\.java$", "")
                .replaceFirst("\\.kt$", "");
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
