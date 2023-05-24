package com.ed.repository.filesystem;

import static java.nio.file.StandardOpenOption.APPEND;
import static java.nio.file.StandardOpenOption.CREATE;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class FileEntry {

  private final int key;
  private final String value;

  public FileEntry(int key, String value) {
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
    boolean exists = Files.exists(file);
    // append to an existing file, create file if it doesn't initially exist
    try (OutputStream outputStream = Files.newOutputStream(file, CREATE, APPEND)) {
//      if (exists) {
//        outputStream.write(System.lineSeparator().getBytes()); // new line before append
//      }
      String entry = String.format("%d%s%s", key, " : ", value);
      outputStream.write(entry.getBytes(ServerRepoEnvironmentResolver.ENCODING));
      outputStream.write(System.lineSeparator().getBytes());
    }
  }

  public static FileEntry readEntry(Path file) throws IOException {
    FileEntry entry = null;
    boolean exists = Files.exists(file);
    if (!exists) {
      // file's doesn't exist
      entry = new FileEntry(ServerRepoEnvironmentResolver.START_VERSION, null);
    } else {
      List<String> lines = Files.readAllLines(file);
      if (lines.size() == 0) {
        // file's empty
        entry = new FileEntry(ServerRepoEnvironmentResolver.START_VERSION, null);
      } else {
        String lastLine = lines.get(lines.size() - 1);
        String[] parts = lastLine.split(" : ");
        int key = Integer.parseInt(parts[0]);
        String value = parts[1];
        entry = new FileEntry(key, value);
      }
    }
    return entry;
  }
}
