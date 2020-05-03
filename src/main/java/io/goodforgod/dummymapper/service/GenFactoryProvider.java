package io.goodforgod.dummymapper.service;

import io.dummymaker.factory.impl.GenFactory;
import io.dummymaker.generator.IGenerator;
import io.dummymaker.model.GenRule;
import io.dummymaker.model.GenRules;
import io.dummymaker.util.CollectionUtils;
import io.goodforgod.dummymapper.marker.EnumMarker;
import io.goodforgod.dummymapper.marker.Marker;
import io.goodforgod.dummymapper.marker.RawMarker;
import io.goodforgod.dummymapper.util.MarkerUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * GenFactory Provider that builds GenFactory with special generators for ENUM or other complex values
 *
 * @author Anton Kurako (GoodforGod)
 * @since 19.4.2020
 */
public class GenFactoryProvider {

    private static final Logger logger = LoggerFactory.getLogger(GenFactoryProvider.class);

    private GenFactoryProvider() {}

    /**
     * @param scanned data from JavaFileScanner
     * @return builds GenFactory based on scanned data from java file scanner
     * @see PsiJavaFileScanner
     * @see ClassFactory
     */
    public static GenFactory get(@NotNull Map<String, Marker> scanned) {
        final Map<String, String> mappedClasses = ClassFactory.getMappedClasses(scanned);
        final List<GenRule> rules = getRules(scanned, mappedClasses);
        return new GenFactory(GenRules.of(rules));
    }

    private static List<GenRule> getRules(@NotNull Map<String, Marker> structure,
                                          @NotNull Map<String, String> mappedClasses) {
        if (structure.isEmpty())
            return Collections.emptyList();

        final String mapped = structure.values().stream()
                .map(m -> mappedClasses.get(m.getRoot()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Class scanned but is not registered by ClassFactory"));

        try {
            final GenRule rule = GenRule.auto(Class.forName(mapped), 2);
            structure.forEach((k, v) -> {
                if (v instanceof EnumMarker) {
                    final IGenerator<String> generator = () -> CollectionUtils.random(((EnumMarker) v).getValues());
                    rule.add(generator, k);
                }
            });

            final List<GenRule> rawRules = MarkerUtils.streamRawMarkers(structure)
                    .flatMap(m -> getRules(m.getStructure(), mappedClasses).stream())
                    .collect(Collectors.toList());

            final List<GenRule> collectionRules = MarkerUtils.streamCollectionRawMarkers(structure)
                    .flatMap(m -> getRules(((RawMarker) m.getErasure()).getStructure(), mappedClasses).stream())
                    .collect(Collectors.toList());

            final List<GenRule> mapRules = MarkerUtils.streamMapRawMarkers(structure)
                    .flatMap(m -> {
                        final Stream<GenRule> stream1 = m.getKeyErasure() instanceof RawMarker
                                ? getRules(((RawMarker) m.getKeyErasure()).getStructure(), mappedClasses).stream()
                                : Stream.empty();

                        final Stream<GenRule> stream2 = m.getValueErasure() instanceof RawMarker
                                ? getRules(((RawMarker) m.getValueErasure()).getStructure(), mappedClasses).stream()
                                : Stream.empty();

                        return Stream.concat(stream1, stream2);
                    })
                    .collect(Collectors.toList());

            return Stream.of(Collections.singletonList(rule), rawRules, collectionRules, mapRules)
                    .flatMap(Collection::stream)
                    .collect(Collectors.toList());
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException(e.getMessage(), e);
        }
    }
}
