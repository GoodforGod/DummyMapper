package io.goodforgod.dummymapper.model;

import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Map;

/**
 * Description in progress
 *
 * @author Anton Kurako (GoodforGod)
 * @since 2.5.2020
 */
public final class AnnotationMarkerBuilder {

    private String name;
    private boolean isFieldMarked = false;
    private boolean isGetterMarked = false;
    private boolean isSetterMarked = false;
    private Map<String, Object> attributes = Collections.emptyMap();

    private AnnotationMarkerBuilder() {}

    public static AnnotationMarkerBuilder get() {
        return new AnnotationMarkerBuilder();
    }

    public AnnotationMarkerBuilder withName(@NotNull String name) {
        this.name = name;
        return this;
    }

    public AnnotationMarkerBuilder ofField() {
        this.isFieldMarked = true;
        this.isGetterMarked = false;
        this.isSetterMarked = false;
        return this;
    }

    public AnnotationMarkerBuilder ofGetter() {
        this.isFieldMarked = false;
        this.isGetterMarked = true;
        this.isSetterMarked = false;
        return this;
    }

    public AnnotationMarkerBuilder ofSetter() {
        this.isFieldMarked = false;
        this.isGetterMarked = false;
        this.isSetterMarked = true;
        return this;
    }

    public AnnotationMarkerBuilder withAttributes(@NotNull Map<String, Object> attributes) {
        this.attributes = (attributes.isEmpty()) ? Collections.emptyMap() : attributes;
        return this;
    }

    public AnnotationMarker build() {
        return new AnnotationMarker(name, isFieldMarked, isGetterMarked, isSetterMarked, attributes);
    }
}
