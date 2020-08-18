package io.goodforgod.dummymapper.marker;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * Marker for {@link java.util.Collection} java type
 *
 * @author Anton Kurako (GoodforGod)
 * @since 24.4.2020
 */
public class CollectionMarker extends TypedMarker {

    private final Marker erasure;

    public CollectionMarker(@NotNull String root,
                            @NotNull String source,
                            @NotNull Class<?> type,
                            @NotNull Marker erasure) {
        super(root, source, type);
        this.erasure = erasure;
    }

    @Override
    public boolean isEmpty() {
        return erasure.isEmpty();
    }

    public boolean isRaw() {
        return erasure instanceof RawMarker;
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
        return Objects.equals(erasure, that.erasure);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), erasure);
    }
}
