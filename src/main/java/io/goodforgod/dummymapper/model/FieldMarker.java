package io.goodforgod.dummymapper.model;

/**
 * Description in progress
 *
 * @author Anton Kurako (GoodforGod)
 * @since 5.4.2020
 */
public abstract class FieldMarker {

    public final String source;

    public FieldMarker(String source) {
        this.source = source;
    }

    public String getSource() {
        return source;
    }
}
