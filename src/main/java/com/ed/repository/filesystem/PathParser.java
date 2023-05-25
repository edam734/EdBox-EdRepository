package com.ed.repository.filesystem;

import java.io.File;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.ed.repository.exceptions.TransformPathException;

public class PathParser {

  /**
   * A Matcher with the following groups:
   * 
   * <pre>
   * group(1) - the path 
   * group(2) - the filename
   * group(3) - the file's extension
   * </pre>
   * 
   * @param path - the client file's path
   * @requires path has to be a path to a file
   * @return A Matcher with groups to extract.
   * @throws TransformPathException if it's not a client's path
   */
  public static Matcher clientToRepoMatcher(Path path) {
    if (path.toString().contains("#")) {
      throw new TransformPathException("Should be a client's path.");
    }
    String regex = "^(.*\\/)(.*)([\\.*]\\S+)".replace("/", File.separator);
    Pattern pattern = Pattern.compile(regex);
    Matcher matcher = pattern.matcher(path.toString().replace("/", File.separator));

    return matcher;
  }

  /**
   * Converts:<br>
   * "C:/a/b/.../filename.extension -> C:/a/b/.../filename#EXTENSION/filename-v{version}.extension"
   * 
   * @param clientFormatPath - the client file's path
   * @requires path has to be a path to a file
   * @return a new path in the server's repository for this file
   * @throws TransformPathException if something's wrong with the argument path
   */
  public static Path clientToRepoPath(Path clientFormatPath, int version) {
    Matcher matcher = clientToRepoMatcher(clientFormatPath);
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
   * A Matcher with the following groups:
   * 
   * <pre>
   * group(1) - the path 
   * group(2) - the file's directory
   * group(3) - the filename
   * </pre>
   * 
   * @param path - the repository file's path
   * @requires path has to be a path to a file
   * @return A Matcher with groups to extract.
   * @throws TransformPathException if it's not a repository's path
   */
  public static Matcher repoToClientMatcher(Path path) {
    if (!path.toString().contains("#")) {
      throw new TransformPathException("Should be a repository's path.");
    }
    String regex =
        "^(.*\\/(?=[^#]*#))([^\\/]*[#][^\\/]*)(\\/[^\\/]*)$".replace("/", File.separator);
    Pattern pattern = Pattern.compile(regex);
    Matcher matcher = pattern.matcher(path.toString().replace("/", File.separator));

    return matcher;
  }

  /**
   * Converts:<br>
   * "C:/a/b/.../filename#EXTENSION/filename-v{version}.extension -> C:/a/b/.../filename.extension"
   * 
   * @param repositoryFormatPath - the server's path to a directory or a file
   * @return a new path in the client's repository for this file
   * @throws TransformPathException if something's wrong with the argument path
   */
  public static Path repoToClientPath(Path repositoryFormatPath) {
    Matcher matcher = repoToClientMatcher(repositoryFormatPath);
    boolean matches = matcher.matches();

    if (!matches) {
      throw new TransformPathException("The server's path syntax is invalid");
    }
    String path = matcher.group(1);
    String versionedFilename = matcher.group(3);
    String[] split = versionedFilename.split("-v\\d+");

    String clientFormatPath = path;
    if (split.length > 1) { // concat a unversionedFilename ?
      String unversionedFilename = split[0].substring(1) + split[1];
      clientFormatPath += unversionedFilename;
    }

    return Path.of(clientFormatPath);
  }
}
