package ru.resprojects.linkchecker.model;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

import static ru.resprojects.linkchecker.util.ValidationUtil.*;
import static ru.resprojects.linkchecker.util.Messages.*;

/**
 * Abstract base class for named database entity.
 */
@MappedSuperclass
public abstract class AbstractNamedEntity extends AbstractBaseEntity {

    /**
     * Unique name of database entity.
     */
    @Column(name = "name", nullable = false, unique = true)
    @NotBlank(message = VALIDATOR_NODE_NOT_BLANK_NAME_MESSAGE)
    @Size(min = MIN_NAME_SIZE, max = MAX_NAME_SIZE,
        message = VALIDATOR_NODE_NAME_RANGE_MESSAGE)
    private String name;

    /**
     * Default ctor.
     */
    AbstractNamedEntity() {
    }

    /**
     * Ctor.
     * @param id of database entity.
     * @param name unique name of database entity.
     */
    AbstractNamedEntity(final Integer id, final String name) {
        super(id);
        this.name = name;
    }

    /**
     * Set unique name of database entity.
     * @param name unique name of database entity.
     */
    public void setName(final String name) {
        this.name = name;
    }

    /**
     * Get unique name of database entity.
     * @return unique name of database entity.
     */
    public String getName() {
        return this.name;
    }

    @Override
    public String toString() {
        return String.format("Entity %s (%s, '%s')",
            getClass().getName(), getId(), name);
    }

}
