package io.goodforgod.dummymapper.model;

/**
 * Description in progress
 *
 * @author Anton Kurako (GoodforGod)
 * @since 5.4.2020
 */
public class SimpleMarker extends FieldMarker {

    private Class<?> type;

    public SimpleMarker(String root, String source, Class<?> type) {
        super(root, source);
        this.type = type;
    }

    public Class<?> getType() {
        return type;
    }
}
