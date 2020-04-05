package io.goodforgod.dummymapper.model;

import java.util.Collection;
import java.util.List;

/**
 * Description in progress
 *
 * @author Anton Kurako (GoodforGod)
 * @since 5.4.2020
 */
public class EnumMarker extends FieldMarker {

    public final Collection values;

    public EnumMarker(String source, Collection values) {
        super(source);
        this.values = values;
    }

    public Collection getValues() {
        return values;
    }
}
