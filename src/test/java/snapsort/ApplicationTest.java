package snapsort;

import io.quarkus.test.junit.main.QuarkusMainLauncher;
import io.quarkus.test.junit.main.QuarkusMainTest;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

@QuarkusMainTest
public class ApplicationTest {
  private final TestResourcesUtils resourcesUtils = new TestResourcesUtils();
  private ApplicationLauncher launcher;
  private Path outputFolder;

  @BeforeEach
  public void setup(TestInfo testInfo, QuarkusMainLauncher quarkusMainLauncher) throws IOException {
    String testName = testInfo.getDisplayName();
    String inputFolderName = resourcesUtils.extractTestFiles("/testfiles.zip", testName);
    outputFolder = Files.createTempDirectory("output" + testName);
    launcher = new ApplicationLauncher(quarkusMainLauncher);
    launcher.withInputFolders(inputFolderName);
    launcher.withOutputFolder(outputFolder.toString());
    System.out.println("input folder is " + inputFolderName);
    System.out.println("output folder is " + inputFolderName);
  }

  @SneakyThrows
  private List<String> listOutput() {
    List<String> res = new ArrayList<>();
    resourcesUtils.listFiles(res, outputFolder, "/");
    // Sort the list to have a deterministic order
    return res.stream().sorted().toList();
  }

  @Test
  public void testDryRun() {
    launcher.run();
    Assertions.assertTrue(listOutput().isEmpty());
  }

  @Test
  public void testRealRun() {
    // Expects all files from the zip to be there except the one where date is read from filesytem
    testOutputFolderIsZipWithout("/2025/20250227_/2025-02-27 00.18.58.jpg");
  }

  @Test
  public void testRealRunWithFilesystemDate() {
    launcher.withReadFilesystemDateModified(true);
    testOutputFolderIsZipWithout();
  }

  @Test
  public void testRunWithDateMin() {
    launcher.withDateMin("2024-01-01");
    // 2016 is filtered out
    testOutputFolderIsZipWithout(
        "/2016/20160530_/2016-05-30 21.03.59.jpg",
        "/2016/20160919_/2016-09-19 21.11.12.jpg",
        "/2025/20250227_/2025-02-27 00.18.58.jpg"
    );
  }

  @Test
  public void testRunWithDateMax() {
    launcher.withDateMax("2024-06-01");
    // 2025 is filtered out
    testOutputFolderIsZipWithout("/2025/20250227_/2025-02-27 00.18.58.jpg");
  }

  @Test
  public void testRunWithDateMinAndDateMax() {
    launcher.withDateMin("2024-01-01");
    launcher.withDateMax("2024-06-01");
    // 2016 and 2025 are filtered out
    testOutputFolderIsZipWithout(
        "/2016/20160530_/2016-05-30 21.03.59.jpg",
        "/2016/20160919_/2016-09-19 21.11.12.jpg",
        "/2025/20250227_/2025-02-27 00.18.58.jpg"
    );
  }

  private void testOutputFolderIsZipWithout(String... unexpected) {
    launcher.withWrite(true);
    launcher.run();
    Set<String> unexpectedFiles = Set.of(unexpected);
    List<String> expected = Stream.of(
        // Camera format jpeg
        "/2016/20160530_/2016-05-30 21.03.59.jpg",
        // Actual photo with exif creation date
        "/2016/20160919_/2016-09-19 21.11.12.jpg",
        // the 3 files have the same date (in their names, but different extractors).
        // They should not have the same final date.
        "/2024/20240530_/2024-05-30 21.03.59.jpg",
        "/2024/20240530_/2024-05-30 21.04.00.jpg",
        "/2024/20240530_/2024-05-30 21.04.01.jpg",
        // Camera format mp4
        "/2024/20240530_/2024-05-30 21.04.12.mp4",
        // Date taken from file last modified date
        "/2025/20250227_/2025-02-27 00.18.58.jpg"
    ).filter(s -> !unexpectedFiles.contains(s)).toList();
    List<String> actual = listOutput();
    Assertions.assertEquals(expected, actual);
  }
}
