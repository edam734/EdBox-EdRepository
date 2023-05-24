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
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.ed.repository.exceptions.TransformPathException;

public class ServerRepoEnvironmentResolver {
  final static Charset ENCODING = StandardCharsets.UTF_8;

  final static String MARK = "#";
  final static int START_VERSION = 0;

  private String extension;
  private String filename;
  private Path directory;
  private Path indexFile;

  public static boolean createFile(final InputStream in, Path path, final String username,
      CopyOption... options) throws IOException {
    FileResolver fileResolver = new FileResolver(path);

    // create repository directory (if doesn't exist)
    Path directory = fileResolver.getRepositoryDirectoryPath();
    boolean wasDirectoryCreated = ServerRepoEnvironmentResolver.createDirectory(directory);
    if (!wasDirectoryCreated) {
      return false;
    }
    // write new entry in repository index file
    Path indexFile2 = fileResolver.getIndexFile();
    createFile(indexFile2);
    int latestVersion = fileResolver.getLatestVersion();
    FileEntry.writeEntry(indexFile2, latestVersion, username);

    // write content in repository file
    Path target = fileResolver.getRepositoryFilePath();
    boolean suceess = Files.copy(in, target, options) > 0;
    return suceess;
  }

  private static void createFile(Path path) throws IOException {
    try {
      Files.createFile(path);
    } catch (FileAlreadyExistsException e) {
      // does nothing
    }
  }

  /**
   * @requires path is a file
   * 
   * @param path
   */
  public ServerRepoEnvironmentResolver(Path path) {
    if (isVersioned(path)) {
      path = getUnversionedFilename(path);
    }
    resolve(path);
  }

  public static boolean isVersioned(Path path) {
    return path.toFile().getAbsolutePath().contains(ServerRepoEnvironmentResolver.MARK);
  }

  public Path getDirectory() {
    return directory;
  }

  public Path getIndexFile() {
    return indexFile;
  }

  /*
   * prepares the repository environment for this file (path)
   * 
   */
  private void resolve(Path path) {
    extension = getExtension(path.getFileName().toString());
    filename = removeExtension(path.getFileName().toString());
    Path pathWithoutExtension = Paths.get(removeExtension(path.toString()));
    directory = directoryPath(pathWithoutExtension,
        String.format("%s%s%s", filename, MARK, extension.substring(1).toUpperCase()));
    indexFile = indexFilePath(directory, filename);
  }

  private String getExtension(final String filename) {
    return filename.substring(filename.lastIndexOf('.'));
  }

  private String removeExtension(final String filename) {
    return filename.substring(0, filename.lastIndexOf('.'));
  }

  private Path directoryPath(final Path path, final String directoryName) {
    return Paths.get(path.getParent().toString(), directoryName);
  }

  private Path indexFilePath(final Path path, String name) {
    return Paths.get(path.toString(), (name + ".index.txt"));
  }

  /*
   * As of Java 7, we have the Files class which contains helper methods to handle operations of
   * I/O.
   *
   * We can use the Files.copy() method to read all bytes from an InputStream and copy them to a
   * local file in a single line of code.
   * 
   */
//  public boolean createFileNewVersion(final InputStream in, String username, CopyOption... options)
//      throws IOException {
//    writeNewEntryInIndexFile(username);
//    Path target = getLastestVersionedPath();
//    // write the new file's version
//    boolean suceess = Files.copy(in, target, options) > 0;
//    return suceess;
//  }

  public Path getLastestVersionedPath() throws IOException {
    return getVersionedPath(getLatestVersion());
  }

  public Path getVersionedPath(int version) {
    Path target = Paths.get(String.format("%s%s-v%d%s", directory.toString(),
        (File.separator + filename), version, extension));
    return target;
  }

  public int getLatestVersion() throws IOException {
    List<String> lines = Files.readAllLines(this.indexFile);
    if (lines.size() == 0) {
      return 0; // empty file for some reason
    }
    String lastLine = lines.get(lines.size() - 1);
    return Integer.parseInt(lastLine.split(" : ")[0]);
  }

//  private void writeNewEntryInIndexFile(String username) throws IOException {
//    boolean alreadyExists = Files.exists(this.indexFile);
//    // append to an existing file, create file if it doesn't initially exist
//    try (OutputStream outputStream = Files.newOutputStream(this.indexFile, CREATE, APPEND)) {
//      if (alreadyExists) {
//        outputStream.write(System.lineSeparator().getBytes()); // new line before append
//      }
//      int latestVersion = this.getLatestVersion();
//      String entry = String.format("%d%s%s", ++latestVersion, " : ", username);
//      outputStream.write(entry.getBytes(ENCODING));
//    }
//  }


