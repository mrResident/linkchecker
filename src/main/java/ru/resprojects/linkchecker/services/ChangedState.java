package ru.resprojects.linkchecker.services;

public interface ChangedState {

    /**
     * Return current state.
     * @return true if state is changed.
     */
    boolean isStateChanged();

    /**
     * Reset current state.
     */
    void resetCurrentState();

}
