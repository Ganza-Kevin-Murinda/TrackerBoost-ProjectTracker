package com.buildermaster.projecttracker.exception;

import lombok.Getter;
import java.util.Map;

/**
 * Exception thrown when business rule validation fails
 */
@Getter
public class ValidationException extends ProjectTrackerException {

    private final Map<String, String> fieldErrors;

    public ValidationException(String message) {
        super(message);
        this.fieldErrors = Map.of();
    }

    public ValidationException(String field, String error) {
        super(String.format("Validation failed for field '%s': %s", field, error));
        this.fieldErrors = Map.of(field, error);
    }

    public ValidationException(Map<String, String> fieldErrors) {
        super("Validation failed for multiple fields");
        this.fieldErrors = fieldErrors;
    }

}