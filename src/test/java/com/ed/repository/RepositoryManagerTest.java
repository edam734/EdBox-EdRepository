package com.ed.repository;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.springframework.util.ResourceUtils;
import com.ed.repository.filesystem.RepositoryManager;
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
        .getFile(OUTPUT_TEST_1 + "repo/test1 # TXT/test1.index.txt".replace("/", File.separator));
    Assertions.assertTrue(indexFile.exists());
    List<String> allIndexLines = Files.readAllLines(Paths.get(indexFile.getPath()));
    Assertions.assertNotNull(allIndexLines);
    Assertions.assertEquals(1, allIndexLines.size());
    String lineIndexFile = allIndexLines.get(0);
    Assertions.assertEquals("1 : edam734", lineIndexFile);

    // created the file 'test1-v1.TXT'
    File newFile = ResourceUtils
        .getFile(OUTPUT_TEST_1 + "repo/test1 # TXT/test1-v1.TXT".replace("/", File.separator));
    Assertions.assertTrue(newFile.exists());
    List<String> allNewFileLines = Files.readAllLines(Paths.get(newFile.getPath()));
    Assertions.assertNotNull(allNewFileLines);
    Assertions.assertEquals(1, allNewFileLines.size());
    String lineNewFile = allNewFileLines.get(0);
    Assertions.assertEquals("My test1", lineNewFile);
  }

  @Test
  public void testReceiveThreeFile_ThenWriteAndUpdateVersionWithSuccess() throws IOException {

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
        .getFile(OUTPUT_TEST_2 + "repo/test2 # TXT/test2.index.txt".replace("/", File.separator));
    Assertions.assertTrue(indexFile.exists());
    List<String> allIndexLines = Files.readAllLines(Paths.get(indexFile.getPath()));
    Assertions.assertNotNull(allIndexLines);
    Assertions.assertEquals(3, allIndexLines.size());
    Assertions.assertEquals("1 : maria", allIndexLines.get(0));
    Assertions.assertEquals("2 : johnny_cash", allIndexLines.get(1));
    Assertions.assertEquals("3 : O'Brien", allIndexLines.get(2));

    // created the file to the third version 'test2-v3.TXT'
    File newFile = ResourceUtils
        .getFile(OUTPUT_TEST_2 + "repo/test2 # TXT/test2-v3.TXT".replace("/", File.separator));
    Assertions.assertTrue(newFile.exists());
    List<String> allNewFileLines = Files.readAllLines(Paths.get(newFile.getPath()));
    Assertions.assertNotNull(allNewFileLines);
    Assertions.assertEquals(1, allNewFileLines.size());
    Assertions.assertEquals("My test for several versions", allNewFileLines.get(0));
  }
}
