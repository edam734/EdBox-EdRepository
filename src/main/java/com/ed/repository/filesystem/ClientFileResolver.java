package com.ed.repository.filesystem;

import java.io.IOException;
import java.nio.file.Path;

public class ClientFileResolver {

  private FileResolver resolver;

  public ClientFileResolver(Path path) throws IOException {
    super();
    this.resolver = FileResolver.createClientFileResolver(path);
  }

  public Path getRepositoryDirectoryPath() {
    return resolver.getRepositoryDirectoryPath();
  }

  public Path getIndexFilePath() {
    return resolver.getIndexFilePath();
  }

  public Path getRepositoryFilePath(int version) {
    return resolver.getRepositoryFilePath(version);
  }

  public int getLatestVersion() throws IOException {
    return resolver.getLatestVersion();
  }

}
