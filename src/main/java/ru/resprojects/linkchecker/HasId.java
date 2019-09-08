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

    /**
     * Get current status of entity id. If entity id is not set - return false,
     * else true.
     * @return true if entity has id, else false.
     */
    default boolean isNew() {
        return getId() != null;
    }

}
