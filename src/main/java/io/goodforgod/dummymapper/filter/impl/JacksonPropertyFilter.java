package io.goodforgod.dummymapper.filter.impl;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.goodforgod.dummymapper.marker.RawMarker;
import io.goodforgod.dummymapper.model.AnnotationMarkerBuilder;
import org.jetbrains.annotations.NotNull;

/**
 * Add {@link JsonProperty#required()} true if such annotation is not present on field
 *
 * @author Anton Kurako (GoodforGod)
 * @since 12.6.2020
 */
public class JacksonPropertyFilter extends BaseFilter {

    private static final String REQUIRED_PROPERTY = "required";

    @NotNull
    @Override
    public RawMarker filter(@NotNull RawMarker marker) {
        marker.getStructure().entrySet().stream()
                .filter(e -> e.getValue().getAnnotations().stream()
                        .noneMatch(a -> a.named(JsonProperty.class) && a.haveAttribute(REQUIRED_PROPERTY)))
                .forEach(e -> e.getValue().addAnnotation(AnnotationMarkerBuilder.get()
                        .ofField()
                        .withName(JsonProperty.class)
                        .withAttribute(REQUIRED_PROPERTY, true)
                        .build()));

        return filterRecursive(marker);
    }
}
