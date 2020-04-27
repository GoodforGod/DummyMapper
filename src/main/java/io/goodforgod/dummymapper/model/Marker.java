package io.goodforgod.dummymapper.model;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static io.dummymaker.util.CollectionUtils.isEmpty;

/**
 * Description in progress
 *
 * @author Anton Kurako (GoodforGod)
 * @since 5.4.2020
 */
public abstract class Marker {

    private final String root;
    private final String source;
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
        return isEmpty(annotations) ? Collections.emptyList() : new ArrayList<>(annotations);
    }
}
