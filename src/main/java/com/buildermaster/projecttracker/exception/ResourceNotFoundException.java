package com.buildermaster.projecttracker.exception;

/**
 * Exception thrown when a requested resource is not found
 */
public class ResourceNotFoundException extends ProjectTrackerException {

    public ResourceNotFoundException(String resourceType, Object id) {
        super(String.format("%s with id '%s' not found", resourceType, id));
    }

    public ResourceNotFoundException(String resourceType, String field, Object value) {
        super(String.format("%s with %s '%s' not found", resourceType, field, value));
    }

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
