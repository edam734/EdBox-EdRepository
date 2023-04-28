package com.ed.repository.filesystem;

import java.io.File;
import java.nio.file.Path;
import java.util.Objects;

/**
 * A class that contains a file and a destination where the contents of the file should be written
 * later
 * 
 * @author Eduardo Amorim
 *
 */
public class WrappedFile {

  private File content;
  private Path destination;

  /**
   * @param content A file
   * @param destination Where the content should be saved
   */
  public WrappedFile(File content, Path destination) {
    super();
    this.content = content;
    this.destination = destination;
  }

  public File getContent() {
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
    WrappedFile other = (WrappedFile) obj;
    return Objects.equals(content, other.content) && Objects.equals(destination, other.destination);
  }

  @Override
  public String toString() {
    return "WrappedFile [content=" + content + ", destination=" + destination + "]";
  }

}
