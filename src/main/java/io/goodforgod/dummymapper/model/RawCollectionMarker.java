package io.goodforgod.dummymapper.model;

import io.dummymaker.util.StringUtils;
import org.jetbrains.annotations.NotNull;

/**
 * Description in progress
 *
 * @author Anton Kurako (GoodforGod)
 * @since 24.4.2020
 */
public class RawCollectionMarker extends CollectionMarker {

    private final String erasure;

    public RawCollectionMarker(String root, String source, Class<?> type, String erasure) {
        super(root, source, type);
        this.erasure = erasure;
    }

    public @NotNull String getErasure() {
        return StringUtils.isEmpty(erasure) ? "" : erasure;
    }
}
