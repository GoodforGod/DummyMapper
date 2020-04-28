package io.goodforgod.dummymapper.marker;

import org.jetbrains.annotations.NotNull;

/**
 * Description in progress
 *
 * @author Anton Kurako (GoodforGod)
 * @since 23.4.2020
 */
public class MapMarker extends Marker {

    private final Class<?> type;
    private final Marker keyErasure;
    private final Marker valueErasure;

    public MapMarker(String root, String source, Class<?> type, Marker keyErasure, Marker valueErasure) {
        super(root, source);
        this.type = type;
        this.keyErasure = keyErasure;
        this.valueErasure = valueErasure;
    }

    public @NotNull Class<?> getType() {
        return type;
    }

    public @NotNull Marker getKeyErasure() {
        return keyErasure;
    }

    public @NotNull Marker getValueErasure() {
        return valueErasure;
    }
}
