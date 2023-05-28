package com.ed.repository.exceptions;

public class TransformPathException extends RuntimeException {
  private static final long serialVersionUID = 1L;

  String message;

  public TransformPathException(String message) {
    super();
    this.message = message;
  }

  public String getMessage() {
    return message;
  }
}
