package com.ed.repository.filesystem;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.CopyOption;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
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

    boolean wasDirectoryCreated = makeDirectory(fileVersionsFolder.toFile());
    if (!wasDirectoryCreated) {
      return false;
    }
    return serverRepoEnvironmentResolver.createFileNewVersion(in, username, options);
  }

  /**
   * Creates a new directory if it does not already exist.
   * 
   * @param file - A directory
   * @requires file.isDirectory()
   * @return true if there's a directory of this file's path
   */
  public static boolean makeDirectory(File file) {
    if (file.exists()) {
      return true;
    } else {
      if (file.mkdirs()) {
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
   * @param file - The file to search
   * @return All subfiles of this {@code file}
   * @throws IOException
   */
  public static List<WrappedFile> getSubfiles(File file) throws IOException {
    return getSubfiles(file, new ArrayList<>());
  }

  private static List<WrappedFile> getSubfiles(File file, List<WrappedFile> subfiles)
      throws IOException {
    if (file.isDirectory()) {
      // verify if it's a directory that is a representation of a file in the server's repository
      if (ServerRepoEnvironmentResolver.isVersioned(file.toPath())) {
        WrappedFile wrappedFile = getFile(file);
        subfiles.add(wrappedFile);
      }
      // it's a common directory
      else {
        for (File subfile : file.listFiles()) {
          getSubfiles(subfile, subfiles);
        }
      }
    }
    // else, it's a file
    else {
      // must be a versioned file
      if (ServerRepoEnvironmentResolver.isVersioned(file.toPath())) {
        WrappedFile wrappedFile = parseFile(file);
        subfiles.add(wrappedFile);
      }
    }
    return subfiles;
  }

  private static WrappedFile parseFile(File file) {
    Path directory = file.toPath();
    if (!file.isDirectory()) {
      directory = directory.getParent();
    }
    Path unversionedPath = ServerRepoEnvironmentResolver.getUnversionedFilename(file.toPath());
    return new WrappedFile(file, unversionedPath);
  }

  /**
   * Get latest version of a file
   * 
   * @param file - A representation of a file in the server's repository
   * @return
   * @throws IOException
   */
  public static WrappedFile getFile(File file)
      throws IOException/* , NotPathToAServerFileException */ {
    return getFile(file, -1);
  }

  /**
   * Get specific version of a file
   * 
   * @param file - A representation of a file in the server's repository
   * @param version - The desired version of the file
   * @return
   * @throws IOException
   */
  public static WrappedFile getFile(File file, int version) throws IOException {
    // file HAS to be a directory in format. dir1/dir2.../filename#extension/

    ServerRepoEnvironmentResolver serverRepoEnvironmentResolver =
        new ServerRepoEnvironmentResolver(file.toPath());
    int latestVersion = serverRepoEnvironmentResolver.getLatestVersion();
    if (version == -1) {
      // set search for the latest version
      version = latestVersion;
    }
    if (version > latestVersion) {
      throw new VersionGreaterThanLatestVersionException(
          String.format("Version %s bigger than the latest version %s", version, latestVersion));
    }
    File content = new File(serverRepoEnvironmentResolver.getVersionedPath(version).toString());
    Path destination = ServerRepoEnvironmentResolver.getUnversionedFilename(content.toPath());
    return new WrappedFile(content, destination);
  }

}
