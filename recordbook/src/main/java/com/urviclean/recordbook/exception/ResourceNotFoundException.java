package com.urviclean.recordbook.exception;

/**
 * Exception thrown when a requested resource is not found
 */
public class ResourceNotFoundException extends RecordbookException {
    public ResourceNotFoundException(String resourceType, String identifier) {
        super(
            String.format("%s not found with identifier: %s", resourceType, identifier),
            "RESOURCE_NOT_FOUND",
            String.format("Could not find %s matching: %s", resourceType, identifier)
        );
    }

    public ResourceNotFoundException(String resourceType, String fieldName, Object fieldValue) {
        super(
            String.format("%s not found with %s: %s", resourceType, fieldName, fieldValue),
            "RESOURCE_NOT_FOUND",
            String.format("No %s exists with %s = %s", resourceType, fieldName, fieldValue)
        );
    }

    public ResourceNotFoundException(String message) {
        super(message, "RESOURCE_NOT_FOUND");
    }
}

