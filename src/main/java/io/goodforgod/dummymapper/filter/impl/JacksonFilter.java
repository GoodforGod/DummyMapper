package io.goodforgod.dummymapper.filter.impl;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.Collection;
import java.util.Collections;

/**
 * Filters out fields, getters, setters which are marked with {@link JsonIgnore}
 *
 * @author Anton Kurako (GoodforGod)
 * @since 1.5.2020
 */
public class JacksonFilter extends AnnotationFilter {

    private final boolean isSettersFiltered;
    private final boolean isGettersFiltered;

    public JacksonFilter(boolean isSettersFiltered, boolean isGettersFiltered) {
        this.isSettersFiltered = isSettersFiltered;
        this.isGettersFiltered = isGettersFiltered;
    }

    @Override
    protected Collection<Class<?>> getIgnoreAnnotations() {
        return Collections.singletonList(JsonIgnore.class);
    }

    @Override
    protected boolean filterSetters() {
        return isSettersFiltered;
    }

    @Override
    protected boolean filterGetters() {
        return isGettersFiltered;
    }
}
