package com.ed.repository.filesystem;

import java.nio.file.Path;
import java.nio.file.Paths;

class ClientPathResolver implements PathResolver {

  private String extension;
  private Path directory;
  private Path directoryName;
  private Path indexFile;

  /**
   * @requires !path.isDirectory()
   * @param path
   */
  public ClientPathResolver(Path path) {
    resolve(path);
  }

  private void resolve(Path path) {
    directory = path;
    extension = "." + directory.getFileName().toString().split(MARK)[1].toLowerCase();
    directoryName = Paths.get(directory.getFileName().toString().split(MARK)[0]);
    indexFile = Paths.get(String.format("%s.index.txt", directory.resolve(directoryName)));
  }

  public Path getDirectory() {
    return directory;
  }

  public Path getIndexFile() {
    return indexFile;
  }

  public Path getVersionedFilename(int newVersion) {
    return Paths
        .get(String.format("%s-v%d%s", directory.resolve(directoryName), newVersion, extension));
  }

  /**
   * Converts a versioned name to an unversioned name
   * 
   * @param path The path to convert
   * @requires path must be a versioned file name
   * @return an unversioned name
   */
  public Path getUnversionedFilename(Path path) {
    String complete = path.getFileName().toString();
    String[] parts = complete.split("-v\\d+");
    Path fname = Paths.get(parts[0].concat(parts[1]));
    Path dir = path.getParent();
    if (dir != null) {
      dir = dir.getParent();
      return Paths.get(dir.toString(), parts[0].concat(parts[1]));
    }
    return fname;
  }
}
