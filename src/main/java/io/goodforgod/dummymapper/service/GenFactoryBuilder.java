package io.goodforgod.dummymapper.service;

import io.dummymaker.annotation.special.GenAuto;
import io.dummymaker.factory.impl.GenFactory;
import io.dummymaker.generator.IGenerator;
import io.dummymaker.model.GenRule;
import io.dummymaker.model.GenRules;
import io.goodforgod.dummymapper.model.EnumMarker;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

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
                                   @NotNull Map<String, Object> scanned) {
        final GenRule rule = GenRule.auto(target, GenAuto.MAX);
        scanned.forEach((k, v) -> {
            if (v instanceof EnumMarker) {
                final IGenerator generator = () -> {
                    final List<String> values = ((EnumMarker) v).getValues();
                    return values.get(ThreadLocalRandom.current().nextInt(values.size()));
                };

                rule.add(generator, k);
            }
        });

        return new GenFactory(GenRules.of(rule));
    }
}
