package ru.resprojects.linkchecker.util;

import ru.resprojects.linkchecker.HasId;
import ru.resprojects.linkchecker.util.exeptions.ErrorPlaceType;
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

    private ValidationUtil() {
    }

    public static <T> T checkNotFoundWithId(T object, int id, ErrorPlaceType place) {
        return checkNotFound(object, "id=" + id, place);
    }

    public static void checkNotFoundWithId(boolean found, int id, ErrorPlaceType place) {
        checkNotFound(found, "id=" + id, place);
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
        if (!bean.isNew()) {
            throw new IllegalRequestDataException(bean + " must be new (id=null)");
        }
    }

}
