package io.goodforgod.dummymapper.model;

import org.jetbrains.annotations.NotNull;

/**
 * Description in progress
 *
 * @author Anton Kurako (GoodforGod)
 * @since 24.4.2020
 */
public class CollectionMarker extends Marker {

    private final Class<?> type;
    private final Marker erasure;

    public CollectionMarker(String root, String source, Class<?> type, Marker erasure) {
        super(root, source);
        this.type = type;
        this.erasure = erasure;
    }

    public @NotNull Class<?> getType() {
        return type;
    }

    public @NotNull Marker getErasure() {
        return erasure;
    }
}
