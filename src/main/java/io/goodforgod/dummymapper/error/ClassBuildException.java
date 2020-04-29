package io.goodforgod.dummymapper.error;

/**
 * Description in progress
 *
 * @author Anton Kurako (GoodforGod)
 * @since 19.4.2020
 */
public class ClassBuildException extends RuntimeException {

    public ClassBuildException(String message) {
        super(message);
    }

    public ClassBuildException(Throwable cause) {
        super(cause.getMessage(), cause);
    }
}
