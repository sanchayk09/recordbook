package com.urviclean.recordbook.exception;

/**
 * Exception thrown when duplicate resource is attempted to be created
 */
public class DuplicateResourceException extends RecordbookException {
    public DuplicateResourceException(String resourceType, String identifier) {
        super(
            String.format("%s already exists with identifier: %s", resourceType, identifier),
            "DUPLICATE_RESOURCE",
            String.format("A %s with this identifier already exists", resourceType)
        );
    }

    public DuplicateResourceException(String message) {
        super(message, "DUPLICATE_RESOURCE");
    }
}

