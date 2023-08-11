package io.goodforgod.dummymapper.marker.filter;

import io.goodforgod.dummymaker.annotation.parameterized.GenEnum;
import io.goodforgod.dummymapper.marker.AnnotationMarker;
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
