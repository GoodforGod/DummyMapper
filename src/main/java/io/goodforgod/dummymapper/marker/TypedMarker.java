package io.goodforgod.dummymapper.marker;

import org.jetbrains.annotations.NotNull;

/**
 * Description in progress
 *
 * @author Anton Kurako (GoodforGod)
 * @since 5.4.2020
 */
public class TypedMarker extends Marker {

    private Class<?> type;

    public TypedMarker(String root, String source, Class<?> type) {
        super(root, source);
        this.type = type;
    }

    public @NotNull Class<?> getType() {
        return type;
    }
}
