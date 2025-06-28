package com.buildermaster.projecttracker.exception;

/**
 * Exception thrown when attempting to create a resource that already exists
 */
public class DuplicateResourceException extends ProjectTrackerException {

    public DuplicateResourceException(String resourceType, String field, Object value) {
        super(String.format("%s with %s '%s' already exists", resourceType, field, value));
    }

    public DuplicateResourceException(String message) {
        super(message);
    }
}
