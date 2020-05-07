package io.goodforgod.dummymapper.marker;

import io.dummymaker.util.CollectionUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Marker for {@link Enum} java type
 *
 * @author Anton Kurako (GoodforGod)
 * @since 5.4.2020
 */
public class EnumMarker extends Marker {

    private final List<String> values;

    public EnumMarker(@NotNull String root,
                      @NotNull String source,
                      @Nullable Collection<String> values) {
        super(root, source);
        this.values = CollectionUtils.isEmpty(values) ? Collections.emptyList() : new ArrayList<>(values);
    }

    public @NotNull List<String> getValues() {
        return values;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        if (!super.equals(o))
            return false;
        EnumMarker that = (EnumMarker) o;
        return Objects.equals(values, that.values);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), values);
    }
}
