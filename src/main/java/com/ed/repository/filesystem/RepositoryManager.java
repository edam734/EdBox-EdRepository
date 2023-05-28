package com.ed.repository.filesystem;

import java.io.InputStream;
import java.nio.file.CopyOption;
import java.nio.file.Path;
import java.util.List;
import com.ed.repository.exceptions.RepositoryManagementException;

public abstract class RepositoryManager {

  public boolean put(final InputStream in, Path clientFile, final String username,
      CopyOption... options) throws RepositoryManagementException {
    RepositoryManager RepositoryManager = createRepositoryManager();
    return RepositoryManager.put(in, clientFile, username, options);
  }

  public List<Pack> get(Path path) throws RepositoryManagementException {
    RepositoryManager RepositoryManager = createRepositoryManager();
    return RepositoryManager.get(path);
  }

  public abstract RepositoryManager createRepositoryManager();
}
