package io.goodforgod.dummymapper.marker;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

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
    private List<String> annotations = Collections.emptyList();

    public Marker(@NotNull String root, @NotNull String source) {
        this.root = root;
        this.source = source;
    }

    @SuppressWarnings("unchecked")
    public <T extends Marker> T setAnnotations(@Nullable Collection<String> annotations) {
        this.annotations = isEmpty(annotations) ? Collections.emptyList() : new ArrayList<>(annotations);
        return (T) this;
    }

    public @NotNull String getSource() {
        return source;
    }

    public @NotNull String getRoot() {
        return root;
    }

    public @NotNull Collection<String> getAnnotations() {
        return annotations;
    }
}
