package io.goodforgod.dummymapper.marker.filter;

import com.fasterxml.jackson.annotation.*;
import io.goodforgod.dummymapper.marker.AnnotationMarker;
import java.lang.annotation.RetentionPolicy;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.avro.reflect.*;

/**
 * Filters all unsupported {@link AnnotationMarker} from
 * {@link io.goodforgod.dummymapper.marker.Marker}
 * All supported annotations are {@link RetentionPolicy#RUNTIME} only
 *
 * @author Anton Kurako (GoodforGod)
 * @since 2.5.2020
 */
public class SupportedAnnotationFilter extends AnnotationFilter {

    private final Set<String> names = Stream.of(JsonProperty.class,
            JsonIgnore.class,
            JsonFormat.class,
            JsonAnySetter.class,
            JsonAnyGetter.class,
            JsonPropertyOrder.class,
            AvroIgnore.class,
            AvroDefault.class,
            AvroDoc.class,
            AvroMeta.class,
            AvroName.class,
            AvroSchema.class,
            Nullable.class,
            Stringable.class)
            .map(Class::getName)
            .collect(Collectors.toSet());

    @Override
    protected Predicate<AnnotationMarker> allowed() {
        return a -> names.contains(a.getName());
    }
}
