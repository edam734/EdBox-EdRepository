package com.ed.repository.filesystem;

import static java.nio.file.StandardOpenOption.APPEND;
import static java.nio.file.StandardOpenOption.CREATE;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class RepositoryManager {

  final static Charset ENCODING = StandardCharsets.UTF_8;

  /**
   * Place a new file in the appropriate location on the server, write to the ".index.txt" file with
   * the uploader's name of this version, and update the file's name version
   * 
   * @param in The input stream that carries the data
   * @param target The path to be modified
   * @param username who uploaded this file
   * @param options Some copy options
   * @return true if was successful
   * @throws IOException
   */
  public static boolean receiveFile(final InputStream in, Path target, final String username,
      CopyOption... options) throws IOException {

    int version = 0;

    UnfoldPathResolver unfoldPathResolver = new UnfoldPathResolver(target);
    final Path directory = unfoldPathResolver.getDirectory();
    final Path indexFile = unfoldPathResolver.getIndexFile();

    CreationDirectoryResult wasDirectoryCreatedResult = makeDirectory(directory.toFile());
    switch (wasDirectoryCreatedResult) {
      case NOT_CREATED:
        return false;
      case ALREADY_EXISTS:
        version = getMostRecentVersion(indexFile);
        break;
      case CREATED:
        break;
      default:
        throw new IllegalArgumentException("Unexpected value: " + wasDirectoryCreatedResult);
    }
    int newVersion = ++version;
    writeIndexFileNewEntry(indexFile.toFile(), Integer.toString(newVersion), " : ", username);

    target = unfoldPathResolver.getVersionedFilename(newVersion);

    return createFileNewVersion(in, target, options);
  }

  private static int getMostRecentVersion(final Path path) throws IOException {
    List<String> lines = Files.readAllLines(path);
    if (lines.size() == 0) {
      return 0; // empty file for some reason
    }
    String lastLine = lines.get(lines.size() - 1);
    return Integer.parseInt(lastLine.split(" : ")[0]);
  }

  private static void writeIndexFileNewEntry(File file, String... strings) throws IOException {
    boolean alreadyExists = Files.exists(Path.of(file.getPath()));
    // append to an existing file, create file if it doesn't initially exist
    try (OutputStream outputStream =
        Files.newOutputStream(Path.of(file.getPath()), CREATE, APPEND)) {
      if (alreadyExists) {
        outputStream.write(System.lineSeparator().getBytes()); // new line before append
      }
      for (String s : strings) {
        outputStream.write(s.getBytes(ENCODING));
      }
    }
  }

  /*
   * As of Java 7, we have the Files class which contains helper methods to handle operations of
   * I/O.
   *
   * We can use the Files.copy() method to read all bytes from an InputStream and copy them to a
   * local file in a single line of code.
   * 
   */
  private static boolean createFileNewVersion(final InputStream in, final Path target,
      CopyOption... options) throws IOException {
    return Files.copy(in, target, options) > 0;
  }

  /**
   * Creates a new directory if it does not already exist.
   * 
   * @param file A directory
   * @requires file.isDirectory()
   * @return A enum CreationDirectoryResult with the result
   */
  public static CreationDirectoryResult makeDirectory(File file) {
    if (file.exists()) {
      return CreationDirectoryResult.ALREADY_EXISTS;
    } else {
      if (file.mkdirs()) {
        return CreationDirectoryResult.CREATED;
      } else {
        return CreationDirectoryResult.NOT_CREATED;
      }
    }
  }

  static enum CreationDirectoryResult {
    ALREADY_EXISTS, CREATED, NOT_CREATED;
  }
}
