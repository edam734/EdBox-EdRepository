package com.ed.repository.filesystem;

import java.io.IOException;
import java.nio.file.Path;

public class RepositoryFileResolver {

  private FileResolver resolver;

  public RepositoryFileResolver(Path path) throws IOException {
    super();
    this.resolver = FileResolver.createRepoFileResolver(path);
  }

  public Path getClientDirectoryPath() {
    return resolver.getClientDirectoryPath();
  }

  public Path getClientFilePath() {
    return resolver.getClientFilePath();
  }

  public int getLatestVersion() throws IOException {
    return resolver.getLatestVersion();
  }

  public Path getRepositoryFilePath(int version) {
    return resolver.getRepositoryFilePath(version);
  }
}
