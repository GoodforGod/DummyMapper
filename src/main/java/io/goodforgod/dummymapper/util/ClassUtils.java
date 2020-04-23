package io.goodforgod.dummymapper.util;

import com.intellij.lang.jvm.JvmModifier;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiType;
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
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Class utils for class scanner
 *
 * @author GoodforGod
 * @since 1.12.2019
 */
public class ClassUtils {

    private ClassUtils() {}

    private static final Map<String, Class> SIMPLE_FIELD_TYPES;

    static {
        SIMPLE_FIELD_TYPES = new HashMap<>(70);
        final List<Class<?>> simpleClasses = Stream.of(
                Enum.class,
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
                UUID.class)
                .collect(Collectors.toList());

        simpleClasses.forEach(c -> {
            SIMPLE_FIELD_TYPES.put(c.getName(), c);
            SIMPLE_FIELD_TYPES.put(c.getSimpleName(), c);
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
        return isTypeSimple(type.getCanonicalText());
    }

    //TODO add collection type scan
    public static boolean isTypeCollection(@NotNull PsiType type) {
        return false;
    }

    //TODO add map type scan
    public static boolean isTypeMap(@NotNull PsiType type) {
        return false;
    }

    public static boolean isTypeSimple(@Nullable String type) {
        return type != null && SIMPLE_FIELD_TYPES.containsKey(type);
    }

    public static boolean isFieldValid(@NotNull PsiField field) {
        return !field.hasModifier(JvmModifier.STATIC)
                && !field.hasModifier(JvmModifier.VOLATILE)
                && !field.hasModifier(JvmModifier.NATIVE)
                && !field.hasModifier(JvmModifier.STATIC)
                && !field.hasModifier(JvmModifier.SYNCHRONIZED)
                && !field.hasModifier(JvmModifier.TRANSITIVE);
    }

    public static Class getTypeByName(String name) {
        return SIMPLE_FIELD_TYPES.get(name);
    }
}
