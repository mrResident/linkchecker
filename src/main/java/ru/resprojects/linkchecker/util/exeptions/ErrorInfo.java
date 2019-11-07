package ru.resprojects.linkchecker.util.exeptions;

import java.util.Arrays;
import java.util.StringJoiner;

/**
 * Data class for exception handler.
 */
public class ErrorInfo {

    private String url;
    private ErrorType type;
    private ErrorPlaceType place;
    private String[] messages;

    public ErrorInfo() {
    }

    /**
     * Ctor.
     * @param url REST request where an error has occurred
     * @param type of error
     * @param place where an error has occurred
     * @param messages program error messages
     */
    public ErrorInfo(String url, ErrorType type, ErrorPlaceType place, String... messages) {
        this.url = url;
        this.type = type;
        this.place = place;
        this.messages = messages;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public ErrorType getType() {
        return type;
    }

    public void setType(ErrorType type) {
        this.type = type;
    }

    public ErrorPlaceType getPlace() {
        return place;
    }

    public void setPlace(ErrorPlaceType place) {
        this.place = place;
    }

    public String[] getMessages() {
        return messages;
    }

    public void setMessages(String[] messages) {
        this.messages = messages;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", ErrorInfo.class.getSimpleName() + "[", "]")
            .add("url='" + url + "'")
            .add("type=" + type)
            .add("place=" + place)
            .add("messages=" + Arrays.toString(messages))
            .toString();
    }
}
