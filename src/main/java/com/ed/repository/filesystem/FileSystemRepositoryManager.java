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

public class FileSystemRepositoryManager extends RepositoryManager {

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
      boolean wasCreated = FileSystemEnvironmentResolver.createFile(in, path, username, options);
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
      if (PathParser.isRepoFormat(path)) {
        Pack pack = FileSystemEnvironmentResolver.getFile(path);
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
      // must be a reposiroty file
      if (PathParser.isRepoFormat(path)) {
        Pack pack = Pack.createPack(path);
        subfiles.add(pack);
      }
    }
    return subfiles;
  }


  @Override
  public RepositoryManager createRepositoryManager() {
    return new FileSystemRepositoryManager();
  }

}
