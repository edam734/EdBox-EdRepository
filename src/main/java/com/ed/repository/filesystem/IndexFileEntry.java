package com.ed.repository.filesystem;

import static java.nio.file.StandardOpenOption.APPEND;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class IndexFileEntry {

  private final int key;
  private final String value;

  public IndexFileEntry(int key, String value) {
    super();
    this.key = key;
    this.value = value;
  }

  public int getKey() {
    return key;
  }

  public String getValue() {
    return value;
  }

  public static void writeEntry(Path file, int key, String value) throws IOException {
    // append to an existing file
    try (OutputStream outputStream = Files.newOutputStream(file, APPEND)) {
      String entry = String.format("%d%s%s", key, " : ", value);
      byte[] bytes = entry.getBytes(FileSystemEnvironmentResolver.ENCODING);
      outputStream.write(bytes);
      outputStream.write(System.lineSeparator().getBytes()); // change line
    }
  }

  public static IndexFileEntry readEntry(Path file) throws IOException {
    IndexFileEntry entry = new IndexFileEntry(0, null);
    boolean exists = Files.exists(file);
    if (exists) {
      List<String> lines = Files.readAllLines(file);
      if (lines.size() != 0) {
        String lastLine = lines.get(lines.size() - 1);
        String[] parts = lastLine.split(" : ");
        int key = Integer.parseInt(parts[0]);
        String value = parts[1];
        entry = new IndexFileEntry(key, value);
      }
    }
    return entry;
  }
}
