package io.goodforgod.dummymapper.marker;

import io.dummymaker.util.CollectionUtils;
import io.dummymaker.util.StringUtils;
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

    Marker(@NotNull String root, @NotNull String source) {
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

    public @NotNull String getRootClassName() {
        if (StringUtils.isEmpty(root))
            return "";

        final String rootWithoutExtension = getRootWithoutExtension();
        return rootWithoutExtension.substring(rootWithoutExtension.lastIndexOf('.') + 1);
    }

    public @NotNull String getRootPackage() {
        if (StringUtils.isEmpty(root))
            return "";

        final String cleanRoot = getRootWithoutExtension();
        return cleanRoot.substring(0, cleanRoot.lastIndexOf('.'));
    }

    public @NotNull String getRootWithoutExtension() {
        return StringUtils.isEmpty(root)
                ? ""
                : root.substring(0, root.lastIndexOf('.'));
    }

    public @NotNull String getSource() {
        if (StringUtils.isEmpty(source))
            return "";

        return source;
    }

    public @NotNull String getSourceClassName() {
        if (StringUtils.isEmpty(source))
            return "";

        final String sourceWithoutExtension = getSourceWithoutExtension();
        return sourceWithoutExtension.substring(sourceWithoutExtension.lastIndexOf('.') + 1);
    }

    public @NotNull String getSourcePackage() {
        if (StringUtils.isEmpty(source))
            return "";

        final String cleanSource = getSourceWithoutExtension();
        return cleanSource.substring(0, cleanSource.lastIndexOf('.'));
    }

    public @NotNull String getSourceWithoutExtension() {
        return StringUtils.isEmpty(source)
                ? ""
                : source.substring(0, source.lastIndexOf('.'));
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
