package io.goodforgod.dummymapper.filter.impl;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.goodforgod.dummymapper.marker.RawMarker;
import io.goodforgod.dummymapper.model.AnnotationMarker;
import io.goodforgod.dummymapper.model.AnnotationMarkerBuilder;
import org.apache.avro.reflect.AvroIgnore;
import org.apache.avro.reflect.Nullable;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * Filter structure for fields that should be ignored during AVRO schema generation
 *
 * @author Anton Kurako (GoodforGod)
 * @since 3.5.2020
 */
public class AvroFilter extends FieldFilter {

    private static final String REQUIRED_PROPERTY = "required";

    @Override
    protected Predicate<AnnotationMarker> predicate() {
        return a -> a.named(AvroIgnore.class);
    }

    @NotNull
    @Override
    public RawMarker filter(@NotNull RawMarker marker) {
        marker.getStructure().forEach((k, v) -> {
            final Collection<AnnotationMarker> annotations = v.getAnnotations();
            if (annotations.stream().anyMatch(a -> a.named(Nullable.class))) {
                final Optional<AnnotationMarker> property = annotations.stream()
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
            }
        });

        return super.filter(marker);
    }
}
