package io.goodforgod.dummymapper.error;

/**
 * Error that occurs when Class Name is not presented while class construction
 *
 * @author Anton Kurako (GoodforGod)
 * @since 19.4.2020
 */
public class ClassBuildException extends RuntimeException {

    public ClassBuildException(String message) {
        super(message);
    }

    public ClassBuildException(Throwable cause) {
        super("Error building Java Class", cause);
    }
}
