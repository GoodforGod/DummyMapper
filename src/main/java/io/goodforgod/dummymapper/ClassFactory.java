package io.goodforgod.dummymapper;

import io.goodforgod.dummymapper.model.EnumMarker;
import io.goodforgod.dummymapper.model.SimpleMarker;
import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtField;

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

    public static Optional<CtClass> build(Map<String, Object> map) {
        return buildInternal(map, new HashMap<>());
    }

    @SuppressWarnings("unchecked")
    private static Optional<CtClass> buildInternal(Map<String, Object> map, Map<String, CtClass> classMap) {
        try {
            final ClassPool pool = ClassPool.getDefault();

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
                    if(fieldClass == null) {
                        fieldClass = buildInternal((Map<String, Object>) entry.getValue(), classMap).orElse(null);
                        classMap.put(fieldClassName, fieldClass);
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
