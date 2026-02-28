package com.urviclean.recordbook.exception;

/**
 * Base exception class for all custom exceptions in the application
 */
public class RecordbookException extends RuntimeException {
    protected String errorCode;
    protected String details;

    public RecordbookException(String message) {
        super(message);
        this.errorCode = "GENERAL_ERROR";
    }

    public RecordbookException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public RecordbookException(String message, String errorCode, String details) {
        super(message);
        this.errorCode = errorCode;
        this.details = details;
    }

    public RecordbookException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "GENERAL_ERROR";
    }

    public String getErrorCode() {
        return errorCode;
    }

    public String getDetails() {
        return details;
    }
}

