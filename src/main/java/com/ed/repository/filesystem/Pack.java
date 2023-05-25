package com.ed.repository.filesystem;

import java.nio.file.Path;
import java.util.Objects;

/**
 * A class that contains a file and a destination where the contents of the file should be written
 * later
 * 
 * @author Eduardo Amorim
 *
 */
public class Pack {

  private Path content;
  private Path destination;

  /**
   * @param content A file
   * @param destination Where the content should be saved
   */
  private Pack(Path content, Path destination) {
    super();
    this.content = content;
    this.destination = destination;
  }

  /**
   * This class has the filesystem repository point of view. Get's a path in repository's format an
   * generate a destination equivalent to the client side.
   * 
   * @param path - a repository's path
   * @return A new Pack containing the same path in two different formats
   * @throws TransformPathException if something's wrong with the argument path
   */
  public static Pack createPack(Path path) {
    Path unversionedPath = PathParser.repoToClientPath(path);
    return new Pack(path, unversionedPath);
  }

  public Path getContent() {
    return content;
  }

  public Path getDestination() {
    return destination;
  }

  public void setDestination(Path destination) {
    this.destination = destination;
  }

  @Override
  public int hashCode() {
    return Objects.hash(content, destination);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    Pack other = (Pack) obj;
    return Objects.equals(content, other.content) && Objects.equals(destination, other.destination);
  }

  @Override
  public String toString() {
    return "Pack [content=" + content + ", destination=" + destination + "]";
  }

}
