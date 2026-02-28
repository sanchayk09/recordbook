package com.urviclean.recordbook.exception;

/**
 * Exception thrown when a business rule violation occurs
 */
public class BusinessLogicException extends RecordbookException {
    public BusinessLogicException(String message) {
        super(message, "BUSINESS_LOGIC_ERROR");
    }

    public BusinessLogicException(String message, String details) {
        super(message, "BUSINESS_LOGIC_ERROR", details);
    }
}

