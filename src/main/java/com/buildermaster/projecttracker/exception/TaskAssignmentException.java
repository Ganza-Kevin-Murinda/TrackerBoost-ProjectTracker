package com.buildermaster.projecttracker.exception;

/**
 * Exception thrown when task assignment operations fail
 */
public class TaskAssignmentException extends ProjectTrackerException {

    public TaskAssignmentException(String message) {
        super(message);
    }

    public TaskAssignmentException(String message, Throwable cause) {
        super(message, cause);
    }
}
