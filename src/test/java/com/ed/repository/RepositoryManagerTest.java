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
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.springframework.util.ResourceUtils;
import com.ed.repository.exceptions.VersionGreaterThanLatestVersionException;
import com.ed.repository.filesystem.RepositoryManager;
import com.ed.repository.filesystem.ServerRepoEnvironmentResolver;
import com.ed.repository.filesystem.WrappedFile;
import com.ed.repository.utils.AppUtils;

@TestInstance(Lifecycle.PER_CLASS)
public class RepositoryManagerTest {

  static final String INPUT_LOCATION = "repo/".replace("/", File.separator);

  static final String OUTPUT_TEST_1 = "output_tests_1/".replace("/", File.separator);
  static final String OUTPUT_TEST_2 = "output_tests_2/".replace("/", File.separator);

  @BeforeAll // delete all output folders to start fresh
  public void init() throws IOException {
    deleteRepo(OUTPUT_TEST_1);
    deleteRepo(OUTPUT_TEST_2);
  }

  private void deleteRepo(String repo) throws FileNotFoundException, IOException {
    File output = ResourceUtils.getFile(repo);
    if (output.exists()) {
      AppUtils.deleteDirectory(output);
    }
  }


  /* received file related tests */


  @Test
  public void testReceiveOneFile_ThenWriteToDiskWithSuccess() throws IOException {

    File file = ResourceUtils.getFile(INPUT_LOCATION + "test1.TXT");

    InputStream inStream = new FileInputStream(file);

    boolean result = RepositoryManager.receiveFile(inStream,
        Path.of(OUTPUT_TEST_1, file.toPath().toString()), "edam734");

    Assertions.assertTrue(result);

    /* check if the files were really created */

    // created the file 'test1.index.txt'
    File indexFile = ResourceUtils
        .getFile(OUTPUT_TEST_1 + "repo/test1#TXT/test1.index.txt".replace("/", File.separator));
    Assertions.assertTrue(indexFile.exists());
    List<String> allIndexLines = Files.readAllLines(Paths.get(indexFile.getPath()));
    Assertions.assertEquals(1, allIndexLines.size());
    String lineIndexFile = allIndexLines.get(0);
    Assertions.assertEquals("1 : edam734", lineIndexFile);

    // created the file 'test1-v1.TXT'
    File newFile = ResourceUtils
        .getFile(OUTPUT_TEST_1 + "repo/test1#TXT/test1-v1.TXT".replace("/", File.separator));
    Assertions.assertTrue(newFile.exists());
    List<String> allNewFileLines = Files.readAllLines(Paths.get(newFile.getPath()));
    Assertions.assertEquals(1, allNewFileLines.size());
    String lineNewFile = allNewFileLines.get(0);
    Assertions.assertEquals("My test1", lineNewFile);
  }

  @Test
  public void testReceiveFileThreeTimes_ThenWriteAndUpdateVersionWithSuccess() throws IOException {

    File file = ResourceUtils.getFile(INPUT_LOCATION + "test2.TXT");

    boolean result1 = RepositoryManager.receiveFile(new FileInputStream(file),
        Path.of(OUTPUT_TEST_2, file.toPath().toString()), "maria");
    Assertions.assertTrue(result1);
    boolean result2 = RepositoryManager.receiveFile(new FileInputStream(file),
        Path.of(OUTPUT_TEST_2, file.toPath().toString()), "johnny_cash");
    Assertions.assertTrue(result2);
    boolean result3 = RepositoryManager.receiveFile(new FileInputStream(file),
        Path.of(OUTPUT_TEST_2, file.toPath().toString()), "O'Brien");
    Assertions.assertTrue(result3);

    // created the file 'test2.index.txt'
    File indexFile = ResourceUtils
        .getFile(OUTPUT_TEST_2 + "repo/test2#TXT/test2.index.txt".replace("/", File.separator));
    Assertions.assertTrue(indexFile.exists());
    List<String> allIndexLines = Files.readAllLines(Paths.get(indexFile.getPath()));
    Assertions.assertEquals(3, allIndexLines.size());
    Assertions.assertEquals("1 : maria", allIndexLines.get(0));
    Assertions.assertEquals("2 : johnny_cash", allIndexLines.get(1));
    Assertions.assertEquals("3 : O'Brien", allIndexLines.get(2));

    // created the file to the third version 'test2-v3.TXT'
    File newFile = ResourceUtils
        .getFile(OUTPUT_TEST_2 + "repo/test2#TXT/test2-v3.TXT".replace("/", File.separator));
    Assertions.assertTrue(newFile.exists());
    List<String> allNewFileLines = Files.readAllLines(Paths.get(newFile.getPath()));
    Assertions.assertEquals(1, allNewFileLines.size());
    Assertions.assertEquals("My test for several versions", allNewFileLines.get(0));
  }



