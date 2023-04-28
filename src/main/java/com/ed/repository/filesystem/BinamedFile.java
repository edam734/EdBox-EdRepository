package com.ed.repository.filesystem;

import java.io.File;
import java.nio.file.Path;
import java.util.Objects;

public class BinamedFile {

  private File content;
  private Path targetPath;


  public BinamedFile(File content, Path targetPath) {
    super();
    this.content = content;
    this.targetPath = targetPath;
  }

  public File getContent() {
    return content;
  }

  public Path getTargetPath() {
    return targetPath;
  }

  public void setTargetPath(Path targetPath) {
    this.targetPath = targetPath;
  }

  @Override
  public int hashCode() {
    return Objects.hash(content, targetPath);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    BinamedFile other = (BinamedFile) obj;
    return Objects.equals(content, other.content) && Objects.equals(targetPath, other.targetPath);
  }

  @Override
  public String toString() {
    return "BinamedFile [content=" + content + ", targetPath=" + targetPath + "]";
  }

}
