package io.goodforgod.dummymapper.marker.filter;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.goodforgod.dummymapper.marker.AnnotationMarker;
import io.goodforgod.dummymapper.marker.RawMarker;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Predicate;
import org.apache.avro.reflect.AvroIgnore;
import org.apache.avro.reflect.Nullable;
import org.jetbrains.annotations.NotNull;

/**
 * Filter structure for fields that should be ignored during AVRO schema generation
 *
 * @author Anton Kurako (GoodforGod)
 * @since 3.5.2020
 */
public class AvroFilter extends MarkerAnnotationFilter {

    private static final String REQUIRED_PROPERTY = "required";

    @Override
    protected Predicate<AnnotationMarker> predicate() {
        return a -> a.named(AvroIgnore.class);
    }

    @NotNull
    @Override
    public RawMarker filter(@NotNull RawMarker marker) {
        marker.getStructure().forEach((k, v) -> {
            if (v.getAnnotations().stream().anyMatch(a -> a.named(Nullable.class))) {
                final Map<String, Object> annotationAttrs = v.getAnnotations().stream()
                        .filter(a -> a.named(JsonProperty.class))
                        .map(AnnotationMarker::getAttributes)
                        .findFirst()
                        .orElseGet(Collections::emptyMap);

                final Map<String, Object> attrs = new LinkedHashMap<>(annotationAttrs);
                attrs.put(REQUIRED_PROPERTY, false);

                v.addAnnotation(AnnotationMarker.builder()
                        .ofField()
                        .withName(JsonProperty.class)
                        .withAttributes(attrs)
                        .build());
            }
        });

        return super.filter(marker);
    }
}
