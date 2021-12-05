package io.goodforgod.dummymapper.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.dummymaker.util.CollectionUtils;
import io.goodforgod.dummymapper.error.ClassBuildException;
import io.goodforgod.dummymapper.error.ClassEmptyException;
import io.goodforgod.dummymapper.marker.*;
import io.goodforgod.dummymapper.model.AnnotationMarker;
import io.goodforgod.dummymapper.model.AnnotationMarkerBuilder;
import io.goodforgod.dummymapper.scanner.impl.PsiJavaFileScanner;
import io.goodforgod.dummymapper.util.MarkerUtils;
import javassist.*;
import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.ConstPool;
import javassist.bytecode.FieldInfo;
import javassist.bytecode.SignatureAttribute;
import javassist.bytecode.annotation.*;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Predicate;

/**
 * Class factory that creates Java Class from recreated java class map
 *
 * @author Anton Kurako (GoodforGod)
 * @see PsiJavaFileScanner
 * @since 5.4.2020
 */
public class ClassFactory {

    private static final ClassPool CLASS_POOL = ClassPool.getDefault();

    // TODO create own classloader that could be GC so old classes can be unloaded from memory
    private static final Map<String, Integer> CLASS_SUFFIX_COUNTER = new HashMap<>();

    private static final String MAPPED_VISITED = "_class_mapped_visited";
    private static final Predicate<RawMarker> IS_VISITED = m -> m.getAnnotations().stream()
            .filter(AnnotationMarker::isInternal)
            .anyMatch(a -> a.getName().equals(MAPPED_VISITED));

    private ClassFactory() {}

    // /**
    // * Contains class cache via className and its hash for structure
    // */
    // private static final Map<String, Integer> CLASS_CACHE = new HashMap<>();

    public static Map<String, String> getMappedClasses(@NotNull RawMarker marker) {
        if (IS_VISITED.test(marker))
            return Collections.emptyMap();

        marker.addAnnotation(AnnotationMarkerBuilder.get().ofInternal().withName(MAPPED_VISITED).build());

        final Map<String, String> mapped = new HashMap<>();
        final Map<String, Marker> structure = marker.getStructure();

        MarkerUtils.streamRawMarkers(structure)
                .filter(m -> !m.isEmpty())
                .map(ClassFactory::getMappedClasses)
                .forEach(mapped::putAll);

        MarkerUtils.streamArrayRawMarkers(structure)
                .filter(m -> !m.isEmpty())
                .map(m -> getMappedClasses(((RawMarker) m.getErasure())))
                .forEach(mapped::putAll);

        MarkerUtils.streamCollectionRawMarkers(structure)
                .filter(m -> !m.isEmpty())
                .map(m -> getMappedClasses(((RawMarker) m.getErasure())))
                .forEach(mapped::putAll);

        MarkerUtils.streamMapRawMarkers(structure)
                .filter(m -> !m.isEmpty())
                .map(m -> {
                    final Map<String, String> mapped1 = m.getKeyErasure() instanceof RawMarker
                            ? getMappedClasses(((RawMarker) m.getKeyErasure()))
                            : new HashMap<>(1);

                    final Map<String, String> mapped2 = m.getKeyErasure() instanceof RawMarker
                            ? getMappedClasses(((RawMarker) m.getKeyErasure()))
                            : Collections.emptyMap();

                    mapped1.putAll(mapped2);
                    return mapped1;
                })
                .forEach(mapped::putAll);

        structure.values().stream()
                .filter(m -> m instanceof TypedMarker || m instanceof RawMarker || m instanceof EnumMarker)
                .findFirst()
                .ifPresent(m -> {
                    final String currentClassName = getPrevClassName(m);
                    mapped.put(m.getRoot(), currentClassName);
                });

        return mapped;
    }

    public static Class<?> build(@NotNull RawMarker rawMarker) {
        if (rawMarker.isEmpty())
            throw new ClassEmptyException();

        try {
            final CtClass ctClass = buildInternal(rawMarker, new HashMap<>());
            return Class.forName(ctClass.getName());
        } catch (ClassBuildException e) {
            throw e;
        } catch (Exception e) {
            throw new ClassBuildException(e);
        }
    }

