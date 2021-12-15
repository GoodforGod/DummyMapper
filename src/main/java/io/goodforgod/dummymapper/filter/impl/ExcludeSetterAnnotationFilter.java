package io.goodforgod.dummymapper.filter.impl;


import io.goodforgod.dummymapper.model.AnnotationMarker;
import java.util.function.Predicate;


/**
 * @author GoodforGod
 * @since 15.07.2020
 */
public class ExcludeSetterAnnotationFilter extends AnnotationFilter {

    @Override
    protected Predicate<AnnotationMarker> allowed() {
        return a -> a.isFieldMarked() || a.isGetterMarked();
    }
}
