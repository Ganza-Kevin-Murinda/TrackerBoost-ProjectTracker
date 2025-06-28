package com.buildermaster.projecttracker.exception;

/**
 * Base exception class for all custom application exceptions
 */
public abstract class ProjectTrackerException extends RuntimeException {

  protected ProjectTrackerException(String message) {
    super(message);
  }

  protected ProjectTrackerException(String message, Throwable cause) {
    super(message, cause);
  }
}
