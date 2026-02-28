package com.urviclean.recordbook.exception;

/**
 * Exception thrown when invalid data is provided
 */
public class InvalidInputException extends RecordbookException {

    public InvalidInputException(String message) {
        super(message, "INVALID_INPUT");
    }

    /**
     * Constructor for field validation errors
     * @param fieldName the field that failed validation
     * @param reason the reason why validation failed
     * @return InvalidInputException with formatted message
     */
    public InvalidInputException(String fieldName, String reason) {
        super(
            String.format("Invalid value for field '%s': %s", fieldName, reason),
            "INVALID_INPUT",
            String.format("Field '%s' validation failed: %s", fieldName, reason)
        );
    }

    /**
     * Static factory method for creating exception with message and details
     * Use this when you have a message and additional details to provide
     */
    public static InvalidInputException withDetails(String message, String details) {
        InvalidInputException ex = new InvalidInputException(message);
        ex.details = details;
        return ex;
    }

    // Protected field to store details (from parent class we need to access it)
    // This is handled by RecordbookException
}