    private static CtClass buildInternal(@NotNull RawMarker classMarker,
                                         @NotNull Map<String, CtClass> scanned) {
        final Map<String, Marker> structure = classMarker.getStructure();
        final String className = getClassName(classMarker);
        final String originClassName = getSourceClassName(classMarker);

        // final int structureHash = structure.hashCode();
        // final Integer hash = CLASS_CACHE.computeIfAbsent(originClassName, k -> -1);
        // if (hash.equals(structureHash)) {
        // final String prevClassName = getPrevClassName(structure);
        // logger.debug("Retrieving CACHED class '{}' with generated name '{}' and structure hash '{}'",
        // originClassName, prevClassName, structureHash);
        // return CLASS_POOL.get(prevClassName);
        // }
        // logger.debug("CACHING class with name '{}' and structure hash '{}'", originClassName, structureHash);
        // CLASS_CACHE.put(originClassName, structureHash);

        final CtClass ownClass = getOrCreateCtClass(className);
        scanned.put(originClassName, ownClass);

        try {
            for (Map.Entry<String, Marker> entry : structure.entrySet()) {
                final Marker fieldMarker = entry.getValue();
                if (fieldMarker.isEmpty())
                    continue;

                final String fieldName = entry.getKey();
                if (fieldMarker instanceof ArrayMarker) {
                    final Marker erasure = ((ArrayMarker) fieldMarker).getErasure();
                    final Class<?> type = getErasureType(erasure, scanned);
                    final CtField field = getArrayField(fieldName, (ArrayMarker) fieldMarker, type, ownClass);
                    ownClass.addField(field);
                } else if (fieldMarker instanceof CollectionMarker) {
                    final Marker erasure = ((CollectionMarker) fieldMarker).getErasure();
                    final Class<?> type = getErasureType(erasure, scanned);
                    final CtField field = getCollectionField(fieldName, (CollectionMarker) fieldMarker, type, ownClass);
                    ownClass.addField(field);
                } else if (fieldMarker instanceof MapMarker) {
                    final Marker keyErasure = ((MapMarker) fieldMarker).getKeyErasure();
                    final Marker valueErasure = ((MapMarker) fieldMarker).getValueErasure();
                    final Class<?> keyType = getErasureType(keyErasure, scanned);
                    final Class<?> valueType = getErasureType(valueErasure, scanned);
                    final CtField field = getMapField(fieldName, (MapMarker) fieldMarker, keyType, valueType, ownClass);
                    ownClass.addField(field);
                } else if (fieldMarker instanceof TypedMarker) {
                    final CtField field = getTypedField(fieldName, (TypedMarker) fieldMarker, ownClass);
                    ownClass.addField(field);
                } else if (fieldMarker instanceof EnumMarker) {
                    final CtField field = getEnumField(fieldName, (EnumMarker) fieldMarker, ownClass);
                    ownClass.addField(field);
                } else if (fieldMarker instanceof RawMarker) {
                    final String innerClassName = getSourceClassName((RawMarker) fieldMarker);
                    CtClass innerClass = scanned.get(innerClassName);
                    if (innerClass == null)
                        innerClass = buildInternal((RawMarker) fieldMarker, scanned);

                    final CtField field = getClassField(fieldName, innerClass, ownClass, (RawMarker) fieldMarker);
                    ownClass.addField(field);
                }
            }

            try {
                Class.forName(className);
                CLASS_SUFFIX_COUNTER.computeIfPresent(originClassName, (k, v) -> v + 1);
                return ownClass;
            } catch (Exception e) {
                ownClass.toClass(ObjectMapper.class.getClassLoader(), null);
                CLASS_SUFFIX_COUNTER.computeIfPresent(originClassName, (k, v) -> v + 1);
                return ownClass;
            }
        } catch (ClassBuildException e) {
            throw e;
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
                final String className = getSourceClassName((RawMarker) erasure);
                CtClass internal = scanned.get(className);
                if (internal == null)
                    internal = buildInternal((RawMarker) erasure, scanned);

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
            // Clean previously used class (actually doesn't work such way)
            final CtClass ctClass = CLASS_POOL.get(className);
            ctClass.defrost();
            for (CtField field : ctClass.getFields())
                ctClass.removeField(field);

            return ctClass;
        } catch (NotFoundException ex) {
            return CLASS_POOL.makeClass(className);
        }
    }

    private static CtField getTypedField(@NotNull String fieldName,
                                         @NotNull TypedMarker marker,
                                         @NotNull CtClass owner) {
        try {
            final String src = String.format("public %s %s;", marker.getType().getName(), fieldName);
            final CtField field = CtField.make(src, owner);
            return addAnnotationInfo(field, marker);
        } catch (CannotCompileException e) {
            throw new ClassBuildException(e);
        }
    }

    private static CtField getArrayField(@NotNull String fieldName,
                                         @NotNull ArrayMarker marker,
                                         @NotNull Class<?> erasure,
                                         @NotNull CtClass owner) {
        try {
            final String src = String.format("public %s[] %s;", erasure.getName(), fieldName);
            final CtField field = CtField.make(src, owner);
            return addAnnotationInfo(field, marker);
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
            return addAnnotationInfo(field, marker);
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
            return addAnnotationInfo(field, marker);
        } catch (CannotCompileException e) {
            throw new ClassBuildException(e);
        }
    }

