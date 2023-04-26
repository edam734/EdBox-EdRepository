package com.ed.repository.utils;

import java.io.File;

public class AppUtils {

  /*
   * from https://www.programiz.com/java-programming/examples/delete-directory
   * 
   */
  public static void deleteDirectory(File directory) {

    // if the file is directory or not
    if (directory.isDirectory()) {
      File[] files = directory.listFiles();

      // if the directory contains any file
      if (files != null) {
        for (File file : files) {

          // recursive call if the subdirectory is non-empty
          deleteDirectory(file);
        }
      }
    }
    directory.delete();
  }
}
