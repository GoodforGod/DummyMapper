package io.goodforgod.dummymapper.model;

import org.jetbrains.annotations.NotNull;

/**
 * Description in progress
 *
 * @author Anton Kurako (GoodforGod)
 * @since 23.4.2020
 */
public abstract class MapMarker extends Marker {

    private final Class<?> type;

    public MapMarker(String root, String source, Class<?> type) {
        super(root, source);
        this.type = type;
    }

    public @NotNull Class<?> getType() {
        return type;
    }
}
