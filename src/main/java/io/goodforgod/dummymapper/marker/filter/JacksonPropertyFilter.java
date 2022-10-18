package io.goodforgod.dummymapper.marker.filter;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.goodforgod.dummymapper.marker.AnnotationMarker;
import io.goodforgod.dummymapper.marker.RawMarker;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
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
        marker.getStructure().forEach((k, v) -> {
            final Map<String, Object> annotationAttrs = v.getAnnotations().stream()
                    .filter(a -> a.named(JsonProperty.class))
                    .map(AnnotationMarker::getAttributes)
                    .findFirst()
                    .orElseGet(Collections::emptyMap);

            final Map<String, Object> attrs = new HashMap<>(annotationAttrs);
            attrs.put(REQUIRED_PROPERTY, true);

            v.addAnnotation(AnnotationMarker.builder()
                    .ofField()
                    .withName(JsonProperty.class)
                    .withAttributes(attrs)
                    .build());
        });

        return filterRecursive(marker);
    }
}
