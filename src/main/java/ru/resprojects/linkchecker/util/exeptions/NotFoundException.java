package ru.resprojects.linkchecker.util.exeptions;

public class NotFoundException extends RuntimeException {

    /**
     * Ctor.
     * @param message exception message.
     */
    public NotFoundException(final String message) {
        super(message);
    }

}
