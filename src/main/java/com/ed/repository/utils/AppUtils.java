package com.ed.repository.utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

public class AppUtils {

  public static void deleteDirectory(Path directory) throws IOException {
    List<Path> allContents = Files.list(directory).collect(Collectors.toList());
    if (allContents != null) {
      for (Path path : allContents) {
        deleteDirectory(path);
      }
    }
    Files.delete(directory);
  }

}
