package io.goodforgod.dummymapper.model;

import io.dummymaker.util.StringUtils;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

/**
 * Description in progress
 *
 * @author Anton Kurako (GoodforGod)
 * @since 24.4.2020
 */
public class ComplexCollectionMarker extends CollectionMarker {

    private final String erasure;

    public ComplexCollectionMarker(String root, String source, Class<?> type, String  erasure) {
        super(root, source, type);
        this.erasure = erasure;
    }

    public @NotNull String getErasure() {
        return StringUtils.isEmpty(erasure) ? "" : erasure;
    }
}
