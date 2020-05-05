package io.goodforgod.dummymapper.marker;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * Array marker for any array typed with {@link Marker}
 *
 * @author Anton Kurako (GoodforGod)
 * @since 5.5.2020
 */
public class ArrayMarker extends Marker {

    private final Marker erasure;

    public ArrayMarker(@NotNull String root, @NotNull String source, @NotNull Marker erasure) {
        super(root, source);
        this.erasure = erasure;
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
        ArrayMarker that = (ArrayMarker) o;
        return Objects.equals(erasure, that.erasure);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), erasure);
    }
}
