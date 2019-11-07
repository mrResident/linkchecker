package ru.resprojects.linkchecker.util.exeptions;

/**
 * Types of errors what my be occurred in program
 */
public enum ErrorType {
    APP_ERROR, //Types of errors that are associated with errors in the program as a whole
    DATA_NOT_FOUND, //Types of errors that are associated with search and extract data
    DATA_ERROR, //Types of errors that are associated with data handling
    VALIDATION_ERROR, //Types of errors are associated with validating data in REST-controllers
}
