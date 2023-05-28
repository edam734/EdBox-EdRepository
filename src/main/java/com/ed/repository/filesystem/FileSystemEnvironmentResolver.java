package com.ed.repository.filesystem;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.CopyOption;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import com.ed.repository.exceptions.FileVersionException;

public class FileSystemEnvironmentResolver {

  final static Charset ENCODING = StandardCharsets.UTF_8;


  public static boolean createFile(final InputStream in, Path path, final String username,
      CopyOption... options) throws IOException {
    ClientFileResolver fileResolver = new ClientFileResolver(path);

    // create repository's directory (if doesn't exist)
    Path versionsFolder = fileResolver.getRepositoryDirectoryPath();
    boolean wasDirectoryCreated = FileSystemEnvironmentResolver.createDirectory(versionsFolder);
    if (!wasDirectoryCreated) {
      return false;
    }
    // write new entry in index file
    Path indexFilePath = fileResolver.getIndexFilePath();
    createFileLazily(indexFilePath);
    
    // the next version is 1 up from the latest one in the archive
    int latestVersion = fileResolver.getLatestVersion();
    int nextVersion = ++latestVersion;
    IndexFileEntry.writeEntry(indexFilePath, nextVersion, username);

    // write content in repository's file
    Path target = fileResolver.getRepositoryFilePath(nextVersion);
    boolean suceess = Files.copy(in, target, options) > 0;
    return suceess;
  }

  /**
   * Creates a file only if it doesn't exist. Else, do nothing
   * 
   * @param path - the path to the new file
   * @throws IOException if an I/O error occurs or the parent directory does not exist
   */
  private static void createFileLazily(Path path) throws IOException {
    try {
      Files.createFile(path);
    } catch (FileAlreadyExistsException e) {
    }
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
   * @throws FileVersionException
   */
  public static Pack getFile(Path path, int version) throws IOException {
    // file HAS to be a directory in format. dir1/dir2.../filename#extension/

    RepositoryFileResolver fileResolver = new RepositoryFileResolver(path);
    int latestVersion = fileResolver.getLatestVersion();
    if (version == -1) {
      version = latestVersion; // set search for the latest version
    }
    if (version > latestVersion) {
      String errorMsg = "Version %s bigger than the latest version %s";
      throw new FileVersionException(String.format(errorMsg, version, latestVersion));
    }
    Path packContent = fileResolver.getRepositoryFilePath(version);
    Pack pack = Pack.createPack(packContent);
    return pack;
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
}
