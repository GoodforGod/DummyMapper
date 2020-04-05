package io.goodforgod.dummymapper.util;

import com.intellij.lang.jvm.JvmModifier;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiType;
import com.intellij.psi.impl.source.PsiImmediateClassType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * ! NO DESCRIPTION !
 *
 * @author GoodforGod
 * @since 1.12.2019
 */
public class ClassUtils {

    private ClassUtils() {}

    private static final Map<String, Class> SIMPLE_FIELD_TYPES = getSimpleFieldTypes();

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

    private static Map<String, Class> getSimpleFieldTypes() {
        final List<Class<?>> simpleClasses = Stream.of(
                Enum.class,
                Object.class,
                String.class,
                Integer.class,
                Byte.class,
                Short.class,
                Integer.class,
                Long.class,
                Float.class,
                Double.class,
                Boolean.class,
                Character.class,
                byte.class,
                int.class,
                long.class,
                float.class,
                double.class,
                boolean.class,
                char.class,
                UUID.class,
                BigInteger.class,
                BigDecimal.class,
                Timestamp.class,
                Date.class,
                LocalDate.class,
                LocalTime.class,
                LocalDateTime.class)
                .collect(Collectors.toList());

        final Map<String, Class> map = new HashMap<>();
        simpleClasses.forEach(c -> {
            map.put(c.getName(), c);
            map.put(c.getSimpleName(), c);
        });

        return map;
    }
}
