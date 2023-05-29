package com.ed.repository;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.springframework.util.ResourceUtils;
import com.ed.repository.exceptions.FileVersionException;
import com.ed.repository.filesystem.FileSystemEnvironmentResolver;
import com.ed.repository.filesystem.FileSystemRepositoryManager;
import com.ed.repository.filesystem.Pack;
import com.ed.repository.filesystem.PathParser;
import com.ed.repository.filesystem.RepositoryFileResolver;
import com.ed.repository.filesystem.RepositoryManager;
import com.ed.repository.utils.AppUtils;

@TestInstance(Lifecycle.PER_CLASS)
public class FileSystemRepositoryManagerTest {

  static final String INPUT_LOCATION = "repo/".replace("/", File.separator);

  static final String OUTPUT_TEST_1 = "output_tests_1/".replace("/", File.separator);
  static final String OUTPUT_TEST_2 = "output_tests_2/".replace("/", File.separator);

  @BeforeAll // delete all output folders to start fresh
  public void init() throws IOException {
    deleteRepo(OUTPUT_TEST_1);
    deleteRepo(OUTPUT_TEST_2);
  }

  private void deleteRepo(String repo) throws FileNotFoundException, IOException {
    Path output = ResourceUtils.getFile(repo).toPath();
    if (Files.exists(output)) {
      AppUtils.deleteDirectory(output);
    }
  }


  /* received file related tests */


  @Test
  public void testReceiveOneFile_ThenWriteToDiskWithSuccess() throws IOException {

    Path file = ResourceUtils.getFile(INPUT_LOCATION + "test1.TXT").toPath();

    InputStream inStream = new FileInputStream(file.toString());

    RepositoryManager repositoryManager = new FileSystemRepositoryManager();
    boolean result =
        repositoryManager.put(inStream, Path.of(OUTPUT_TEST_1, file.toString()), "edam734");

    Assertions.assertTrue(result);

    /* check if the files were really created */

    // created the file 'test1.index.txt'
    Path indexFile = ResourceUtils
        .getFile(OUTPUT_TEST_1 + "repo/test1#TXT/test1.index.txt".replace("/", File.separator))
        .toPath();
    Assertions.assertTrue(Files.exists(indexFile));
    List<String> allIndexLines = Files.readAllLines(indexFile);
    Assertions.assertEquals(1, allIndexLines.size());
    String lineIndexFile = allIndexLines.get(0);
    Assertions.assertEquals("1 : edam734", lineIndexFile);

    // created the file 'test1-v1.TXT'
    Path newFile = ResourceUtils
        .getFile(OUTPUT_TEST_1 + "repo/test1#TXT/test1-v1.TXT".replace("/", File.separator))
        .toPath();
    Assertions.assertTrue(Files.exists(newFile));
    List<String> allNewFileLines = Files.readAllLines(newFile);
    Assertions.assertEquals(1, allNewFileLines.size());
    String lineNewFile = allNewFileLines.get(0);
    Assertions.assertEquals("My test1", lineNewFile);
  }

  @Test
  public void testReceiveFileThreeTimes_ThenWriteAndUpdateVersionWithSuccess() throws IOException {

    Path file = ResourceUtils.getFile(INPUT_LOCATION + "test2.TXT").toPath();

    RepositoryManager repositoryManager = new FileSystemRepositoryManager();
    boolean result1 = repositoryManager.put(new FileInputStream(file.toString()),
        Path.of(OUTPUT_TEST_2, file.toString()), "maria");
    Assertions.assertTrue(result1);
    boolean result2 = repositoryManager.put(new FileInputStream(file.toString()),
        Path.of(OUTPUT_TEST_2, file.toString()), "johnny_cash");
    Assertions.assertTrue(result2);
    boolean result3 = repositoryManager.put(new FileInputStream(file.toString()),
        Path.of(OUTPUT_TEST_2, file.toString()), "O'Brien");
    Assertions.assertTrue(result3);

    // created the file 'test2.index.txt'
    Path indexFile = ResourceUtils
        .getFile(OUTPUT_TEST_2 + "repo/test2#TXT/test2.index.txt".replace("/", File.separator))
        .toPath();
    Assertions.assertTrue(Files.exists(indexFile));
    List<String> allIndexLines = Files.readAllLines(indexFile);
    Assertions.assertEquals(3, allIndexLines.size());
    Assertions.assertEquals("1 : maria", allIndexLines.get(0));
    Assertions.assertEquals("2 : johnny_cash", allIndexLines.get(1));
    Assertions.assertEquals("3 : O'Brien", allIndexLines.get(2));

    // created the file to the third version 'test2-v3.TXT'
    Path newFile = ResourceUtils
        .getFile(OUTPUT_TEST_2 + "repo/test2#TXT/test2-v3.TXT".replace("/", File.separator))
        .toPath();
    Assertions.assertTrue(Files.exists(newFile));
    List<String> allNewFileLines = Files.readAllLines(newFile);
    Assertions.assertEquals(1, allNewFileLines.size());
    Assertions.assertEquals("My test for several versions", allNewFileLines.get(0));
  }


