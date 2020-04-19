package io.goodforgod.dummymapper.model;

import java.util.List;

/**
 * Description in progress
 *
 * @author Anton Kurako (GoodforGod)
 * @since 5.4.2020
 */
public class EnumMarker extends FieldMarker {

    public final List<String> values;

    public EnumMarker(String root, String source, List<String> values) {
        super(root, source);
        this.values = values;
    }

    public List<String> getValues() {
        return values;
    }
}
