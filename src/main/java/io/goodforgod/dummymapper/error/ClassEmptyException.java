package io.goodforgod.dummymapper.error;

/**
 * Error that occurs when Class Name is not presented while class construction
 *
 * @author Anton Kurako (GoodforGod)
 * @since 19.4.2020
 */
public class ClassEmptyException extends RuntimeException {

    public ClassEmptyException() {
        super("Class structure is empty");
    }
}