  /* get subfiles related tests */


  @Test
  public void testGetAllSubfiles_edam734Repo() throws IOException {
    Path directory = ResourceUtils.getFile(INPUT_LOCATION + "edam734").toPath();

    RepositoryManager repositoryManager = new FileSystemRepositoryManager();
    List<Pack> files = repositoryManager.get(directory);

    Assertions.assertEquals(2, files.size());

    Pack wrappedFile1 = Pack
        .createPack(Paths.get("repo/edam734/test2#TXT/test2-v3.txt".replace("/", File.separator)));
    Pack wrappedFile2 = Pack
        .createPack(Paths.get("repo/edam734/test3#TXT/test3-v2.txt".replace("/", File.separator)));
    List<Pack> expectedList = new ArrayList<>();
    expectedList.add(wrappedFile1);
    expectedList.add(wrappedFile2);

    Assertions.assertEquals(expectedList, files);
  }

  @Test
  public void testGetAllSubfiles_butInputIsAFile() throws IOException {
    Path file = ResourceUtils.getFile(INPUT_LOCATION + "edam734/test2#TXT/test2-v1.TXT").toPath();
    RepositoryManager repositoryManager = new FileSystemRepositoryManager();
    List<Pack> files = repositoryManager.get(file);

    Assertions.assertEquals(1, files.size());

    Pack wrappedFile = Pack
        .createPack(Paths.get("repo/edam734/test2#TXT/test2-v1.TXT".replace("/", File.separator)));
    List<Pack> expectedList = new ArrayList<>();
    expectedList.add(wrappedFile);

    Assertions.assertEquals(expectedList, files);
  }

  @Test
  public void testGetLatestFileVersion() throws IOException {
    Path directory = ResourceUtils
        .getFile(INPUT_LOCATION + "edam734/test2#TXT".replace("/", File.separator)).toPath();
    Pack file = FileSystemEnvironmentResolver.getFile(directory);

    // the latest version is 3
    String filename = Objects.toString(file.getContent().getFileName());
    Assertions.assertEquals(3, PathParser.getVersionFromFilename(filename));
  }

  @Test
  public void testGetSpecificFileVersion() throws IOException {
    Path directory = ResourceUtils
        .getFile(INPUT_LOCATION + "edam734/test2#TXT".replace("/", File.separator)).toPath();
    int wantedVersion = 2;
    Pack file = FileSystemEnvironmentResolver.getFile(directory, wantedVersion);

    String filename = Objects.toString(file.getContent().getFileName());
    Assertions.assertEquals(wantedVersion, PathParser.getVersionFromFilename(filename));
  }

  @Test
  public void testGetSpecificFileVersion_butVersionIsBiggerThanLatestVersion() throws IOException {
    Path directory = ResourceUtils
        .getFile(INPUT_LOCATION + "edam734/test2#TXT".replace("/", File.separator)).toPath();
    // latest version is 3, so we ask for version 4
    FileVersionException exception = Assertions.assertThrows(FileVersionException.class,
        () -> FileSystemEnvironmentResolver.getFile(directory, 4));
    Assertions.assertEquals(String.format("Version %s bigger than the latest version %s", 4, 3),
        exception.getMessage());
  }


  @Test
  public void testGetUnversionedName_butNameWithSeveralMarkersOfVersion() throws IOException {
    String filename1 = "dir1/dir2/test-v4f-vfs-v_-v#TXT/test-v4f-vfs-v_-v-v2.TXT";
    String filename2 = "dir1/dir2/test-v4f-v67-v_-v#TXT/test-v4f-v67-v_-v wdwe-v2.TXT";
    String filename3 = "dir1/dir2/test#TXT/test-v2.TXT";
    String filename4 = "dir1/dir2/test_without_version#TXT/test_without_version.TXT";
    RepositoryFileResolver rfr1 = new RepositoryFileResolver(Paths.get(filename1));
    Path actual1 = rfr1.getClientFilePath();
    RepositoryFileResolver rfr2 = new RepositoryFileResolver(Paths.get(filename2));
    Path actual2 = rfr2.getClientFilePath();
    RepositoryFileResolver rfr3 = new RepositoryFileResolver(Paths.get(filename3));
    Path actual3 = rfr3.getClientFilePath();
    RepositoryFileResolver rfr4 = new RepositoryFileResolver(Paths.get(filename4));
    Path actual4 = rfr4.getClientFilePath();

    String expected1 = "dir1/dir2/test-v4f-vfs-v_-v.TXT".replace("/", File.separator);
    String expected2 = "dir1/dir2/test-v4f-v67-v_-v wdwe.TXT".replace("/", File.separator);
    String expected3 = "dir1/dir2/test.TXT".replace("/", File.separator);
    String expected4 = "dir1/dir2/test_without_version.TXT".replace("/", File.separator);
    Assertions.assertEquals(expected1, actual1.toString());
    Assertions.assertEquals(expected2, actual2.toString());
    Assertions.assertEquals(expected3, actual3.toString());
    Assertions.assertEquals(expected4, actual4.toString());
  }


