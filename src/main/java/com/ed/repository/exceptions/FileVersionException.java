package com.ed.repository.exceptions;

public class FileVersionException extends RuntimeException {
  private static final long serialVersionUID = 1L;

  String message;

  public FileVersionException(String message) {
    this.message = message;
  }

  @Override
  public String getMessage() {
    return this.message;
  }
}
