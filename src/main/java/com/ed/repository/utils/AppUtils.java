package com.ed.repository.utils;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

  /**
   * Get the version number X of a string like this: folder1/folder2/.../filename-vX.extension
   * 
   * @param filename
   * @return
   */
  public static int getVersionFromFilename(String filename) {
    Pattern p = Pattern.compile("-v.*?\\.");
    Matcher m = p.matcher(filename);
    if (m.find()) {
      String val = m.group().subSequence(2, m.group().length() - 1).toString();
      if (!val.isEmpty()) {
        return Integer.valueOf(val);
      }
    }
    return -1;
  }
}