    private static CtField getEnumField(@NotNull String fieldName,
                                        @NotNull EnumMarker marker,
                                        @NotNull CtClass owner) {
        try {
            final String src = String.format("public java.lang.String %s;", fieldName);
            final CtField field = CtField.make(src, owner);
            return addAnnotationInfo(field, marker);
        } catch (CannotCompileException e) {
            throw new ClassBuildException(e);
        }
    }

    private static CtField getClassField(@NotNull String fieldName,
                                         @NotNull CtClass fieldClass,
                                         @NotNull CtClass owner,
                                         @NotNull RawMarker marker) {
        try {
            final String src = String.format("public %s %s;", fieldClass.getName(), fieldName);
            final CtField field = CtField.make(src, owner);
            return addAnnotationInfo(field, marker);
        } catch (CannotCompileException e) {
            throw new ClassBuildException(e);
        }
    }

    private static CtField addAnnotationInfo(@NotNull CtField field,
                                             @NotNull Marker marker) {
        if (marker.getAnnotations().isEmpty())
            return field;

        final FieldInfo fieldInfo = field.getFieldInfo();
        final ConstPool constPool = fieldInfo.getConstPool();

        final AnnotationsAttribute attribute = new AnnotationsAttribute(constPool, AnnotationsAttribute.visibleTag);
        for (AnnotationMarker annotationMarker : marker.getAnnotations()) {
            if (annotationMarker.isInternal())
                continue;

            final Annotation a = new Annotation(annotationMarker.getName(), constPool);
            annotationMarker.getAttributes()
                    .forEach((n, v) -> getMember(v, constPool)
                            .ifPresent(member -> a.addMemberValue(n, member)));
            try {
                attribute.addAnnotation(a);
            } catch (Exception e) {
                // nothing we can do if javaassist fail
            }
        }

        if (CollectionUtils.isNotEmpty(attribute.getAnnotations()))
            fieldInfo.addAttribute(attribute);

        return field;
    }

    private static Optional<MemberValue> getMember(Object v, ConstPool constPool) {
        if (v instanceof Boolean) {
            return Optional.of(new BooleanMemberValue((Boolean) v, constPool));
        } else if (v instanceof String) {
            return Optional.of(new StringMemberValue((String) v, constPool));
        } else if (v instanceof Character) {
            return Optional.of(new CharMemberValue((Character) v, constPool));
        } else if (v instanceof Byte) {
            return Optional.of(new ByteMemberValue((Byte) v, constPool));
        } else if (v instanceof Short) {
            return Optional.of(new ShortMemberValue((Short) v, constPool));
        } else if (v instanceof Integer) {
            return Optional.of(new IntegerMemberValue((Integer) v, constPool));
        } else if (v instanceof Long) {
            return Optional.of(new LongMemberValue((Long) v, constPool));
        } else if (v instanceof Float) {
            return Optional.of(new FloatMemberValue((Float) v, constPool));
        } else if (v instanceof Double) {
            return Optional.of(new DoubleMemberValue((Double) v, constPool));
        } else if (v instanceof Collection) {
            final MemberValue[] values = ((Collection<?>) v).stream()
                    .map(r -> getMember(r, constPool))
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .toArray(MemberValue[]::new);

            if (values.length > 0) {
                final ArrayMemberValue value = new ArrayMemberValue(values[0], constPool);
                value.setValue(values);
                return Optional.of(value);
            }
        }

        return Optional.empty();
    }

    private static String getSourceClassName(@NotNull RawMarker marker) {
        return getClassNameFromPackage(marker.getSource());
    }

    private static String getRootClassName(@NotNull RawMarker marker) {
        return getClassNameFromPackage(marker.getRoot());
    }

    private static String getClassName(@NotNull Marker marker) {
        final String name = getClassNameFromPackage(marker.getSource());
        return getClassNameWithSuffix(name);
    }

    private static String getClassNameWithSuffix(@NotNull String name) {
        final Integer num = CLASS_SUFFIX_COUNTER.computeIfAbsent(name, k -> 0);
        return getClassPackage(num) + "." + name;
    }

    private static String getPrevClassName(@NotNull Marker marker) {
        final String originClassName = getClassNameFromPackage(marker.getRoot());
        final int num = CLASS_SUFFIX_COUNTER.computeIfAbsent(originClassName, k -> 1) - 1;
        return getClassPackage(num) + "." + originClassName;
    }

    private static String getClassNameFromPackage(@NotNull String source) {
        final int lastIndexOf = source.lastIndexOf('.', source.length() - 6);
        return source.substring(lastIndexOf + 1, source.length() - 5);
    }

    private static String getClassPackage(int num) {
        return "io.goodforgod.dummymapper.dummies_" + num;
    }
}
