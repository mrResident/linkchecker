package ru.resprojects.linkchecker.dto;

import ru.resprojects.linkchecker.HasId;

/**
 * Abstract base class for data transfer object.
 */
abstract public class BaseDto implements HasId {

    /**
     * ID of transport object.
     */
    protected Integer id;

    /**
     * Default ctor.
     */
    BaseDto() {
    }

    /**
     * Ctor.
     * @param id of transport object.
     */
    BaseDto(final Integer id) {
        this.id = id;
    }

    @Override
    public Integer getId() {
        return id;
    }

    @Override
    public void setId(final Integer id) {
        this.id = id;
    }

}
