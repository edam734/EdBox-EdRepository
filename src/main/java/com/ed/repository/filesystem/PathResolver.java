package com.ed.repository.filesystem;

import java.nio.file.Path;

public interface PathResolver {
  final static String MARK = "#";

  Path getDirectory();

  Path getIndexFile();

  Path getVersionedFilename(final int newVersion);
}
