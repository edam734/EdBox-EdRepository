package com.ed.repository.filesystem;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import com.ed.repository.exceptions.RepositoryManagementException;
import com.ed.repository.exceptions.VersionGreaterThanLatestVersionException;

public class DiskRepositoryManager extends RepositoryManager {

  /**
   * Place a new file in the appropriate location on the server, write to the ".index.txt" file with
   * the uploader's name of this version, and update the file's name version
   * 
   * @param in - the input stream that carries the data
   * @param path - the file's path of the client
   * @param username - who uploaded this file
   * @param options - some copy options
   * @return true if was successful
   * @throws RepositoryManagementException
   */
  public boolean put(final InputStream in, Path path, final String username, CopyOption... options)
      throws RepositoryManagementException {
    try {
      boolean wasCreated = ServerRepoEnvironmentResolver.createFile(in, path, username, options);
      return wasCreated;
    } catch (IOException e) {
      throw new RepositoryManagementException(e.getMessage());
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
   * @throws RepositoryManagementException
   */
  public List<Pack> get(Path path) throws RepositoryManagementException {
    try {
      return get(path, new ArrayList<>());
    } catch (IOException e) {
      throw new RepositoryManagementException(e.getMessage());
    }
  }

  private static List<Pack> get(Path path, List<Pack> subfiles) throws IOException {
    if (Files.isDirectory(path)) {
      // verify if it's a directory that is a representation of a file in the server's repository
      if (ServerRepoEnvironmentResolver.isVersioned(path)) {
        Pack pack = getFile(path);
        subfiles.add(pack);
      }
      // it's a common directory
      else {
        List<Path> contentDirectory = Files.list(path).collect(Collectors.toList());
        for (Path subpaths : contentDirectory) {
          get(subpaths, subfiles);
        }
      }
    }
    // else, it's a file
    else {
      // must be a versioned file
      if (ServerRepoEnvironmentResolver.isVersioned(path)) {
        Pack pack = parseFile(path);
        subfiles.add(pack);
      }
    }
    return subfiles;
  }

  private static Pack parseFile(Path path) {
    Path unversionedPath = ServerRepoEnvironmentResolver.getUnversionedFilename(path);
    return new Pack(path, unversionedPath);
  }

  /**
   * Get latest version of a file
   * 
   * @param path - A representation of a file in the server's repository
   * @return
   * @throws IOException
   */
  public static Pack getFile(Path path) throws IOException {
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
  public static Pack getFile(Path path, int version) throws IOException {
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
    Pack pack = parseFile(content);
    return pack;
  }

  @Override
  public RepositoryManager createRepositoryManager() {
    return new DiskRepositoryManager();
  }

}
