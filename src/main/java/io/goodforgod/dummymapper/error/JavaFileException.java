package io.goodforgod.dummymapper.error;

/**
 * @author Anton Kurako (GoodforGod)
 * @since 6.5.2020
 */
public class JavaFileException extends RuntimeException {

    public JavaFileException() {
        super("File is not Java File!");
    }
}
