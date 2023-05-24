package com.ed.repository.exceptions;

public class RepositoryManagementException extends RuntimeException {
  private static final long serialVersionUID = 1L;

  String message;
  Exception exception;

  public RepositoryManagementException(Exception exception) {
    super();
    this.exception = exception;
  }

  public RepositoryManagementException(String message) {
    super();
    this.message = message;
  }

  public RepositoryManagementException(String message, Exception exception) {
    super();
    this.message = message;
    this.exception = exception;
  }

  public String getMessage() {
    return message;
  }
}
