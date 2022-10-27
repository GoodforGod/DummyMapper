package io.goodforgod.dummymapper.error;

/**
 * @author Anton Kurako (GoodforGod)
 * @since 6.5.2020
 */
public class UnsupportedPsiFileException extends RuntimeException {

    public UnsupportedPsiFileException() {
        super("This is not supported File type!");
    }
}
