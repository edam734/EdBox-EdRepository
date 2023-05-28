package com.ed.repository.utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

public class AppUtils {

  public static void deleteDirectory(Path directory) throws IOException {
    Files.walk(directory).sorted(Comparator.reverseOrder()).forEach(AppUtils::deleteFile);
  }

  public static void deleteFile(Path path) {
    try {
      Files.delete(path);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
