package io.goodforgod.dummymapper.service;

import io.goodforgod.dummymaker.GenFactory;
import io.goodforgod.dummymaker.GenRule;
import io.goodforgod.dummymaker.generator.Generator;
import io.goodforgod.dummymaker.util.CollectionUtils;
import io.goodforgod.dummymaker.util.RandomUtils;
import io.goodforgod.dummymaker.util.StringUtils;
import io.goodforgod.dummymapper.marker.*;
import io.goodforgod.dummymapper.marker.AnnotationMarker;
import io.goodforgod.dummymapper.util.MarkerUtils;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.jetbrains.annotations.NotNull;

/**
 * GenFactory Provider that builds GenFactory with special generators for ENUM or other complex
 * values
 *
 * @author Anton Kurako (GoodforGod)
 * @since 19.4.2020
 */
public class GenFactoryProvider {

    private static final int FACTORY_DEPTH = 16;
    private static final String VISITED = "_rules_visited";
    private static final Predicate<RawMarker> IS_VISITED = m -> m.getAnnotations().stream()
            .filter(AnnotationMarker::isInternal)
            .anyMatch(a -> a.getName().equals(VISITED));

    private GenFactoryProvider() {}

    /**
     * @param rawMarker data from JavaFileScanner
     * @return builds GenFactory based on scanned data from java file scanner
     * @see PsiClassScanner
     * @see AssistClassFactory
     */
    public static GenFactory get(@NotNull RawMarker rawMarker) {
        final Map<String, String> mappedClasses = AssistClassFactory.getMappedClasses(rawMarker);
        final List<GenRule> rules = getRules(rawMarker, mappedClasses);
        GenFactory.Builder builder = GenFactory.builder();
        rules.forEach(builder::addRule);
        return builder.build();
    }

    private static List<GenRule> getRules(@NotNull RawMarker marker,
                                          @NotNull Map<String, String> mappedClasses) {
        if (marker.isEmpty())
            return Collections.emptyList();

        if (IS_VISITED.test(marker))
            return Collections.emptyList();

        marker.addAnnotation(AnnotationMarker.builder().ofInternal().withName(VISITED).build());

        final Map<String, Marker> structure = marker.getStructure();
        final Optional<String> mapped = structure.values().stream()
                .map(m -> mappedClasses.get(m.getRoot()))
                .filter(StringUtils::isNotEmpty)
                .findFirst();

        if (mapped.isEmpty()) {
            return Collections.emptyList();
        }

        try {
            final GenRule rule = GenRule.ofClass(Class.forName(mapped.get()), true, FACTORY_DEPTH);
            structure.forEach((k, v) -> {
                if (v instanceof EnumMarker) {
                    final Generator<String> generator = () -> CollectionUtils.random(((EnumMarker) v).getValues());
                    rule.generateForNames(k, () -> generator);
                } else if (v instanceof CollectionMarker && ((CollectionMarker) v).getErasure() instanceof EnumMarker) {
                    final EnumMarker erasure = (EnumMarker) ((CollectionMarker) v).getErasure();
                    final int total = RandomUtils.random(1, erasure.getValues().size());
                    final Generator<Collection<String>> generator = () -> IntStream.range(0, total)
                            .mapToObj(i -> erasure.getValues().get(i))
                            .collect(Collectors.toCollection(() -> Set.class.isAssignableFrom(((CollectionMarker) v).getType())
                                    ? new HashSet<>()
                                    : new ArrayList<>()));
                    rule.generateForNames(k, () -> generator);
                } else if (v instanceof ArrayMarker && ((ArrayMarker) v).getErasure() instanceof EnumMarker) {
                    final EnumMarker erasure = (EnumMarker) ((ArrayMarker) v).getErasure();
                    final int total = RandomUtils.random(1, erasure.getValues().size());
                    final Generator<String[]> generator = () -> IntStream.range(0, total)
                            .mapToObj(i -> erasure.getValues().get(i))
                            .toArray(String[]::new);
                    rule.generateForNames(k, () -> generator);
                }
            });

            final List<GenRule> rawRules = MarkerUtils.streamRawMarkers(structure)
                    .flatMap(m -> getRules(m, mappedClasses).stream())
                    .collect(Collectors.toList());

            final List<GenRule> arrayRules = MarkerUtils.streamArrayRawMarkers(structure)
                    .flatMap(m -> getRules(((RawMarker) m.getErasure()), mappedClasses).stream())
                    .collect(Collectors.toList());

            final List<GenRule> collectionRules = MarkerUtils.streamCollectionRawMarkers(structure)
                    .flatMap(m -> getRules(((RawMarker) m.getErasure()), mappedClasses).stream())
                    .collect(Collectors.toList());

            final List<GenRule> mapRules = MarkerUtils.streamMapRawMarkers(structure)
                    .flatMap(m -> {
                        final Stream<GenRule> stream1 = m.getKeyErasure() instanceof RawMarker
                                ? getRules(((RawMarker) m.getKeyErasure()), mappedClasses).stream()
                                : Stream.empty();

                        final Stream<GenRule> stream2 = m.getValueErasure() instanceof RawMarker
                                ? getRules(((RawMarker) m.getValueErasure()), mappedClasses).stream()
                                : Stream.empty();

                        return Stream.concat(stream1, stream2);
                    })
                    .collect(Collectors.toList());

            return Stream.of(Collections.singletonList(rule), rawRules, arrayRules, collectionRules, mapRules)
                    .flatMap(Collection::stream)
                    .collect(Collectors.toList());
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException(e.getMessage(), e);
        }
    }
}
