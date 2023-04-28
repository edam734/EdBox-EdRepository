package com.ed.repository.exceptions;

public class VersionGreaterThanLatestVersionException extends RuntimeException {
  private static final long serialVersionUID = 1L;

  String message;

  public VersionGreaterThanLatestVersionException(String message) {
    this.message = message;
  }

  @Override
  public String getMessage() {
    return this.message;
  }
}