  /**
   * Get the version number X of a string like this: folder1/folder2/.../filename-vX.extension
   * 
   * @param filename
   * @return
   */
  public static int getVersionFromFilename(String filename) {
    Pattern p = Pattern.compile("-v[0-9]+?\\.");
    Matcher m = p.matcher(filename);
    if (m.find()) {
      String val = m.group().subSequence(2, m.group().length() - 1).toString();
      if (!val.isEmpty()) {
        return Integer.valueOf(val);
      }
    }
    return -1;

  }


  /**
   * Converts a versioned name to an unversioned name
   * 
   * @param path The path to convert
   * @requires path must be a versioned file name
   * @return an unversioned name
   */
  public static Path getUnversionedFilename(Path path) {
    // dir1/dir2.../filename#TXT
    if (path.getFileName().toString().contains(MARK)) {
      String[] parts = path.getFileName().toString().split(MARK);
      String part = parts[0];
      String extension = "." + parts[1].toLowerCase();
      String filename = part + extension;
      return Paths.get(path.getParent().toString(), filename);
    }
    // dir1/dir2.../filename#TXT/filename-vX.txt
    else if (path.getParent().toString().contains(MARK)) {
      String complete = path.getFileName().toString();
      Pattern p = Pattern.compile("-v[0-9]+?\\.");
      Matcher m = p.matcher(complete);
      String newFilename = complete;
      if (m.find()) {
        int idx1 = m.start();
        int idx2 = m.end() - 1;
        newFilename =
            complete.substring(0, idx1).concat(complete.substring(idx2, complete.length()));
      }
      String dir = path.getParent().getParent().toString();
      return Paths.get(dir, newFilename);
    }
    // dir1/dir2.../filename.TXT
    else {
      return path;
    }
  }

  /**
   * Creates a new directory if it does not already exist.
   * 
   * @param path - A path to a directory
   * @requires Files.isDirectory(path)
   * @return true if there's a directory of this file's path
   * @throws IOException
   */
  public static boolean createDirectory(Path path) throws IOException {
    if (Files.exists(path)) {
      return true;
    } else {
      if (Files.createDirectories(path) != null) {
        return true;
      }
      return false;
    }
  }

  // temp
  /**
   * "C:/a/b/.../filename.extension -> C:/a/b/.../filename#EXTENSION/filename-v{version}.extension"
   * 
   * @param clientFormatPath - the client file's path
   * @return a new path in the server's repository for this file
   * @throws TransformPathException if something's wrong with the argument path
   */
  public static Path clientFormatToRepositoryFormatPath(Path clientFormatPath, int version) {
    if (clientFormatPath.toString().contains("#")) {
      throw new TransformPathException("Client path contains marker '#'");
    }
    String regex = "^(.*\\/)(.*)([\\.*]\\S+)".replace("/", File.separator);
    Pattern pattern = Pattern.compile(regex);
    Matcher matcher = pattern.matcher(clientFormatPath.toString().replace("/", File.separator));
    boolean matches = matcher.matches();

    if (!matches) {
      throw new TransformPathException("The client path is invalid");
    }
    String path = matcher.group(1);
    String filename = matcher.group(2);
    String extension = matcher.group(3);

    String repositoryFormatPath =
        String.format("%s%s%s%s%s%s%d%s", path, filename, extension.toUpperCase().replace(".", "#"),
            File.separator, filename, "-v", version, extension);

    return Path.of(repositoryFormatPath);
  }

  /**
   * "C:/a/b/.../filename#EXTENSION/filename-v{version}.extension -> C:/a/b/.../filename.extension"
   * 
   * @param repositoryFormatPath - the server file's path
   * @return a new path in the client's repository for this file
   * @throws TransformPathException if something's wrong with the argument path
   */
  public static Path repositoryFormatToPathClientFormat(Path repositoryFormatPath) {
    String regex =
        "^(.*\\/(?=[^#]*#))([^\\/]*[#][^\\/]*)(\\/[^\\/]*)$".replace("/", File.separator);
    Pattern pattern = Pattern.compile(regex);
    Matcher matcher = pattern.matcher(repositoryFormatPath.toString().replace("/", File.separator));
    boolean matches = matcher.matches();

    if (!matches) {
      throw new TransformPathException("The server's path syntax is invalid");
    }
    String path = matcher.group(1);
    String versionedFilename = matcher.group(3);
    String[] split = versionedFilename.split("-v\\d+");
    String unversionedFilename = split[0].substring(1) + split[1];

    String clientFormatPath = path + unversionedFilename;

    return Path.of(clientFormatPath);
  }

}
