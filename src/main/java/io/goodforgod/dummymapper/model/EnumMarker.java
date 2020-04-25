package io.goodforgod.dummymapper.model;

import io.dummymaker.util.CollectionUtils;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Description in progress
 *
 * @author Anton Kurako (GoodforGod)
 * @since 5.4.2020
 */
public class EnumMarker extends Marker {

    public final List<String> values;

    public EnumMarker(String root, String source, Collection<String> values) {
        super(root, source);
        this.values = CollectionUtils.isEmpty(values) ? Collections.emptyList() : new ArrayList<>(values);
    }

    public @NotNull List<String> getValues() {
        return values;
    }
}
