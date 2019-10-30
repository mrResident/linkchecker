package ru.resprojects.linkchecker.util;

import org.slf4j.Logger;
import ru.resprojects.linkchecker.util.exeptions.ErrorPlaceType;
import ru.resprojects.linkchecker.util.exeptions.ErrorType;
import ru.resprojects.linkchecker.util.exeptions.NotFoundException;

import javax.servlet.http.HttpServletRequest;

/**
 * Helper class for work with validation.
 */
public final class ValidationUtil {

    public static final int NODE_COUNTER_DEFAULT = 0;
    public static final int MIN_NAME_SIZE = 1;
    public static final int MAX_NAME_SIZE = 20;
    public static final String VALIDATOR_NOT_NULL_MESSAGE = "Object must not be null";
    public static final String VALIDATOR_NODE_NOT_BLANK_NAME_MESSAGE = "Value must not be empty";
    public static final String VALIDATOR_NODE_NAME_RANGE_MESSAGE = "Value must be at range from "
        + MIN_NAME_SIZE + " to " + MAX_NAME_SIZE;

    private ValidationUtil() {
    }

    public static <T> T checkNotFound(T object, String msg, ErrorPlaceType place) {
        checkNotFound(object != null, msg, place);
        return object;
    }

    public static void checkNotFound(boolean found, String arg, ErrorPlaceType place) {
        if (!found) {
            throw new NotFoundException(arg, place);
        }
    }

    //  http://stackoverflow.com/a/28565320/548473
    private static Throwable getRootCause(Throwable t) {
        Throwable result = t;
        Throwable cause;

        while (null != (cause = result.getCause()) && (result != cause)) {
            result = cause;
        }
        return result;
    }

    public static String getMessage(Throwable e) {
        return e.getLocalizedMessage() != null ? e.getLocalizedMessage() : e.getClass().getName();
    }

    public static Throwable logAndGetRootCause(Logger log, HttpServletRequest req,
        Exception e, boolean logException, ErrorType errorType) {
        Throwable rootCause = ValidationUtil.getRootCause(e);
        if (logException) {
            log.error(errorType + " at request " + req.getRequestURL(), rootCause);
        } else {
            log.warn("{} at request  {}: {}", errorType, req.getRequestURL(), rootCause.toString());
        }
        return rootCause;
    }

}
