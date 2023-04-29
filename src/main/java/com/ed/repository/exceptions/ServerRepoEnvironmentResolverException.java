package com.ed.repository.exceptions;

public class ServerRepoEnvironmentResolverException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  String message;

  public ServerRepoEnvironmentResolverException(String message) {
    super();
    this.message = message;
  }

  public String getMessage() {
    return message;
  }

}
