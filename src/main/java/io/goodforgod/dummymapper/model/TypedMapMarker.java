package io.goodforgod.dummymapper.model;

import org.jetbrains.annotations.NotNull;

/**
 * Description in progress
 *
 * @author Anton Kurako (GoodforGod)
 * @since 23.4.2020
 */
public class TypedMapMarker extends MapMarker {

    private final Class<?> keyErasure;
    private final Class<?> valueErasure;

    public TypedMapMarker(String root, String source, Class<?> type, Class<?> keyErasure, Class<?> valueErasure) {
        super(root, source, type);
        this.keyErasure = keyErasure;
        this.valueErasure = valueErasure;
    }

    public @NotNull Class<?> getKeyErasure() {
        return keyErasure;
    }

    public @NotNull Class<?> getValueErasure() {
        return valueErasure;
    }
}
