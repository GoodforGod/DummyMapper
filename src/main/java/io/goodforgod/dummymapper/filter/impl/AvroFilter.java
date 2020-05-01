package io.goodforgod.dummymapper.filter.impl;

import org.apache.avro.reflect.AvroIgnore;

import java.util.Collection;
import java.util.Collections;

/**
 * Filters out fields which are marked with {@link AvroIgnore}
 *
 * @author Anton Kurako (GoodforGod)
 * @since 1.5.2020
 */
public class AvroFilter extends AnnotationFilter {

    @Override
    protected Collection<Class<?>> getIgnoreAnnotations() {
        return Collections.singletonList(AvroIgnore.class);
    }
}
