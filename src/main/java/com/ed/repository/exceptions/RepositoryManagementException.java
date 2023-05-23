package com.ed.repository.exceptions;

public class RepositoryManagementException extends RuntimeException {
  private static final long serialVersionUID = 1L;

  String message;

  public RepositoryManagementException(String message) {
    super();
    this.message = message;
  }

  public String getMessage() {
    return message;
  }
}
