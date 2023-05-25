package com.ed.repository.filesystem;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.regex.Matcher;
import com.ed.repository.exceptions.TransformPathException;

public class FileResolver {

  private Path repositoryDirectoryPath = null;
  private Path repositoryFilePath = null;
  private Path indexFilePath = null;
  private int nextVersion = -1;

  public FileResolver(Path path) throws IOException {
    resolve(path);
  }

  private void resolve(Path clientFormatPath) throws IOException {
    Matcher matcher = PathParser.clientToRepoMatcher(clientFormatPath);
    boolean matches = matcher.matches();

    if (!matches) {
      throw new TransformPathException("The client path is invalid");
    }
    String path = matcher.group(1);
    String filename = matcher.group(2);
    String extension = matcher.group(3);

    this.indexFilePath = buildPathToIndexFile(path, filename, extension);
    this.nextVersion = getNextVersion();
    this.repositoryDirectoryPath = buildPathToRepositoryDirectoryPath(path, filename, extension);
    this.repositoryFilePath = buildPathToRepositoryFilePath(path, filename, extension, nextVersion);
  }

  private static Path buildPathToRepositoryDirectoryPath(String path, String filename,
      String extension) {
    String pathStr =
        String.format("%s%s%s", path, filename, extension.toUpperCase().replace(".", "#"));
    return Path.of(pathStr);
  }

  private static Path buildPathToRepositoryFilePath(String path, String filename, String extension,
      int version) {
    String pathStr =
        String.format("%s%s%s%s%s%s%d%s", path, filename, extension.toUpperCase().replace(".", "#"),
            File.separator, filename, "-v", version, extension);
    return Path.of(pathStr);
  }

  private static Path buildPathToIndexFile(String path, String filename, String extension) {
    String pathStr = String.format("%s%s%s%s%s%s", path, filename,
        extension.toUpperCase().replace(".", "#"), File.separator, filename, ".index.txt");
    return Path.of(pathStr);
  }

  public Path getRepositoryDirectoryPath() {
    return repositoryDirectoryPath;
  }

  public Path getRepositoryFilePath() {
    return repositoryFilePath;
  }

  public Path getIndexFilePath() {
    return indexFilePath;
  }

  public int getNextVersion() throws IOException {
    if (nextVersion == -1) {
      IndexFileEntry lastEntry = IndexFileEntry.readEntry(this.indexFilePath);
      int latestVersion = lastEntry.getKey();
      return ++latestVersion; // the next version is 1 up from the latest one in the archive
    }
    return nextVersion;
  }
}
