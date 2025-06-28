package com.buildermaster.projecttracker.exception;

/**
 * Exception thrown when business rules are violated
 */
public class BusinessRuleViolationException extends ProjectTrackerException {

  public BusinessRuleViolationException(String message) {
    super(message);
  }

  public BusinessRuleViolationException(String message, Throwable cause) {
    super(message, cause);
  }
}
