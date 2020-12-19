package io.goodforgod.dummymapper.filter.impl;

import io.dummymaker.annotation.complex.GenEnum;
import io.goodforgod.dummymapper.model.AnnotationMarker;

import java.util.function.Predicate;

/**
 * @author GoodforGod
 * @since 15.07.2020
 */
public class GenEnumAnnotationFilter extends AnnotationFilter {

    @Override
    protected Predicate<AnnotationMarker> allowed() {
        return a -> !a.getName().equals(GenEnum.class.getName());
    }
}
