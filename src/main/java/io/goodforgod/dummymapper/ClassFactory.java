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
                    ownClass.addField(CtField.make(String.format("") entry.getKey(), ));
                } else if (entry.getValue() instanceof EnumMarker) {

                } else if (entry.getValue() instanceof Map) {
                    final String fieldClassName = getClassName((Map<?, ?>) entry.getValue());
                    final CtClass fieldClass = classMap.computeIfAbsent(fieldClassName, (k) -> buildInternal((Map<String, Object>) entry.getValue(), classMap).orElse(null));
                    if (fieldClass != null)
                        ownClass.addField(CtField.make(entry.getKey(), fieldClass));
                }
            }

            return Optional.ofNullable(ownClass);
        } catch (CannotCompileException e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }

    public static String getClassName(Map<?, ?> map) {
        return map.values().stream()
                .filter(v -> v instanceof SimpleMarker)
                .map(v -> ((SimpleMarker) v).getSource().substring(((SimpleMarker) v).getSource().lastIndexOf('.')))
                .findFirst()
                .orElse("");
    }
}
