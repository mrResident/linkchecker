package ru.resprojects.linkchecker.model;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * Abstract base class for named database entity.
 */
@MappedSuperclass
public abstract class AbstractNamedEntity extends AbstractBaseEntity {

    /**
     * Maximum string database field size.
     */
    private static final int MAX_SIZE = 50;

    /**
     * Minimum string database field size.
     */
    private static final int MIN_SIZE = 1;

    /**
     * Unique name of database entity.
     */
    @Column(name = "name", nullable = false, unique = true)
    @NotBlank
    @Size(min = MIN_SIZE, max = MAX_SIZE)
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
