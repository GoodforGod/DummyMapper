package io.goodforgod.dummymapper.marker;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * Marker for {@link java.util.Collection} java type
 *
 * @author Anton Kurako (GoodforGod)
 * @since 24.4.2020
 */
public class CollectionMarker extends Marker {

    private final Class<?> type;
    private final Marker erasure;

    public CollectionMarker(@NotNull String root,
                            @NotNull String source,
                            @NotNull Class<?> type,
                            @NotNull Marker erasure) {
        super(root, source);
        this.type = type;
        this.erasure = erasure;
    }

    public boolean isRaw() {
        return erasure instanceof RawMarker;
    }

    public @NotNull Class<?> getType() {
        return type;
    }

    public @NotNull Marker getErasure() {
        return erasure;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        if (!super.equals(o))
            return false;
        CollectionMarker that = (CollectionMarker) o;
        return Objects.equals(type.getName(), that.type.getName()) &&
                Objects.equals(erasure, that.erasure);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), type.getName(), erasure);
    }
}
