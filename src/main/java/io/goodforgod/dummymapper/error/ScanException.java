package io.goodforgod.dummymapper.error;

/**
 * Description in progress
 *
 * @author Anton Kurako (GoodforGod)
 * @since 26.4.2020
 */
public class ScanException extends RuntimeException {

    public ScanException(String message) {
        super(message);
    }

    public ScanException(Throwable cause) {
        super(cause.getMessage(), cause.getCause());
    }
}
