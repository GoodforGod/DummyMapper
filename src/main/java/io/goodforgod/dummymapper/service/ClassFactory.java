package io.goodforgod.dummymapper.service;

import io.goodforgod.dummymapper.error.ClassBuildException;
import io.goodforgod.dummymapper.model.EnumMarker;
import io.goodforgod.dummymapper.model.Marker;
import io.goodforgod.dummymapper.model.RawMarker;
import io.goodforgod.dummymapper.model.TypedMarker;
import javassist.*;
import javassist.bytecode.SignatureAttribute;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Class factory that creates Java Class from recreated java class map
 *
 * @author Anton Kurako (GoodforGod)
 * @see JavaFileScanner
 * @since 5.4.2020
 */
public class ClassFactory {

    private static final ClassPool CLASS_POOL = ClassPool.getDefault();
    // TODO create own classloader that could be GC so old classes can be unloaded from memory
    private static final Map<String, Integer> CLASS_NAME_SUFFIX_COUNTER = new HashMap<>();

    public static Class build(@NotNull Map<String, Marker> map) {
        if (map.isEmpty())
            throw new IllegalArgumentException("Scanned map for Class construction is empty!");

        try {
            final Map<String, CtClass> frostMap = new HashMap<>();
            final CtClass ctClass = buildInternal(map, frostMap);
            for (String key : CLASS_NAME_SUFFIX_COUNTER.keySet()) {
                final Integer counter = CLASS_NAME_SUFFIX_COUNTER.get(key);
                CLASS_NAME_SUFFIX_COUNTER.put(key, counter + 1);
            }

            return Class.forName(ctClass.getName());
        } catch (Exception e) {
            throw new ClassBuildException(e);
        }
    }

    private static CtClass buildInternal(@NotNull Map<String, Marker> map,
                                         @NotNull Map<String, CtClass> classMap) {
        try {
            final String className = getClassName(map);
            final CtClass ownClass = getOrCreateCtClass(className);
            classMap.put(className, ownClass);

            for (Map.Entry<String, Marker> entry : map.entrySet()) {
                final String fieldName = entry.getKey();
                if (entry.getValue() instanceof TypedMarker) {
                    final CtField field = getSimpleField(fieldName, (TypedMarker) entry.getValue(), ownClass);
                    ownClass.addField(field);
                } else if (entry.getValue() instanceof EnumMarker) {
                    final CtField field = getEnumField(fieldName, (EnumMarker) entry.getValue(), ownClass);
                    ownClass.addField(field);
                } else if (entry.getValue() instanceof RawMarker) {
                    final Map<String, Marker> structure = ((RawMarker) entry.getValue()).getStructure();
                    final String fieldClassName = getClassName(structure);
                    CtClass fieldClass = classMap.get(fieldClassName);
                    if (fieldClass == null)
                        fieldClass = buildInternal(structure, classMap);

                    ownClass.addField(getClassField(fieldName, fieldClass, ownClass));
                }
            }

            try {
                Class.forName(className);
                return ownClass;
            } catch (Exception e) {
                // ownClass.toClass(ClassFactory.class.getClassLoader(), null);
                ownClass.toClass();
                return ownClass;
            }
        } catch (Exception e) {
            throw new ClassBuildException(e);
        }
    }

    private static CtClass getOrCreateCtClass(@NotNull String className) {
        try {
            // Clean previously used class
            final CtClass ctClass = CLASS_POOL.get(className);
            ctClass.defrost();
            for (CtField field : ctClass.getFields())
                ctClass.removeField(field);

            return ctClass;
        } catch (NotFoundException ex) {
            return CLASS_POOL.makeClass(className);
        }
    }

    private static CtField getSimpleField(String fieldName, TypedMarker marker, CtClass owner) {
        try {
            final String src = String.format("public %s %s;", marker.getType().getName(), fieldName);
            return CtField.make(src, owner);
        } catch (CannotCompileException e) {
            throw new ClassBuildException(e);
        }
    }

    private static CtField getEnumField(String fieldName, EnumMarker marker, CtClass owner) {
        try {
            final String signature = new SignatureAttribute.ClassType(Collection.class.getName(),
                    new SignatureAttribute.TypeArgument[] {
                            new SignatureAttribute.TypeArgument(new SignatureAttribute.ClassType(String.class.getName())) })
                                    .encode();

            final String src = String.format("public java.lang.String %s;", fieldName);
            return CtField.make(src, owner);
        } catch (CannotCompileException e) {
            throw new ClassBuildException(e);
        }
    }

    private static CtField getClassField(String fieldName, CtClass fieldClass, CtClass owner) {
        try {
            final String src = String.format("public %s %s;", fieldClass.getName(), fieldName);
            return CtField.make(src, owner);
        } catch (CannotCompileException e) {
            throw new ClassBuildException(e);
        }
    }

    private static String getClassName(@NotNull Map<?, ?> map) {
        return map.values().stream()
                .filter(v -> v instanceof TypedMarker)
                .map(v -> getClassNameFromPackage(((TypedMarker) v).getRoot()))
                .map(name -> name + "_" + CLASS_NAME_SUFFIX_COUNTER.computeIfAbsent(name, k -> 0))
                .findFirst()
                .orElseThrow(() -> new ClassBuildException("Can not find Name while class construction!"));
    }

    private static String getClassNameFromPackage(String source) {
        final int lastIndexOf = source.lastIndexOf('.', source.length() - 6);
        return source.substring(lastIndexOf + 1, source.length() - 5);
    }
}