  @Test
  public void testGetUnversionedName_butInputIsADirectory() throws IOException {
    String filename = "dir1/dir2/test#TXT";
    RepositoryFileResolver rfr = new RepositoryFileResolver(Paths.get(filename));
    Path actual = rfr.getClientFilePath();
    // Path actual = FileSystemEnvironmentResolver.getUnversionedFilename(Paths.get(filename));

    String expected = "dir1/dir2/test.TXT".replace("/", File.separator);
    Assertions.assertEquals(expected, actual.toString());
  }

//  @Test
//  @Disabled
//  public void testGetUnversionedName_butAlreadyUnversioned() throws IOException {
//    String filename = "dir1/dir2/test.TXT";
//    ClientFileResolver rfr = new ClientFileResolver(Paths.get(filename));
//    Path actual = rfr.getClientFilePath();
//    // Path actual = FileSystemEnvironmentResolver.getUnversionedFilename(Paths.get(filename));
//
//    String expected = "dir1/dir2/test.TXT".replace("/", File.separator);
//    Assertions.assertEquals(expected, actual.toString());
//  }

  @Test
  public void testGetAllSubfiles_johnn50Repo() throws IOException {
    Path directory = ResourceUtils.getFile(INPUT_LOCATION + "johnny50").toPath();
    RepositoryManager repositoryManager = new FileSystemRepositoryManager();
    List<Pack> files = repositoryManager.get(directory);

    Assertions.assertEquals(2, files.size());

    Pack wrappedFile1 = Pack.createPack(
        Paths.get("repo/johnny50/city/house/room1#TXT/room1-v2.txt".replace("/", File.separator)));
    Pack wrappedFile2 = Pack
        .createPack(Paths.get("repo/johnny50/test2#TXT/test2-v3.txt".replace("/", File.separator)));
    List<Pack> expectedList = new ArrayList<>();
    expectedList.add(wrappedFile1);
    expectedList.add(wrappedFile2);

    Assertions.assertEquals(expectedList, files);
  }

  @Test
  public void testGetAllFilesDirectlyOfFolder() throws IOException {
    Path directory = ResourceUtils
        .getFile(INPUT_LOCATION + "johnny50/test2#TXT".replace("/", File.separator)).toPath();
    RepositoryManager repositoryManager = new FileSystemRepositoryManager();
    List<Pack> files = repositoryManager.get(directory);

    Pack wrappedFile = Pack
        .createPack(Paths.get("repo/johnny50/test2#TXT/test2-v3.txt".replace("/", File.separator)));

    Assertions.assertEquals(wrappedFile, files.get(0));
  }


  @Test
  public void testGetAllFilesDirectlyOfAVersionedFile() throws IOException {
    Path directory = ResourceUtils
        .getFile(INPUT_LOCATION + "johnny50/test2#TXT/test2-v1.TXT".replace("/", File.separator))
        .toPath();
    RepositoryManager repositoryManager = new FileSystemRepositoryManager();
    List<Pack> files = repositoryManager.get(directory);

    Pack wrappedFile = Pack
        .createPack(Paths.get("repo/johnny50/test2#TXT/test2-v1.TXT".replace("/", File.separator)));

    Assertions.assertEquals(wrappedFile, files.get(0));
  }

  @Test
  public void testGetAllFilesDirectlyOfAUnversionedFile() throws IOException {
    Path directory = ResourceUtils
        .getFile(INPUT_LOCATION + "steven123/test100.TXT".replace("/", File.separator)).toPath();
    RepositoryManager repositoryManager = new FileSystemRepositoryManager();
    List<Pack> files = repositoryManager.get(directory);

    // don't return unversioned files
    // even dough, server's repo could have unversioned files
    Assertions.assertTrue(files.isEmpty());
  }
}
