package ru.resprojects.linkchecker.util;

import ru.resprojects.linkchecker.HasId;
import ru.resprojects.linkchecker.util.exeptions.IllegalRequestDataException;
import ru.resprojects.linkchecker.util.exeptions.NotFoundException;

/**
 * Helper class for work with validation.
 */
public final class ValidationUtil {

    private ValidationUtil() {
    }

    public static <T> T checkNotFoundWithId(T object, int id) {
        return checkNotFound(object, "id=" + id);
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
