package io.goodforgod.dummymapper.model;

import org.jetbrains.annotations.NotNull;

/**
 * Description in progress
 *
 * @author Anton Kurako (GoodforGod)
 * @since 24.4.2020
 */
public class TypedCollectionMarker extends CollectionMarker {

    private final Class<?> erasure;

    public TypedCollectionMarker(String root, String source, Class<?> type, Class<?> erasure) {
        super(root, source, type);
        this.erasure = erasure;
    }

    public @NotNull Class<?> getErasure() {
        return erasure;
    }
}
