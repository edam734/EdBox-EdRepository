package com.ed.repository.filesystem;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.ed.repository.exceptions.TransformPathException;

public class FileResolver {

  private Path repositoryDirectoryPath = null;
  private Path repositoryFilePath = null;
  private Path indexFile = null;
  private int latestVersion = -1;

  public FileResolver(Path path) throws IOException {
    resolve(path);
  }

  private void resolve(Path clientFormatPath) throws IOException {
    if (clientFormatPath.toString().contains("#")) {
      throw new TransformPathException("Client path contains marker '#'");
    }
    String regex = "^(.*\\/)(.*)([\\.*]\\S+)".replace("/", File.separator);
    Pattern pattern = Pattern.compile(regex);
    Matcher matcher = pattern.matcher(clientFormatPath.toString().replace("/", File.separator));
    boolean matches = matcher.matches();

    if (!matches) {
      throw new TransformPathException("The client path is invalid");
    }
    String path = matcher.group(1);
    String filename = matcher.group(2);
    String extension = matcher.group(3);

    this.indexFile = buildPathToIndexFile(path, filename, extension);
    this.latestVersion = getLatestVersion();
    this.repositoryDirectoryPath = buildPathToRepositoryDirectoryPath(path, filename, extension);
    this.repositoryFilePath =
        buildPathToRepositoryFilePath(path, filename, extension, latestVersion);
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

  public Path getIndexFile() {
    return indexFile;
  }

  public int getLatestVersion() throws IOException {
    if (latestVersion == -1) {
      FileEntry readEntry = FileEntry.readEntry(this.indexFile);
      int latest = readEntry.getKey();
      return ++latest;
    }
    return latestVersion;
  }
}
