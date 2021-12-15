package io.goodforgod.dummymapper.model;


import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.jetbrains.annotations.NotNull;


/**
 * Builder for {@link AnnotationMarker}
 *
 * @author Anton Kurako (GoodforGod)
 * @since 2.5.2020
 */
public final class AnnotationMarkerBuilder {

    private boolean isInternal = false;
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

    public AnnotationMarkerBuilder withName(@NotNull Class<?> annotation) {
        this.name = annotation.getName();
        return this;
    }

    public AnnotationMarkerBuilder ofField() {
        this.isFieldMarked = true;
        return this;
    }

    public AnnotationMarkerBuilder ofGetter() {
        this.isGetterMarked = true;
        return this;
    }

    public AnnotationMarkerBuilder ofSetter() {
        this.isSetterMarked = true;
        return this;
    }

    public AnnotationMarkerBuilder ofInternal() {
        this.isInternal = true;
        return this;
    }

    public AnnotationMarkerBuilder withAttribute(@NotNull String name, Object value) {
        if (this.attributes.isEmpty())
            this.attributes = new HashMap<>(2);
        this.attributes.put(name, value);
        return this;
    }

    public AnnotationMarkerBuilder withAttributes(@NotNull Map<String, Object> attributes) {
        this.attributes = attributes.isEmpty()
                ? Collections.emptyMap()
                : attributes;
        return this;
    }

    public AnnotationMarker build() {
        return new AnnotationMarker(name, isInternal, isFieldMarked, isGetterMarked, isSetterMarked, attributes);
    }
}
