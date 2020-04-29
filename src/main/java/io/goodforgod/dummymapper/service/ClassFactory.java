package io.goodforgod.dummymapper.service;

import io.goodforgod.dummymapper.error.ClassBuildException;
import io.goodforgod.dummymapper.marker.*;
import javassist.*;
import javassist.bytecode.SignatureAttribute;
import org.jetbrains.annotations.NotNull;

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

    public static Map<String, String> getMappedClasses(@NotNull Map<String, Marker> scan) {
        final Map<String, String> scanned = new HashMap<>();
        scan.values().stream()
                .filter(m -> m instanceof RawMarker)
                .map(m -> getMappedClasses(((RawMarker) m).getStructure()))
                .forEach(scanned::putAll);

        scan.values().stream()
                .filter(m -> m instanceof CollectionMarker && ((CollectionMarker) m).isRaw())
                .map(m -> getMappedClasses(((RawMarker) ((CollectionMarker) m).getErasure()).getStructure()))
                .forEach(scanned::putAll);

        scan.values().stream()
                .filter(m -> m instanceof MapMarker && ((MapMarker) m).isRaw())
                .map(m -> ((MapMarker) m).getKeyErasure() instanceof RawMarker
                        ? getMappedClasses(((RawMarker) ((MapMarker) m).getKeyErasure()).getStructure())
                        : getMappedClasses(((RawMarker) ((MapMarker) m).getValueErasure()).getStructure()))
                .forEach(scanned::putAll);

        scan.values().stream()
                .filter(m -> m instanceof TypedMarker)
                .findFirst()
                .ifPresent(s -> {
                    final String className = getClassName(s);
                    final int num = Integer.parseInt(Character.valueOf(className.charAt(className.length() - 1)).toString());
                    final String actualClassName = className.substring(0, className.length() - 1) + (num - 1);
                    scanned.put(s.getRoot(), actualClassName);
                });

        return scanned;
    }

    public static Class build(@NotNull Map<String, Marker> scan) {
        if (scan.isEmpty())
            throw new IllegalArgumentException("Scanned map for Class construction is empty!");

        try {
            final CtClass ctClass = buildInternal(scan, new HashMap<>());
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
                                         @NotNull Map<String, CtClass> scanned) {
        try {
            final String className = getClassName(map);
            final CtClass ownClass = getOrCreateCtClass(className);
            scanned.put(className, ownClass);

            for (Map.Entry<String, Marker> entry : map.entrySet()) {
                final String fieldName = entry.getKey();
                if (entry.getValue() instanceof TypedMarker) {
                    final CtField field = getSimpleField(fieldName, (TypedMarker) entry.getValue(), ownClass);
                    ownClass.addField(field);
                } else if (entry.getValue() instanceof CollectionMarker) {
                    final Marker erasure = ((CollectionMarker) entry.getValue()).getErasure();
                    final Class<?> type = getErasureType(erasure, scanned);
                    final CtField field = getCollectionField(fieldName, (CollectionMarker) entry.getValue(), type, ownClass);
                    ownClass.addField(field);
                } else if (entry.getValue() instanceof MapMarker) {
                    final Marker keyErasure = ((MapMarker) entry.getValue()).getKeyErasure();
                    final Marker valueErasure = ((MapMarker) entry.getValue()).getValueErasure();
                    final Class<?> keyType = getErasureType(keyErasure, scanned);
                    final Class<?> valueType = getErasureType(valueErasure, scanned);
                    final CtField field = getMapField(fieldName, (MapMarker) entry.getValue(), keyType, valueType, ownClass);
                    ownClass.addField(field);
                } else if (entry.getValue() instanceof EnumMarker) {
                    final CtField field = getEnumField(fieldName, (EnumMarker) entry.getValue(), ownClass);
                    ownClass.addField(field);
                } else if (entry.getValue() instanceof RawMarker) {
                    final Map<String, Marker> structure = ((RawMarker) entry.getValue()).getStructure();
                    final String fieldClassName = getClassName(structure);
                    CtClass fieldClass = scanned.get(fieldClassName);
                    if (fieldClass == null)
                        fieldClass = buildInternal(structure, scanned);

                    ownClass.addField(getClassField(fieldName, fieldClass, ownClass));
                }
            }

            try {
                Class.forName(className);
                return ownClass;
            } catch (Exception e) {
                ownClass.toClass();
                return ownClass;
            }
        } catch (Exception e) {
            throw new ClassBuildException(e);
        }
    }

    private static Class<?> getErasureType(@NotNull Marker erasure,
                                           @NotNull Map<String, CtClass> scanned) {
        if (erasure instanceof TypedMarker) {
            return ((TypedMarker) erasure).getType();
        } else if (erasure instanceof EnumMarker) {
            return String.class;
        } else if (erasure instanceof RawMarker) {
            try {
                final CtClass internal = buildInternal(((RawMarker) erasure).getStructure(), scanned);
                return Class.forName(internal.getName());
            } catch (ClassNotFoundException e) {
                return String.class;
            }
        } else {
            return String.class;
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

    private static CtField getSimpleField(@NotNull String fieldName,
                                          @NotNull TypedMarker marker,
                                          @NotNull CtClass owner) {
        try {
            final String src = String.format("public %s %s;", marker.getType().getName(), fieldName);
            return CtField.make(src, owner);
        } catch (CannotCompileException e) {
            throw new ClassBuildException(e);
        }
    }

    private static CtField getCollectionField(@NotNull String fieldName,
                                              @NotNull CollectionMarker marker,
                                              @NotNull Class<?> erasure,
                                              @NotNull CtClass owner) {
        try {
            final String src = String.format("public %s %s;", marker.getType().getName(), fieldName);
            final SignatureAttribute.ClassType signature = new SignatureAttribute.ClassType(marker.getType().getName(),
                    new SignatureAttribute.TypeArgument[] {
                            new SignatureAttribute.TypeArgument(new SignatureAttribute.ClassType(erasure.getName())) });

            final CtField field = CtField.make(src, owner);
            field.setGenericSignature(signature.encode());
            return field;
        } catch (CannotCompileException e) {
            throw new ClassBuildException(e);
        }
    }

    private static CtField getMapField(@NotNull String fieldName,
                                       @NotNull MapMarker marker,
                                       @NotNull Class<?> keyErasure,
                                       @NotNull Class<?> valueErasure,
                                       @NotNull CtClass owner) {
        try {
            final String src = String.format("public %s %s;", marker.getType().getName(), fieldName);
            final SignatureAttribute.ClassType signature = new SignatureAttribute.ClassType(marker.getType().getName(),
                    new SignatureAttribute.TypeArgument[] {
                            new SignatureAttribute.TypeArgument(new SignatureAttribute.ClassType(keyErasure.getName())),
                            new SignatureAttribute.TypeArgument(new SignatureAttribute.ClassType(valueErasure.getName()))
                    });

            final CtField field = CtField.make(src, owner);
            field.setGenericSignature(signature.encode());
            return field;
        } catch (CannotCompileException e) {
            throw new ClassBuildException(e);
        }
    }

    private static CtField getEnumField(@NotNull String fieldName,
                                        @NotNull EnumMarker marker,
                                        @NotNull CtClass owner) {
        try {
            final String src = String.format("public java.lang.String %s;", fieldName);
            return CtField.make(src, owner);
        } catch (CannotCompileException e) {
            throw new ClassBuildException(e);
        }
    }

    private static CtField getClassField(@NotNull String fieldName,
                                         @NotNull CtClass fieldClass,
                                         @NotNull CtClass owner) {
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
                .map(m -> getClassName((Marker) m))
                .findFirst()
                .orElseThrow(() -> new ClassBuildException("Can not find Name while class construction!"));
    }

    private static String getClassName(@NotNull Marker marker) {
        final String name = getClassNameFromPackage(marker.getRoot());
        return getClassNameWithSuffix(name);
    }

    private static String getClassNameWithSuffix(@NotNull String name) {
        return name + "_" + CLASS_NAME_SUFFIX_COUNTER.computeIfAbsent(name, k -> 0);
    }

    private static String getClassNameFromPackage(@NotNull String source) {
        final int lastIndexOf = source.lastIndexOf('.', source.length() - 6);
        return source.substring(lastIndexOf + 1, source.length() - 5);
    }
}
