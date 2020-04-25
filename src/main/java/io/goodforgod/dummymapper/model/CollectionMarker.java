package io.goodforgod.dummymapper.model;

import org.jetbrains.annotations.NotNull;

/**
 * Description in progress
 *
 * @author Anton Kurako (GoodforGod)
 * @since 23.4.2020
 */
public abstract class CollectionMarker extends Marker {

    private final Class<?> type;

    public CollectionMarker(String root, String source, Class<?> type) {
        super(root, source);
        this.type = type;
    }

    public @NotNull Class<?> getType() {
        return type;
    }
}
