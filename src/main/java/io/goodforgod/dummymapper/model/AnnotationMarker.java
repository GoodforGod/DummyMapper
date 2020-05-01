package io.goodforgod.dummymapper.model;

import org.jetbrains.annotations.NotNull;

/**
 * Annotation marker
 *
 * @author Anton Kurako (GoodforGod)
 * @since 1.5.2020
 */
public class AnnotationMarker {

    private String name;
    private boolean isFieldMarked;
    private boolean isGetterMarked;

    private AnnotationMarker(String name, boolean isFieldMarked, boolean isGetterMarked) {
        this.name = name;
        this.isFieldMarked = isFieldMarked;
        this.isGetterMarked = isGetterMarked;
    }

    public static AnnotationMarker ofField(@NotNull String name) {
        return new AnnotationMarker(name, true, false);
    }

    public static AnnotationMarker ofMethod(@NotNull String name) {
        return new AnnotationMarker(name, false, true);
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
}
