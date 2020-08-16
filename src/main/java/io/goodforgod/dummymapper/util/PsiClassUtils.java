package io.goodforgod.dummymapper.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.intellij.lang.jvm.JvmModifier;
import com.intellij.psi.PsiArrayType;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiType;
import com.intellij.psi.impl.source.PsiClassReferenceType;
import com.intellij.psi.impl.source.PsiImmediateClassType;
import io.dummymaker.util.StringUtils;
import io.goodforgod.dummymapper.scanner.impl.PsiJavaFileScanner;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Stream;

/**
 * Class utils for {@link PsiJavaFileScanner}
 *
 * @author GoodforGod
 * @since 1.12.2019
 */
public class PsiClassUtils {

    private PsiClassUtils() {}

    private static final Map<String, Class<?>> SIMPLE_FIELD_TYPES = new HashMap<>(70);
    private static final Map<String, Class<?>> COLLECTION_FIELD_TYPES = new HashMap<>(30);
    private static final Map<String, Class<?>> MAP_FIELD_TYPES = new HashMap<>(10);
    private static final Set<String> FORBIDDEN_TYPES = new HashSet<>(20);

    static {
        Stream.of(Enum.class,
                Object.class,
                Boolean.class,
                String.class,
                Character.class,
                Float.class,
                Double.class,
                Byte.class,
                Short.class,
                Integer.class,
                Long.class,
                byte.class,
                short.class,
                int.class,
                long.class,
                float.class,
                double.class,
                boolean.class,
                char.class,
                BigInteger.class,
                BigDecimal.class,
                LocalTime.class,
                LocalDate.class,
                LocalDateTime.class,
                Date.class,
                java.sql.Date.class,
                Time.class,
                Timestamp.class,
                UUID.class).forEach(c -> {
                    SIMPLE_FIELD_TYPES.put(c.getName(), c);
                    SIMPLE_FIELD_TYPES.put(c.getSimpleName(), c);
                });

        Stream.of(Iterable.class,
                Collection.class,
                List.class,
                Set.class,
                ArrayList.class,
                LinkedList.class,
                CopyOnWriteArrayList.class,
                SortedSet.class,
                NavigableSet.class,
                TreeSet.class,
                HashSet.class,
                LinkedHashSet.class).forEach(c -> {
                    COLLECTION_FIELD_TYPES.put(c.getName(), c);
                    COLLECTION_FIELD_TYPES.put(c.getSimpleName(), c);
                });

        Stream.of(Map.class,
                WeakHashMap.class,
                HashMap.class,
                SortedMap.class,
                Hashtable.class,
                NavigableMap.class,
                TreeMap.class,
                LinkedHashMap.class).forEach(c -> {
                    MAP_FIELD_TYPES.put(c.getName(), c);
                    MAP_FIELD_TYPES.put(c.getSimpleName(), c);
                });

        Stream.of(
                Class.class,
                ObjectMapper.class).forEach(c -> {
                    FORBIDDEN_TYPES.add(c.getName());
                    FORBIDDEN_TYPES.add(c.getSimpleName());
                });
    }

    public static boolean isTypeEnum(@NotNull PsiType type) {
        final PsiType[] superTypes = type.getSuperTypes();
        return superTypes.length > 0
                && superTypes[0] instanceof PsiImmediateClassType
                && "java.lang.Enum".equals(((PsiImmediateClassType) superTypes[0]).rawType().getCanonicalText());
    }

    public static boolean isTypeEnum(@Nullable String type) {
        return "java.lang.Enum".equals(type);
    }

    public static boolean isTypeForbidden(@NotNull PsiType type) {
        final String name = type.getCanonicalText();
        return name.startsWith("sun.reflect")
                || name.startsWith("java.lang")
                || FORBIDDEN_TYPES.contains(name);
    }

    public static boolean isTypeSimple(@NotNull PsiType type) {
        return getSimpleTypeByName(type) != null;
    }

    public static boolean isTypeArray(@NotNull PsiType type) {
        return type instanceof PsiArrayType;
    }

    public static boolean isTypeCollection(@NotNull PsiType type) {
        return getCollectionType(type) != null;
    }

    public static @Nullable Class<?> getCollectionType(@NotNull PsiType type) {
        if (!(type instanceof PsiClassReferenceType))
            return null;

        return COLLECTION_FIELD_TYPES.get(((PsiClassReferenceType) type).rawType().getCanonicalText());
    }

    public static boolean isTypeMap(@NotNull PsiType type) {
        return getMapType(type) != null;
    }

    public static @Nullable Class<?> getMapType(@NotNull PsiType type) {
        if (!(type instanceof PsiClassReferenceType))
            return null;

        return MAP_FIELD_TYPES.get(((PsiClassReferenceType) type).rawType().getCanonicalText());
    }

    public static boolean isTypeSimple(@Nullable String type) {
        return getSimpleTypeByName(type) != null;
    }

    public static boolean isFieldValid(@NotNull PsiField field) {
        return !field.hasModifier(JvmModifier.STATIC)
                && !field.hasModifier(JvmModifier.VOLATILE)
                && !field.hasModifier(JvmModifier.SYNCHRONIZED)
                && !field.hasModifier(JvmModifier.NATIVE)
                && !field.hasModifier(JvmModifier.TRANSIENT)
                && !field.hasModifier(JvmModifier.TRANSITIVE);
    }

    public static @Nullable Class<?> getSimpleTypeByName(@NotNull PsiType type) {
        return getSimpleTypeByName(type.getCanonicalText());
    }

    public static @Nullable Class<?> getSimpleTypeByName(@Nullable String name) {
        if (StringUtils.isEmpty(name))
            return null;

        return "?".equals(name)
                ? String.class
                : SIMPLE_FIELD_TYPES.get(name);
    }
}
