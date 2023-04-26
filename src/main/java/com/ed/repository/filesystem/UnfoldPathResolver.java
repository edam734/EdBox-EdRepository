package com.ed.repository.filesystem;

import java.nio.file.Path;
import java.nio.file.Paths;

public class UnfoldPathResolver implements PathResolver {
  private String extension;
  private String filename;
  private Path directory;
  private Path indexFile;

  public Path getDirectory() {
    return directory;
  }

  public Path getIndexFile() {
    return indexFile;
  }

  public Path getVersionedFilename(final int newVersion) {
    return filePath(directory, filename, newVersion);
  }

  public UnfoldPathResolver(Path path) {
    resolve(path);
  }

  private void resolve(Path path) {
    extension = getExtension(path.getFileName().toString());
    filename = removeExtension(path.getFileName().toString());
    Path pathWithoutExtension = Paths.get(removeExtension(path.toString()));
    directory = directoryPath(pathWithoutExtension,
        String.format("%s" + MARK + "%s", filename, extension.substring(1).toUpperCase()));
    indexFile = indexFilePath(directory, filename);
  }

  private String getExtension(final String filename) {
    int beginIndex = filename.lastIndexOf('.');
    return filename.substring(beginIndex);
  }

  private String removeExtension(final String filename) {
    int endIndex = filename.lastIndexOf('.');
    return filename.substring(0, endIndex);
  }

  private Path directoryPath(final Path path, final String directoryName) {
    return Paths.get(path.getParent().toString(), directoryName);
  }

  private Path indexFilePath(final Path path, String name) {
    return Paths.get(path.toString(), (name + ".index.txt"));
  }

  private Path filePath(final Path path, String name, int version) {
    return Paths.get(path.toString(), name + "-v" + version + extension);
  }
}
