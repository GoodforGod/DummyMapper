package io.goodforgod.dummymapper.util;

import com.intellij.lang.jvm.JvmModifier;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiType;
import com.intellij.psi.impl.source.PsiClassReferenceType;
import com.intellij.psi.impl.source.PsiImmediateClassType;
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
 * Class utils for class scanner
 *
 * @author GoodforGod
 * @since 1.12.2019
 */
public class ClassUtils {

    private ClassUtils() {
    }

    private static final Map<String, Class<?>> SIMPLE_FIELD_TYPES = new HashMap<>(70);
    private static final Map<String, Class<?>> COLLECTION_FIELD_TYPES = new HashMap<>(30);
    private static final Map<String, Class<?>> MAP_FIELD_TYPES = new HashMap<>(10);

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

    public static boolean isTypeSimple(@NotNull PsiType type) {
        return getSimpleTypeByName(type) != null;
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
                && !field.hasModifier(JvmModifier.NATIVE)
                && !field.hasModifier(JvmModifier.STATIC)
                && !field.hasModifier(JvmModifier.SYNCHRONIZED)
                && !field.hasModifier(JvmModifier.TRANSITIVE);
    }

    public static @Nullable Class<?> getSimpleTypeByName(@NotNull PsiType type) {
        return getSimpleTypeByName(type.getCanonicalText());
    }

    public static Class<?> getSimpleTypeByName(String name) {
        return SIMPLE_FIELD_TYPES.get(name);
    }
}
