package com.ed.repository.filesystem;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Objects;
import java.util.regex.Matcher;
import com.ed.repository.exceptions.TransformPathException;

public class FileResolver {

  private String directory = "";
  private String filename = "";
  private String versionsFolder = "";
  private String extension = "";

  private Path repositoryDirectoryPath = null;
  private Path indexFilePath = null;

  private FileResolver() {
    super();
  }

  public static FileResolver createClientFileResolver(Path path) throws IOException {
    FileResolver resolver = new FileResolver();
    resolver.resolveClient(path);
    return resolver;
  }

  public static FileResolver createRepoFileResolver(Path path) {
    FileResolver resolver = new FileResolver();
    resolver.resolveRepo(path);
    return resolver;
  }

  private void resolveClient(Path path) throws IOException {
    Matcher matcher = PathParser.getClientToRepoMatcher(path);
    boolean matches = matcher.matches();

    if (!matches) {
      throw new TransformPathException("The client's path sintaxe is invalid");
    }
    this.directory = matcher.group(1);
    this.filename = matcher.group(2);
    this.extension = matcher.group(3);
  }

  private void resolveRepo(Path path) {
    Matcher matcher = PathParser.getRepoToClientMatcher(path);
    boolean matches = matcher.matches();

    if (!matches) {
      throw new TransformPathException("The repository's path sintaxe is invalid");
    }
    this.directory = matcher.group(1);
    this.versionsFolder = matcher.group(2);
    String rest = matcher.group(3);
    if (rest.isBlank()) {
      String[] parts = versionsFolder.split("#");
      this.filename = parts[0];
      this.extension = "." + parts[1];
    } else {
      String[] parts = rest.split("\\.");
      this.filename = parts[0].split("-v\\d+$")[0];
      this.extension = "." + parts[parts.length - 1];
    }
  }

  public Path getRepositoryFilePath(int version) {
    String pathStr = String.format("%s%s%s%s%s%s%d%s", directory, filename,
        extension.toUpperCase().replace(".", "#"), File.separator, filename, "-v", version,
        extension);
    return Path.of(pathStr);
  }

  public Path getRepositoryDirectoryPath() {
    if (Objects.isNull(repositoryDirectoryPath)) {
      String pathStr =
          String.format("%s%s%s", directory, filename, extension.toUpperCase().replace(".", "#"));
      this.repositoryDirectoryPath = Path.of(pathStr);
    }
    return repositoryDirectoryPath;
  }

  public Path getIndexFilePath() {
    if (Objects.isNull(indexFilePath)) {
      String pathStr = String.format("%s%s%s%s%s%s", directory, filename,
          extension.toUpperCase().replace(".", "#"), File.separator, filename, ".index.txt");
      this.indexFilePath = Path.of(pathStr);
    }
    return indexFilePath;
  }

  public int getLatestVersion() throws IOException {
    IndexFileEntry lastEntry = IndexFileEntry.readEntry(getIndexFilePath());
    return lastEntry.getKey();
  }

  // -----------

  public Path getClientDirectoryPath() {
    return Path.of(directory);
  }

  public Path getClientFilePath() {
    String pathStr = String.format("%s%s%s", directory, filename, extension);
    return Path.of(pathStr);
  }

}
