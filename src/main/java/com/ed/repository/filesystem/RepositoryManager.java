package com.ed.repository.filesystem;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import com.ed.repository.exceptions.VersionGreaterThanLatestVersionException;

public class RepositoryManager {

  /**
   * Place a new file in the appropriate location on the server, write to the ".index.txt" file with
   * the uploader's name of this version, and update the file's name version
   * 
   * @param in The input stream that carries the data
   * @param clientFile The path to convert into a new versioning repo
   * @param username who uploaded this file
   * @param options Some copy options
   * @return true if was successful
   * @throws IOException
   */
  public static boolean receiveFile(final InputStream in, Path clientFile, final String username,
      CopyOption... options) throws IOException {

    ServerRepoEnvironmentResolver serverRepoEnvironmentResolver =
        new ServerRepoEnvironmentResolver(clientFile);
    final Path fileVersionsFolder = serverRepoEnvironmentResolver.getDirectory();

    boolean wasDirectoryCreated = makeDirectory(fileVersionsFolder);
    if (!wasDirectoryCreated) {
      return false;
    }
    return serverRepoEnvironmentResolver.createFileNewVersion(in, username, options);
  }

  /**
   * Creates a new directory if it does not already exist.
   * 
   * @param path - A path to a directory
   * @requires Files.isDirectory(path)
   * @return true if there's a directory of this file's path
   * @throws IOException
   */
  public static boolean makeDirectory(Path path) throws IOException {
    if (Files.exists(path)) {
      return true;
    } else {
      if (Files.createDirectories(path) != null) {
        return true;
      }
      return false;
    }
  }


  /**
   * Returns all subfiles of this {@code file} with the name adapted to go to the user.
   * <p>
   * After this method is called, it's necessary to treat the name that goes within WrappedFile,
   * removing the part that corresponds to the name of the user's repository.
   * 
   * @param path - The file's path to search
   * @return All subfiles of this {@code file}
   * @throws IOException
   */
  public static List<WrappedFile> getSubfiles(Path path) throws IOException {
    return getSubfiles(path, new ArrayList<>());
  }

  private static List<WrappedFile> getSubfiles(Path path, List<WrappedFile> subfiles)
      throws IOException {
    if (Files.isDirectory(path)) {
      // verify if it's a directory that is a representation of a file in the server's repository
      if (ServerRepoEnvironmentResolver.isVersioned(path)) {
        WrappedFile wrappedFile = getFile(path);
        subfiles.add(wrappedFile);
      }
      // it's a common directory
      else {
        List<Path> contentDirectory = Files.list(path).collect(Collectors.toList());
        for (Path subfile : contentDirectory) {
          getSubfiles(subfile, subfiles);
        }
      }
    }
    // else, it's a file
    else {
      // must be a versioned file
      if (ServerRepoEnvironmentResolver.isVersioned(path)) {
        WrappedFile wrappedFile = parseFile(path);
        subfiles.add(wrappedFile);
      }
    }
    return subfiles;
  }

  private static WrappedFile parseFile(Path path) {

    // if (!Files.isDirectory(path)) {
    //
    // }
    // Path directory = path.toPath();
    // if (!path.isDirectory()) {
    // directory = directory.getParent();
    // }
    Path unversionedPath = ServerRepoEnvironmentResolver.getUnversionedFilename(path);
    return new WrappedFile(path, unversionedPath);
  }

  /**
   * Get latest version of a file
   * 
   * @param path - A representation of a file in the server's repository
   * @return
   * @throws IOException
   */
  public static WrappedFile getFile(Path path) throws IOException {
    return getFile(path, -1);
  }

  /**
   * Get specific version of a file
   * 
   * @param path - A representation of a file in the server's repository
   * @param version - The desired version of the file
   * @return
   * @throws IOException
   */
  public static WrappedFile getFile(Path path, int version) throws IOException {
    // file HAS to be a directory in format. dir1/dir2.../filename#extension/

    ServerRepoEnvironmentResolver serverRepoEnvironmentResolver =
        new ServerRepoEnvironmentResolver(path);
    int latestVersion = serverRepoEnvironmentResolver.getLatestVersion();
    if (version == -1) {
      // set search for the latest version
      version = latestVersion;
    }
    if (version > latestVersion) {
      throw new VersionGreaterThanLatestVersionException(
          String.format("Version %s bigger than the latest version %s", version, latestVersion));
    }
    Path content = serverRepoEnvironmentResolver.getVersionedPath(version);
    WrappedFile wrappedFile = parseFile(content);
    return wrappedFile;
//    Path destination = ServerRepoEnvironmentResolver.getUnversionedFilename(content);
//    return new WrappedFile(content, destination);
  }

}
