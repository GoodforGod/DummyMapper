package io.goodforgod.dummymapper.filter.impl;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.Collection;
import java.util.Collections;

/**
 * Filters out fields which are marked with {@link JsonIgnore}
 *
 * @author Anton Kurako (GoodforGod)
 * @since 1.5.2020
 */
public class JacksonGetFilter extends AnnotationFilter {

    @Override
    protected Collection<Class<?>> getIgnoreAnnotations() {
        return Collections.singletonList(JsonIgnore.class);
    }
}
