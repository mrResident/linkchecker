package ru.resprojects.linkchecker.model;

import org.hibernate.Hibernate;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.SequenceGenerator;
import java.io.Serializable;

/**
 * Abstract base class for database entity.
 */
@MappedSuperclass
public abstract class AbstractBaseEntity implements HasId, Serializable {

    /**
     * Initial number for database id sequential.
     */
    private static final int START_SEQ = 5000;

    /**
     * Unique identity for database entity.
     */
    @Id
    @SequenceGenerator(name = "global_seq", sequenceName = "global_seq",
        allocationSize = 1, initialValue = START_SEQ)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "global_seq")
    protected Integer id;

    /**
     * Default ctor.
     */
    AbstractBaseEntity() {
    }

    /**
     * Ctor.
     * @param id - unique database entity identity.
     */
    AbstractBaseEntity(final Integer id) {
        this.id = id;
    }

    /**
     * Get unique database entity identity.
     * @return unique database entity identity.
     */
    @Override
    public Integer getId() {
        return id;
    }

    /**
     * Set unique database entity identity.
     * @param id of database entity.
     */
    @Override
    public void setId(final Integer id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return String.format("Entity %s (%s)", getClass().getName(), id);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || !getClass().equals(Hibernate.getClass(o))) {
            return false;
        }
        AbstractBaseEntity that = (AbstractBaseEntity) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id == null ? 0 : id;
    }

}
