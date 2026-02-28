package com.urviclean.recordbook.exception;

/**
 * Exception thrown when database operation fails
 */
public class DatabaseException extends RecordbookException {
    public DatabaseException(String message) {
        super(message, "DATABASE_ERROR");
    }

    public DatabaseException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "DATABASE_ERROR";
    }

    public DatabaseException(String operation, String details, Throwable cause) {
        super(
            String.format("Database operation failed: %s", operation),
            cause
        );
        this.errorCode = "DATABASE_ERROR";
        this.details = details;
    }
}

