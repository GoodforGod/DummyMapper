package io.goodforgod.dummymapper.service;

import io.dummymaker.factory.impl.GenFactory;
import io.dummymaker.generator.IGenerator;
import io.dummymaker.model.GenRule;
import io.dummymaker.model.GenRules;
import io.dummymaker.util.CollectionUtils;
import io.goodforgod.dummymapper.marker.EnumMarker;
import io.goodforgod.dummymapper.marker.Marker;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * Description in progress
 *
 * @author Anton Kurako (GoodforGod)
 * @since 19.4.2020
 */
public class GenFactoryBuilder {

    /**
     * @param scanned data from JavaFileScanner
     * @return builds GenFactory based on scanned data from java file scanner
     * @see JavaFileScanner
     * @see ClassFactory
     */
    public static GenFactory build(@NotNull Class target,
                                   @NotNull Map<String, Marker> scanned) {
        final GenRule rule = GenRule.auto(target, 2);
        scanned.forEach((k, v) -> {
            if (v instanceof EnumMarker) {
                final IGenerator<String> generator = () -> CollectionUtils.random(((EnumMarker) v).getValues());
                rule.add(generator, k);
            }
        });

        return new GenFactory(GenRules.of(rule));
    }
}
