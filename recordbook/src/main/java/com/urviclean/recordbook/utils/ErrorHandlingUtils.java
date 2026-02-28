package com.urviclean.recordbook.utils;

import com.urviclean.recordbook.exception.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class for handling errors and validation
 */
public class ErrorHandlingUtils {

    private static final Logger logger = LoggerFactory.getLogger(ErrorHandlingUtils.class);

    /**
     * Validates that a value is not null or empty
     */
    public static void validateNotEmpty(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new InvalidInputException(fieldName, "cannot be null or empty");
        }
    }

    /**
     * Validates that an object is not null
     */
    public static void validateNotNull(Object value, String fieldName) {
        if (value == null) {
            throw new InvalidInputException(fieldName, "cannot be null");
        }
    }

    /**
     * Validates that a value is positive
     */
    public static void validatePositive(Number value, String fieldName) {
        if (value == null || value.doubleValue() <= 0) {
            throw new InvalidInputException(fieldName, "must be positive");
        }
    }

    /**
     * Validates that a value matches a pattern
     */
    public static void validatePattern(String value, String pattern, String fieldName) {
        if (value != null && !value.matches(pattern)) {
            throw new InvalidInputException(fieldName, "invalid format");
        }
    }

    /**
     * Validates date range
     */
    public static void validateDateRange(java.time.LocalDate startDate, java.time.LocalDate endDate) {
        validateNotNull(startDate, "startDate");
        validateNotNull(endDate, "endDate");

        if (startDate.isAfter(endDate)) {
            throw new InvalidInputException(
                "Date range",
                "startDate cannot be after endDate"
            );
        }
    }

    /**
     * Validates minimum length
     */
    public static void validateMinLength(String value, int minLength, String fieldName) {
        if (value != null && value.length() < minLength) {
            throw new InvalidInputException(
                fieldName,
                String.format("must be at least %d characters long", minLength)
            );
        }
    }

    /**
     * Validates maximum length
     */
    public static void validateMaxLength(String value, int maxLength, String fieldName) {
        if (value != null && value.length() > maxLength) {
            throw new InvalidInputException(
                fieldName,
                String.format("cannot exceed %d characters", maxLength)
            );
        }
    }

    /**
     * Handles optional resources - throws exception if empty
     */
    public static <T> T orElseThrowNotFound(java.util.Optional<T> optional, String resourceType, String identifier) {
        return optional.orElseThrow(() ->
            new ResourceNotFoundException(resourceType, identifier)
        );
    }

    /**
     * Logs and rethrows database errors
     */
    public static void handleDatabaseError(Exception ex, String operation) {
        logger.error("Database operation failed: {}", operation, ex);
        throw new DatabaseException(operation, ex.getMessage(), ex);
    }

    /**
     * Logs and rethrows business logic errors
     */
    public static void throwBusinessLogicError(String message, String details) {
        logger.warn("Business logic error: {}", message);
        throw new BusinessLogicException(message, details);
    }
}

