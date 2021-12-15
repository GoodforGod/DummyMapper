package io.goodforgod.dummymapper.error;


/**
 * @author Anton Kurako (GoodforGod)
 * @since 13.6.2020
 */
public class ExternalException extends RuntimeException {

    public ExternalException(String message) {
        super(message);
    }

    public ExternalException(Throwable cause) {
        super(cause.getMessage(), cause);
    }
}
