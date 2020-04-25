package io.goodforgod.dummymapper.model;

import org.jetbrains.annotations.NotNull;

/**
 * Description in progress
 *
 * @author Anton Kurako (GoodforGod)
 * @since 5.4.2020
 */
public abstract class Marker {

    private final String root;
    private final String source;

    public Marker(@NotNull String root, @NotNull String source) {
        this.root = root;
        this.source = source;
    }

    public @NotNull String getSource() {
        return source;
    }

    public @NotNull String getRoot() {
        return root;
    }
}
