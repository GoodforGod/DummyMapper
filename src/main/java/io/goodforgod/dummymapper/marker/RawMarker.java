package io.goodforgod.dummymapper.marker;

import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Map;

/**
 * @author Anton Kurako (GoodforGod)
 * @since 26.4.2020
 */
public class RawMarker extends Marker {

    public static final RawMarker EMPTY = new RawMarker("", "", Collections.emptyMap());

    private final Map<String, Marker> structure;

    public RawMarker(@NotNull String root, @NotNull String source, Map<String, Marker> structure) {
        super(root, source);
        this.structure = (structure == null) ? Collections.emptyMap() : structure;
    }

    public boolean isEmpty() {
        return getStructure().isEmpty();
    }

    public @NotNull Map<String, Marker> getStructure() {
        return structure;
    }
}
