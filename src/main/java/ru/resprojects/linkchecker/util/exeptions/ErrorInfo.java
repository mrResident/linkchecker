package ru.resprojects.linkchecker.util.exeptions;

public class ErrorInfo {

    private final String url;
    private final ErrorType type;
    private final ErrorPlaceType place;
    private final String msg;

    public ErrorInfo(String url, ErrorType type, ErrorPlaceType place, String msg) {
        this.url = url;
        this.type = type;
        this.place = place;
        this.msg = msg;
    }
}