  /* get subfiles related tests */


  @Test
  public void testGetAllSubfiles_edam734Repo() throws IOException {
    File directory = ResourceUtils.getFile(INPUT_LOCATION + "edam734");
    List<WrappedFile> files = RepositoryManager.getSubfiles(directory);

    Assertions.assertEquals(2, files.size());

    WrappedFile wrappedFile1 = new WrappedFile(
        new File("repo/edam734/test2#TXT/test2-v3.txt".replace("/", File.separator)),
        Paths.get("repo/edam734/test2.txt".replace("/", File.separator)));
    WrappedFile wrappedFile2 = new WrappedFile(
        new File("repo/edam734/test3#TXT/test3-v2.txt".replace("/", File.separator)),
        Paths.get("repo/edam734/test3.txt".replace("/", File.separator)));
    List<WrappedFile> expectedList = new ArrayList<>();
    expectedList.add(wrappedFile1);
    expectedList.add(wrappedFile2);

    Assertions.assertEquals(expectedList, files);
  }

  @Test
  public void testGetAllSubfiles_butInputIsAFile() throws IOException {
    File file = ResourceUtils.getFile(INPUT_LOCATION + "edam734/test2#TXT/test2-v1.TXT");
    List<WrappedFile> files = RepositoryManager.getSubfiles(file);

    Assertions.assertEquals(1, files.size());

    WrappedFile wrappedFile = new WrappedFile(
        new File("repo/edam734/test2#TXT/test2-v1.TXT".replace("/", File.separator)),
        Paths.get("repo/edam734/test2.TXT".replace("/", File.separator)));
    List<WrappedFile> expectedList = new ArrayList<>();
    expectedList.add(wrappedFile);

    Assertions.assertEquals(expectedList, files);
  }

  @Test
  public void testGetLatestFileVersion() throws IOException {
    File directory =
        ResourceUtils.getFile(INPUT_LOCATION + "edam734/test2#TXT".replace("/", File.separator));
    WrappedFile file = RepositoryManager.getFile(directory);

    // the latest version is 3
    Assertions.assertEquals(3,
        ServerRepoEnvironmentResolver.getVersionFromFilename(file.getContent().getName()));
  }

  @Test
  public void testGetSpecificFileVersion() throws IOException {
    File directory =
        ResourceUtils.getFile(INPUT_LOCATION + "edam734/test2#TXT".replace("/", File.separator));
    int wantedVersion = 2;
    WrappedFile file = RepositoryManager.getFile(directory, wantedVersion);

    Assertions.assertEquals(wantedVersion,
        ServerRepoEnvironmentResolver.getVersionFromFilename(file.getContent().getName()));
  }

  @Test
  public void testGetSpecificFileVersion_butVersionIsBiggerThanLatestVersion() throws IOException {
    File directory =
        ResourceUtils.getFile(INPUT_LOCATION + "edam734/test2#TXT".replace("/", File.separator));
    // latest version is 3, so we ask for version 4
    VersionGreaterThanLatestVersionException exception =
        Assertions.assertThrows(VersionGreaterThanLatestVersionException.class,
            () -> RepositoryManager.getFile(directory, 4));
    Assertions.assertEquals(String.format("Version %s bigger than the latest version %s", 4, 3),
        exception.getMessage());
  }


