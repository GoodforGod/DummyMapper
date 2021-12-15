package io.goodforgod.dummymapper.error;


/**
 * Error that occurs when Class Name is not presented while class construction
 *
 * @author Anton Kurako (GoodforGod)
 * @since 19.4.2020
 */
public class ClassNameException extends RuntimeException {

    public ClassNameException() {
        super("Class Name is not presented in Marker");
    }
}
