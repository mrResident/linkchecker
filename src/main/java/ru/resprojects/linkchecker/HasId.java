package ru.resprojects.linkchecker;

/**
 * The interface indicates that the object has an identifier.
 */
public interface HasId {

    /**
     * Get entity id.
     * @return entity id.
     */
    Integer getId();

    /**
     * Set entity id.
     * @param id of entity.
     */
    void setId(Integer id);

}
