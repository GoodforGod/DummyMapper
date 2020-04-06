package io.goodforgod.dummymapper;

import io.goodforgod.dummymapper.model.EnumMarker;
import io.goodforgod.dummymapper.model.SimpleMarker;
import javassist.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Description in progress
 *
 * @author Anton Kurako (GoodforGod)
 * @since 5.4.2020
 */
public class ClassFactory {

    private static final ClassPool pool = ClassPool.getDefault();

    public static Optional<Class> build(Map<String, Object> map) {
        final String className = getClassName(map);
        try {
            return Optional.ofNullable(pool.getCtClass(className))
                    .map(c -> {
                        try {
                            return Class.forName(c.getName());
                        } catch (ClassNotFoundException e) {
                            throw new IllegalArgumentException(e);
                        }
                    });
        } catch (NotFoundException e) {
            final Map<String, CtClass> frostMap = new HashMap<>();
            return buildInternal(map, frostMap)
                    .flatMap(c -> {
                        try {
                            final Optional<Class> aClass = Optional.ofNullable(ClassPool.getDefault().toClass(c, ClassFactory.class.getClassLoader(), null));
                            aClass.ifPresent(a -> frostMap.forEach((k, v) -> v.defrost()));
                            return aClass;
                        } catch (CannotCompileException ex) {
                            ex.printStackTrace();
                            return Optional.empty();
                        }
                    });
        }


    }

    @SuppressWarnings("unchecked")
    private static Optional<CtClass> buildInternal(Map<String, Object> map, Map<String, CtClass> classMap) {
        try {
            final String className = getClassName(map);
            final CtClass ownClass = pool.makeClass(className);
            classMap.put(className, ownClass);

            for (Map.Entry<String, Object> entry : map.entrySet()) {
                if (entry.getValue() instanceof SimpleMarker) {
                    final CtField field = getSimpleField(entry.getKey(), (SimpleMarker) entry.getValue(), ownClass);
                    ownClass.addField(field);
                } else if (entry.getValue() instanceof EnumMarker) {

                } else if (entry.getValue() instanceof Map) {
                    final String fieldClassName = getClassName((Map<?, ?>) entry.getValue());
                    CtClass fieldClass = classMap.get(fieldClassName);
                    if (fieldClass == null) {
                        fieldClass = buildInternal((Map<String, Object>) entry.getValue(), classMap).orElse(null);
                        if (fieldClass != null) {
                            classMap.put(fieldClassName, fieldClass);
                        }
                    }

                    if (fieldClass != null) {
//                        ownClass.addField(new CtField(fieldClass, entry.getKey(), fieldClass));
                    }
                }
            }

            return Optional.ofNullable(ownClass);
        } catch (Exception e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }

    private static CtClass getCtClass(String className, ClassPool pool) {
        try {
            return pool.getCtClass(className);
        } catch (NotFoundException e) {
            return pool.makeClass(className);
        }
    }

    private static CtField getSimpleField(String fieldName, SimpleMarker marker, CtClass owner) {
        try {
            final String src = String.format("private %s %s;", marker.getType().getName(), fieldName);
            return CtField.make(src, owner);
        } catch (CannotCompileException e) {
            throw new IllegalArgumentException(e);
        }
    }

    private static String getClassName(Map<?, ?> map) {
        return map.values().stream()
                .filter(v -> v instanceof SimpleMarker)
                .map(v -> getClassNameFromPackage(((SimpleMarker) v).getSource()))
                .findFirst()
                .orElse("");
    }

    private static String getClassNameFromPackage(String source) {
        final int lastIndexOf = source.lastIndexOf('.', source.length() - 6);
        return source.substring(lastIndexOf + 1, source.length() - 5);
    }
}
