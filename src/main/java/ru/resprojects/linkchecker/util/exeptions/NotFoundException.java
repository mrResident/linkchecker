package ru.resprojects.linkchecker.util.exeptions;

import org.springframework.http.HttpStatus;

public class NotFoundException extends ApplicationException {

    /**
     * Ctor.
     * @param message exception message.
     * @param place place of exception {@link ErrorPlaceType}.
     */
    public NotFoundException(final String message, ErrorPlaceType place) {
        super(ErrorType.DATA_NOT_FOUND, place, HttpStatus.UNPROCESSABLE_ENTITY, message);
    }

}