  @Test
  public void testGetUnversionedName_butNameWithSeveralMarkersOfVersion() {
    String filename1 = "dir1/dir2/test-v4f-vfs-v_-v#TXT/test-v4f-vfs-v_-v-v2.TXT";
    String filename2 = "dir1/dir2/test-v4f-v67-v_-v#TXT/test-v4f-v67-v_-v wdwe-v2.TXT";
    String filename3 = "dir1/dir2/test#TXT/test-v2.TXT";
    String filename4 = "dir1/dir2/test_without_version#TXT/test_without_version.TXT";
    Path actual1 = ServerRepoEnvironmentResolver.getUnversionedFilename(Paths.get(filename1));
    Path actual2 = ServerRepoEnvironmentResolver.getUnversionedFilename(Paths.get(filename2));
    Path actual3 = ServerRepoEnvironmentResolver.getUnversionedFilename(Paths.get(filename3));
    Path actual4 = ServerRepoEnvironmentResolver.getUnversionedFilename(Paths.get(filename4));

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
  public void testGetUnversionedName_butInputIsADirectory() {
    String filename = "dir1/dir2/test#TXT";
    Path actual = ServerRepoEnvironmentResolver.getUnversionedFilename(Paths.get(filename));

    String expected = "dir1/dir2/test.txt".replace("/", File.separator);
    Assertions.assertEquals(expected, actual.toString());
  }

  @Test
  public void testGetUnversionedName_butAlreadyUnversioned() {
    String filename = "dir1/dir2/test.TXT";
    Path actual = ServerRepoEnvironmentResolver.getUnversionedFilename(Paths.get(filename));

    String expected = "dir1/dir2/test.TXT".replace("/", File.separator);
    Assertions.assertEquals(expected, actual.toString());
  }

  @Test
  public void testGetAllSubfiles_johnn50Repo() throws IOException {
    File directory = ResourceUtils.getFile(INPUT_LOCATION + "johnny50");
    List<WrappedFile> files = RepositoryManager.getSubfiles(directory);

    Assertions.assertEquals(2, files.size());

    WrappedFile wrappedFile1 = new WrappedFile(
        new File("repo/johnny50/city/house/room1#TXT/room1-v2.txt".replace("/", File.separator)),
        Paths.get("repo/johnny50/city/house/room1.txt".replace("/", File.separator)));
    WrappedFile wrappedFile2 = new WrappedFile(
        new File("repo/johnny50/test2#TXT/test2-v3.txt".replace("/", File.separator)),
        Paths.get("repo/johnny50/test2.txt".replace("/", File.separator)));
    List<WrappedFile> expectedList = new ArrayList<>();
    expectedList.add(wrappedFile1);
    expectedList.add(wrappedFile2);

    Assertions.assertEquals(expectedList, files);
  }

  @Test
  public void testGetAllFilesDirectlyOfFolder() throws IOException {
    File directory =
        ResourceUtils.getFile(INPUT_LOCATION + "johnny50/test2#TXT".replace("/", File.separator));
    List<WrappedFile> files = RepositoryManager.getSubfiles(directory);

    WrappedFile wrappedFile = new WrappedFile(
        new File("repo/johnny50/test2#TXT/test2-v3.txt".replace("/", File.separator)),
        Paths.get("repo/johnny50/test2.txt".replace("/", File.separator)));

    Assertions.assertEquals(wrappedFile, files.get(0));
  }


  @Test
  public void testGetAllFilesDirectlyOfAVersionedFile() throws IOException {
    File directory = ResourceUtils
        .getFile(INPUT_LOCATION + "johnny50/test2#TXT/test2-v1.TXT".replace("/", File.separator));
    List<WrappedFile> files = RepositoryManager.getSubfiles(directory);

    WrappedFile wrappedFile = new WrappedFile(
        new File("repo/johnny50/test2#TXT/test2-v1.TXT".replace("/", File.separator)),
        Paths.get("repo/johnny50/test2.TXT".replace("/", File.separator)));

    Assertions.assertEquals(wrappedFile, files.get(0));
  }

  @Test
  public void testGetAllFilesDirectlyOfAUnversionedFile() throws IOException {
    File directory = ResourceUtils
        .getFile(INPUT_LOCATION + "steven123/test100.TXT".replace("/", File.separator));
    List<WrappedFile> files = RepositoryManager.getSubfiles(directory);

    // don't return unversioned files
    // (server's repo should't have unversioned files)
    Assertions.assertTrue(files.isEmpty());
  }
}
