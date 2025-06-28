package com.buildermaster.projecttracker.exception;

/**
 * Exception thrown when audit operations fail
 */
public class AuditException extends ProjectTrackerException {

    public AuditException(String message) {
        super(message);
    }

    public AuditException(String message, Throwable cause) {
        super(message, cause);
    }
}
