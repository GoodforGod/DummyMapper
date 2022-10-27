package io.goodforgod.dummymapper.marker;

import java.util.Objects;
import org.jetbrains.annotations.NotNull;

/**
 * Array marker for any array typed with {@link Marker}
 *
 * @author Anton Kurako (GoodforGod)
 * @since 5.5.2020
 */
public class ArrayMarker extends Marker {

    private final Marker erasure;
    private final int dimensions;

    public ArrayMarker(@NotNull String root, @NotNull String source, @NotNull Marker erasure, int dimensions) {
        super(root, source);
        this.erasure = erasure;
        this.dimensions = dimensions;
    }

    @Override
    public boolean isEmpty() {
        return erasure.isEmpty();
    }

    public boolean isRaw() {
        return erasure instanceof RawMarker;
    }

    public int getDimensions() {
        return dimensions;
    }

    public @NotNull Marker getErasure() {
        return erasure;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof ArrayMarker))
            return false;
        if (!super.equals(o))
            return false;
        ArrayMarker that = (ArrayMarker) o;
        return dimensions == that.dimensions && Objects.equals(erasure, that.erasure);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), erasure, dimensions);
    }
}
