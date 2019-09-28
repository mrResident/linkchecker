package ru.resprojects.linkchecker.util.exeptions;

import org.springframework.http.HttpStatus;

public class ApplicationException extends RuntimeException {

    private final ErrorType type;
    private final ErrorPlaceType place;
    private final String msg;
    private final HttpStatus httpStatus;

    public ApplicationException(String msg, HttpStatus httpStatus) {
        this(ErrorType.APP_ERROR, ErrorPlaceType.GRAPH, httpStatus, msg);
    }

    public ApplicationException(ErrorType type, ErrorPlaceType place, HttpStatus httpStatus, String msg) {
        super(String.format("type=%s, place=%s, msg=%s", type, place, msg));
        this.type = type;
        this.place = place;
        this.msg = msg;
        this.httpStatus = httpStatus;
    }

    public ErrorType getType() {
        return type;
    }

    public ErrorPlaceType getPlace() {
        return place;
    }

    @Override
    public String getMessage() {
        return msg;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }
}
