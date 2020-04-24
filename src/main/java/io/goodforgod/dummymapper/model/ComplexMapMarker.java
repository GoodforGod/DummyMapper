package io.goodforgod.dummymapper.model;

import io.dummymaker.util.StringUtils;
import org.jetbrains.annotations.NotNull;

/**
 * Description in progress
 *
 * @author Anton Kurako (GoodforGod)
 * @since 23.4.2020
 */
public class ComplexMapMarker extends MapMarker {

    private final String keyErasure;
    private final String valueErasure;

    public ComplexMapMarker(String root, String source, Class<?> type, String keyErasure, String valueErasure) {
        super(root, source, type);
        this.keyErasure = keyErasure;
        this.valueErasure = valueErasure;
    }

    public @NotNull String getKeyErasure() {
        return StringUtils.isEmpty(keyErasure) ? "" : keyErasure;
    }

    public @NotNull String getValueErasure() {
        return StringUtils.isEmpty(valueErasure) ? "" : valueErasure;
    }
}
