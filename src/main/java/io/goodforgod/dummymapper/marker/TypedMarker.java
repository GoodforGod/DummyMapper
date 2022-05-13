package io.goodforgod.dummymapper.marker;

import java.util.Objects;
import org.jetbrains.annotations.NotNull;

/**
 * Marker for known Java type (like {@link Integer}, {@link java.util.Date}, etc)
 *
 * @author Anton Kurako (GoodforGod)
 * @since 5.4.2020
 */
public class TypedMarker extends Marker {

    private final Class<?> type;

    public TypedMarker(@NotNull String root, @NotNull String source, @NotNull Class<?> type) {
        super(root, source);
        this.type = type;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    public @NotNull Class<?> getType() {
        return type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        if (!super.equals(o))
            return false;
        TypedMarker that = (TypedMarker) o;
        return Objects.equals(type, that.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), type);
    }
}
