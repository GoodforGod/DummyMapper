package io.goodforgod.dummymapper.filter.impl;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.goodforgod.dummymapper.marker.RawMarker;
import io.goodforgod.dummymapper.model.AnnotationMarker;
import io.goodforgod.dummymapper.model.AnnotationMarkerBuilder;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

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
        marker.getStructure().forEach((k, v) -> {
            final Optional<AnnotationMarker> property = v.getAnnotations().stream()
                    .filter(a -> a.named(JsonProperty.class))
                    .findFirst();
            if (!property.isPresent()) {
                v.addAnnotation(AnnotationMarkerBuilder.get()
                        .ofField()
                        .withName(JsonProperty.class)
                        .withAttribute(REQUIRED_PROPERTY, true)
                        .build());
            } else {
                property.get().getAttributes().put(REQUIRED_PROPERTY, true);
            }
        });

        return filterRecursive(marker);
    }
}
