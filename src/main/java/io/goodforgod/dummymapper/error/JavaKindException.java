package io.goodforgod.dummymapper.error;


import com.intellij.lang.jvm.JvmClassKind;


/**
 * @author Anton Kurako (GoodforGod)
 * @since 6.5.2020
 */
public class JavaKindException extends RuntimeException {

    public JavaKindException(JvmClassKind kind) {
        super("Java File is " + kind + " and can not be mapped!");
    }
}
