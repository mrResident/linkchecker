package ru.resprojects.linkchecker.util.exeptions;

import org.springframework.http.HttpStatus;

import java.util.Arrays;

public class ApplicationException extends RuntimeException {

    private final ErrorType type;
    private final ErrorPlaceType place;
    private final HttpStatus httpStatus;
    private final String[] messages;

    public ApplicationException(String messages, HttpStatus httpStatus) {
        this(ErrorType.APP_ERROR, ErrorPlaceType.GRAPH, httpStatus, messages);
    }

    public ApplicationException(ErrorType type, ErrorPlaceType place, HttpStatus httpStatus, String... messages) {
        super(String.format("type=%s, place=%s, msg=%s", type, place, Arrays.toString(messages)));
        this.type = type;
        this.place = place;
        this.messages = messages;
        this.httpStatus = httpStatus;
    }

    public ErrorType getType() {
        return type;
    }

    public ErrorPlaceType getPlace() {
        return place;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    public String[] getMessages() {
        return messages;
    }
}
