package com.ed.repository.exceptions;

public class NotPathToAServerFileException extends Exception {
  private static final long serialVersionUID = 1L;

  String message;

  public NotPathToAServerFileException(String message) {
    super();
    this.message = message;
  }

  @Override
  public String getMessage() {
    return this.message;
  }
}
