package io.goodforgod.dummymapper.marker;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Marker for unknown Java type (custom class)
 *
 * @author Anton Kurako (GoodforGod)
 * @since 26.4.2020
 */
public class RawMarker extends Marker {

    public static final RawMarker EMPTY = new RawMarker("", "", Collections.emptyMap());

    private final Map<String, Marker> structure;

    public RawMarker(@NotNull String root,
                     @NotNull String source,
                     @Nullable Map<String, Marker> structure) {
        super(root, source);
        this.structure = (structure == null || structure.isEmpty())
                ? Collections.emptyMap()
                : new LinkedHashMap<>(structure);
    }

    @Override
    public boolean isEmpty() {
        return structure.isEmpty();
    }

    public @NotNull Map<String, Marker> getStructure() {
        return structure;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        if (!super.equals(o))
            return false;
        RawMarker rawMarker = (RawMarker) o;
        return Objects.equals(structure, rawMarker.structure);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), structure);
    }
}
