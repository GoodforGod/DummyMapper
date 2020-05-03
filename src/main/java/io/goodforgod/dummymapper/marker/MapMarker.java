package io.goodforgod.dummymapper.marker;

import org.jetbrains.annotations.NotNull;

/**
 * Marker for {@link java.util.Map} java type
 *
 * @author Anton Kurako (GoodforGod)
 * @since 23.4.2020
 */
public class MapMarker extends Marker {

    private final Class<?> type;
    private final Marker keyErasure;
    private final Marker valueErasure;

    public MapMarker(@NotNull String root,
                     @NotNull String source,
                     @NotNull Class<?> type,
                     @NotNull Marker keyErasure,
                     @NotNull Marker valueErasure) {
        super(root, source);
        this.type = type;
        this.keyErasure = keyErasure;
        this.valueErasure = valueErasure;
    }

    public boolean isRaw() {
        return keyErasure instanceof RawMarker || valueErasure instanceof RawMarker;
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
