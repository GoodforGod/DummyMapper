package io.goodforgod.dummymapper.model;

import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Map;

/**
 * Description in progress
 *
 * @author Anton Kurako (GoodforGod)
 * @since 26.4.2020
 */
public class RawMarker extends Marker {

    private final Map<String, Marker> structure;

    public RawMarker(@NotNull String root, @NotNull String source, Map<String, Marker> structure) {
        super(root, source);
        this.structure = structure;
    }

    public @NotNull Map<String, Marker> getStructure() {
        return (structure == null) ? Collections.emptyMap() : structure;
    }
}
