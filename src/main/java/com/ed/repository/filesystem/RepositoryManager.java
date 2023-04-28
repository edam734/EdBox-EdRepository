package com.ed.repository.filesystem;

import static java.nio.file.StandardOpenOption.APPEND;
import static java.nio.file.StandardOpenOption.CREATE;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import com.ed.repository.exceptions.NotPathToAServerFileException;
import com.ed.repository.exceptions.VersionGreaterThanLatestVersionException;

public class RepositoryManager {

  final static Charset ENCODING = StandardCharsets.UTF_8;

  static enum CreationDirectoryResult {
    ALREADY_EXISTS, CREATED, NOT_CREATED;
  }

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

    ServerPathResolver serverPathResolver = new ServerPathResolver(target);
    final Path directory = serverPathResolver.getDirectory();
    final Path indexFile = serverPathResolver.getIndexFile();

    CreationDirectoryResult wasDirectoryCreatedResult = makeDirectory(directory.toFile());
    switch (wasDirectoryCreatedResult) {
      case NOT_CREATED:
        return false;
      case ALREADY_EXISTS:
        version = getLatestVersion(indexFile);
        break;
      case CREATED:
        break;
      default:
        throw new IllegalArgumentException("Unexpected value: " + wasDirectoryCreatedResult);
    }
    int newVersion = ++version;
    writeIndexFileNewEntry(indexFile.toFile(), Integer.toString(newVersion), " : ", username);

    target = serverPathResolver.getVersionedFilename(newVersion);
    return createFileNewVersion(in, target, options);
  }

  private static int getLatestVersion(final Path indexPath) throws IOException {
    List<String> lines = Files.readAllLines(indexPath);
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


  /**
   * Returns all subfiles of this {@code file} with the name adapted to go to the user.
   * <p>
   * After this method is called, it's necessary to treat the name that goes within BinamedFile,
   * removing the part that corresponds to the name of the user's repository.
   * 
   * @param file The file to search
   * @return All subfiles of this {@code file}
   * @throws IOException
   */
  public static List<BinamedFile> getSubfiles(File file) throws IOException {
    return getSubfiles(file, new ArrayList<>());
  }

  private static List<BinamedFile> getSubfiles(File file, List<BinamedFile> subfiles)
      throws IOException {
    try {
      BinamedFile binamedFile = getFile(file);
      subfiles.add(binamedFile);
    } catch (NotPathToAServerFileException e) {
      if (file.isDirectory()) {
        for (File subfile : file.listFiles()) {
          getSubfiles(subfile, subfiles);
        }
        return subfiles;
      } else {
        BinamedFile binamedFile = parseFile(file);
        subfiles.add(binamedFile);
      }
    }
    return subfiles;
  }

  private static BinamedFile parseFile(File file) {
    Path directory = file.toPath();
    if (!file.isDirectory()) {
      directory = directory.getParent();
    }
    ClientPathResolver clientPathResolver = new ClientPathResolver(directory);
    Path unversionedPath = clientPathResolver.getUnversionedFilename(file.toPath());
    return new BinamedFile(file, unversionedPath);
  }

  /**
   * Get latest version of a file
   * 
   * @param file A file with 'MARK' on his path
   * @return
   * @throws IOException
   */
  public static BinamedFile getFile(File file) throws IOException, NotPathToAServerFileException {
    return getFile(file, -1);
  }

  /**
   * Get specific version of a file
   * 
   * @param file A file with 'MARK' on his path
   * @param version The desired version of the file
   * @return
   * @throws IOException
   */
  public static BinamedFile getFile(File file, int version)
      throws IOException, NotPathToAServerFileException {
    if (!file.getName().contains(PathResolver.MARK)) {
      throw new NotPathToAServerFileException("Is not a server file path");
    }

    ClientPathResolver clientPathResolver = new ClientPathResolver(file.toPath());
    int latestVersion = getLatestVersion(clientPathResolver.getIndexFile());
    if (version == -1) {
      // set latest version
      version = latestVersion;
    }
    if (version > latestVersion) {
      throw new VersionGreaterThanLatestVersionException(
          String.format("Version %s bigger than the latest version %s", version, latestVersion));
    }
    File subfile = new File(clientPathResolver.getVersionedFilename(version).toString());
    Path unversionedPath = clientPathResolver.getUnversionedFilename(subfile.toPath());
    return new BinamedFile(subfile, unversionedPath);
  }

}
