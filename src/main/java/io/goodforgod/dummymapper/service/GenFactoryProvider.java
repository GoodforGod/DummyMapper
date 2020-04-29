package io.goodforgod.dummymapper.service;

import io.dummymaker.factory.impl.GenFactory;
import io.dummymaker.generator.IGenerator;
import io.dummymaker.model.GenRule;
import io.dummymaker.model.GenRules;
import io.dummymaker.util.CollectionUtils;
import io.goodforgod.dummymapper.marker.*;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Description in progress
 *
 * @author Anton Kurako (GoodforGod)
 * @since 19.4.2020
 */
public class GenFactoryProvider {

    /**
     * @param scanned data from JavaFileScanner
     * @return builds GenFactory based on scanned data from java file scanner
     * @see JavaFileScanner
     * @see ClassFactory
     */
    public static GenFactory get(@NotNull Map<String, Marker> scanned) {
        final Map<String, String> mappedClasses = ClassFactory.getMappedClasses(scanned);
        final List<GenRule> rules = getRules(scanned, mappedClasses);
        return new GenFactory(GenRules.of(rules));
    }

    private static List<GenRule> getRules(@NotNull Map<String, Marker> scanned,
                                          @NotNull Map<String, String> mappedClasses) {
        if (scanned.isEmpty())
            return Collections.emptyList();

        final String mapped = scanned.values().stream()
                .map(m -> mappedClasses.get(m.getRoot()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Class scanned but is not registered by ClassFactory"));

        try {
            final GenRule rule = GenRule.auto(Class.forName(mapped), 2);
            scanned.forEach((k, v) -> {
                if (v instanceof EnumMarker) {
                    final IGenerator<String> generator = () -> CollectionUtils.random(((EnumMarker) v).getValues());
                    rule.add(generator, k);
                }
            });

            final List<GenRule> rawRules = scanned.values().stream()
                    .filter(m -> m instanceof RawMarker)
                    .flatMap(m -> getRules(((RawMarker) m).getStructure(), mappedClasses).stream())
                    .collect(Collectors.toList());

            final List<GenRule> collectionRules = scanned.values().stream()
                    .filter(m -> m instanceof CollectionMarker && ((CollectionMarker) m).isRaw())
                    .flatMap(m -> getRules(((RawMarker) ((CollectionMarker) m).getErasure()).getStructure(), mappedClasses)
                            .stream())
                    .collect(Collectors.toList());

            final List<GenRule> mapRules = scanned.values().stream()
                    .filter(m -> m instanceof MapMarker && ((MapMarker) m).isRaw())
                    .flatMap(m -> ((MapMarker) m).getKeyErasure() instanceof RawMarker
                            ? getRules(((RawMarker) ((MapMarker) m).getKeyErasure()).getStructure(), mappedClasses).stream()
                            : getRules(((RawMarker) ((MapMarker) m).getValueErasure()).getStructure(), mappedClasses).stream())
                    .collect(Collectors.toList());

            return Stream.of(Collections.singletonList(rule), rawRules, collectionRules, mapRules)
                    .flatMap(Collection::stream)
                    .collect(Collectors.toList());
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException(e.getMessage(), e.getCause());
        }
    }
}
