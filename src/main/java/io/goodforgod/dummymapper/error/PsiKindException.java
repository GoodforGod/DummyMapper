package io.goodforgod.dummymapper.error;

import com.intellij.lang.jvm.JvmClassKind;

/**
 * @author Anton Kurako (GoodforGod)
 * @since 6.5.2020
 */
public class PsiKindException extends RuntimeException {

    public PsiKindException(JvmClassKind kind) {
        super("Class is '" + kind + "' and can not be mapped!");
    }
}
