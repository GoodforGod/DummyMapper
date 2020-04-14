package io.goodforgod.dummymapper;

import io.goodforgod.dummymapper.model.EnumMarker;
import io.goodforgod.dummymapper.model.SimpleMarker;
import javassist.*;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * Description in progress
 *
 * @author Anton Kurako (GoodforGod)
 * @since 5.4.2020
 */
public class ClassFactory {

    private static final ClassPool pool = ClassPool.getDefault();

    public static Optional<Class> build(@NotNull Map<String, Object> map) {
        if(map.isEmpty())
            return Optional.empty();

        final String className = getClassName(map);
        try {
            return Optional.of(Class.forName(className));
        } catch (ClassNotFoundException e) {
            final Map<String, CtClass> frostMap = new HashMap<>();
            return buildInternal(map, frostMap).flatMap(c -> {
                try {
                    return Optional.of(Class.forName(className));
                } catch (Exception ex) {
                    ex.printStackTrace();
                    return Optional.empty();
                }
            });
        }
    }

    @SuppressWarnings("unchecked")
    private static Optional<CtClass> buildInternal(@NotNull Map<String, Object> map,
                                                   @NotNull Map<String, CtClass> classMap) {
        final String className = getClassName(map);
        try {
            return Optional.of(pool.getCtClass(className));
        } catch (NotFoundException ex) {
            try {
                final CtClass ownClass = pool.makeClass(className);
                classMap.put(className, ownClass);

                for (Map.Entry<String, Object> entry : map.entrySet()) {
                    final String fieldName = entry.getKey();
                    if (entry.getValue() instanceof SimpleMarker) {
                        final CtField field = getSimpleField(fieldName, (SimpleMarker) entry.getValue(), ownClass);
                        ownClass.addField(field);
                    } else if (entry.getValue() instanceof EnumMarker) {
                        final CtField field = getEnumField(fieldName, (EnumMarker) entry.getValue(), ownClass);
                        ownClass.addField(field);
                    } else if (entry.getValue() instanceof Map) {
                        final String fieldClassName = getClassName((Map<?, ?>) entry.getValue());
                        CtClass fieldClass = classMap.get(fieldClassName);
                        if (fieldClass == null)
                            fieldClass = buildInternal((Map<String, Object>) entry.getValue(), classMap).orElse(null);

                        if (fieldClass != null)
                            ownClass.addField(getClassField(fieldName, fieldClass, ownClass));
                    }
                }

                return Optional.of(ClassPool.getDefault().toClass(ownClass, ClassFactory.class.getClassLoader(), null))
                        .map(c -> {
                            ownClass.defrost();
                            return ownClass;
                        });
            } catch (Exception e) {
                e.printStackTrace();
                return Optional.empty();
            }
        }
    }

    private static CtField getSimpleField(String fieldName, SimpleMarker marker, CtClass owner) {
        try {
            final String src = String.format("public %s %s;", marker.getType().getName(), fieldName);
            return CtField.make(src, owner);
        } catch (CannotCompileException e) {
            throw new IllegalArgumentException(e);
        }
    }

    private static CtField getEnumField(String fieldName, EnumMarker marker, CtClass owner) {
        try {
            final String src = String.format("public java.lang.String %s;", fieldName);
            return CtField.make(src, owner);
        } catch (CannotCompileException e) {
            throw new IllegalArgumentException(e);
        }
    }

    private static CtField getClassField(String fieldName, CtClass fieldClass, CtClass owner) {
        try {
            final String src = String.format("public %s %s;", fieldClass.getName(), fieldName);
            return CtField.make(src, owner);
        } catch (CannotCompileException e) {
            throw new IllegalArgumentException(e);
        }
    }

    private static String getClassName(@NotNull Map<?, ?> map) {
        return map.values().stream()
                .filter(v -> v instanceof SimpleMarker)
                .map(v -> getClassNameFromPackage(((SimpleMarker) v).getRoot()))
                .findFirst()
                .orElse("");
    }

    private static String getClassNameFromPackage(String source) {
        final int lastIndexOf = source.lastIndexOf('.', source.length() - 6);
        return source.substring(lastIndexOf + 1, source.length() - 5);
    }
}
