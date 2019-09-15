package ru.resprojects.linkchecker.util;

import ru.resprojects.linkchecker.HasId;
import ru.resprojects.linkchecker.util.exeptions.IllegalRequestDataException;
import ru.resprojects.linkchecker.util.exeptions.NotFoundException;

/**
 * Helper class for work with validation.
 */
public final class ValidationUtil {

    public static final int NODE_PROBABILITY_DEFAULT = 50;
    public static final int NODE_COUNTER_DEFAULT = 0;
    public static final int MIN_NAME_SIZE = 1;
    public static final int MAX_NAME_SIZE = 50;
    public static final int MIN_PROBABILITY_VALUE = 0;
    public static final int MAX_PROBABILITY_VALUE = 100;
    public static final String VALIDATOR_NODE_NOT_BLANK_NAME_MESSAGE = "value does not be empty";
    public static final String VALIDATOR_NODE_NAME_RANGE_MESSAGE = "value must be at range from "
        + MIN_NAME_SIZE + " to " + MAX_NAME_SIZE;
    public static final String VALIDATOR_NODE_PROBABILITY_RANGE_MESSAGE = "value must be at range from "
        + MIN_PROBABILITY_VALUE + " to " + MAX_PROBABILITY_VALUE;
    public static final String VALIDATOR_NOT_NULL_MESSAGE = "object does not be null";

    private ValidationUtil() {
    }

    public static <T> T checkNotFoundWithId(T object, int id) {
        return checkNotFound(object, "Node with id=" + id + " is not found");
    }

    public static void checkNotFoundWithId(boolean found, int id) {
        checkNotFound(found, "id=" + id);
    }

    public static <T> T checkNotFound(T object, String msg) {
        checkNotFound(object != null, msg);
        return object;
    }

    public static void checkNotFound(boolean found, String arg) {
        if (!found) {
            throw new NotFoundException(arg);
        }
    }

    public static void checkNew(HasId bean) {
        if (!bean.isNew()) {
            throw new IllegalRequestDataException(bean + " must be new (id=null)");
        }
    }

}
