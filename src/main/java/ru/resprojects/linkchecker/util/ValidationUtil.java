package ru.resprojects.linkchecker.util;

import ru.resprojects.linkchecker.HasId;
import ru.resprojects.linkchecker.util.exeptions.ErrorPlaceType;
import ru.resprojects.linkchecker.util.exeptions.IllegalRequestDataException;
import ru.resprojects.linkchecker.util.exeptions.NotFoundException;

/**
 * Helper class for work with validation.
 */
public final class ValidationUtil {

    public static final int NODE_COUNTER_DEFAULT = 0;
    public static final int MIN_NAME_SIZE = 1;
    public static final int MAX_NAME_SIZE = 50;
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

    public static void checkNew(HasId bean) {
        if (bean.getId() != null) {
            throw new IllegalRequestDataException(bean + " must be new (id=null)");
        }
    }

}
