package io.goodforgod.dummymapper.model;

import org.jetbrains.annotations.NotNull;

/**
 * Annotation marker
 *
 * @author Anton Kurako (GoodforGod)
 * @since 1.5.2020
 */
public class AnnotationMarker {

    private final String name;
    private final boolean isFieldMarked;
    private final boolean isGetterMarked;
    private final boolean isSetterMarked;

    private AnnotationMarker(String name, boolean isFieldMarked, boolean isGetterMarked, boolean isSetterMarked) {
        this.name = name;
        this.isFieldMarked = isFieldMarked;
        this.isGetterMarked = isGetterMarked;
        this.isSetterMarked = isSetterMarked;
    }

    public static AnnotationMarker ofField(@NotNull String name) {
        return new AnnotationMarker(name, true, false, false);
    }

    public static AnnotationMarker ofGetter(@NotNull String name) {
        return new AnnotationMarker(name, false, true, false);
    }

    public static AnnotationMarker ofSetter(@NotNull String name) {
        return new AnnotationMarker(name, false, false, true);
    }

    public @NotNull String getName() {
        return name;
    }

    public boolean isFieldMarked() {
        return isFieldMarked;
    }

    public boolean isGetterMarked() {
        return isGetterMarked;
    }

    public boolean isSetterMarked() {
        return isSetterMarked;
    }
}
