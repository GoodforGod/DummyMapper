package io.goodforgod.dummymapper.marker;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * Marker for {@link java.util.Map} java type
 *
 * @author Anton Kurako (GoodforGod)
 * @since 23.4.2020
 */
public class MapMarker extends TypedMarker {

    private final Marker keyErasure;
    private final Marker valueErasure;

    public MapMarker(@NotNull String root,
                     @NotNull String source,
                     @NotNull Class<?> type,
                     @NotNull Marker keyErasure,
                     @NotNull Marker valueErasure) {
        super(root, source, type);
        this.keyErasure = keyErasure;
        this.valueErasure = valueErasure;
    }

    public boolean isRaw() {
        return keyErasure instanceof RawMarker || valueErasure instanceof RawMarker;
    }

    public @NotNull Marker getKeyErasure() {
        return keyErasure;
    }

    public @NotNull Marker getValueErasure() {
        return valueErasure;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        MapMarker mapMarker = (MapMarker) o;
        return Objects.equals(keyErasure, mapMarker.keyErasure) &&
                Objects.equals(valueErasure, mapMarker.valueErasure);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), keyErasure, valueErasure);
    }
}
