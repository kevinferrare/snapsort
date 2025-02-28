package snapsort;

import lombok.SneakyThrows;
import net.lingala.zip4j.ZipFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

public class TestResourcesUtils {

  @SneakyThrows
  public void listFiles(List<String> res, Path folder, String parentPath) {
    try (Stream<Path> files = Files.list(folder)) {
      files.forEach(file -> {
        String name = file.getFileName().toString();
        if (Files.isDirectory(file)) {
          listFiles(res, file, parentPath + name + "/");
        } else {
          res.add(parentPath + name);
        }
      });
    }
  }

  private Path copyResourceFileToTempFolder(String resourceName) throws IOException {
    String fileName = Path.of(resourceName).getFileName().toString();
    InputStream resourceStream = this.getClass().getResourceAsStream(resourceName);
    Path resourceContainerFolder = Files.createTempDirectory(fileName);

    Path filePath = resourceContainerFolder.resolve(fileName);
    Files.copy(resourceStream, filePath);
    return filePath;
  }

  public String extractTestFiles(String zipName, String testName) throws IOException {
    // copy the zip file to a temporary directory so that the zip lib can extract it
    Path zipFilePath = copyResourceFileToTempFolder(zipName);

    // Zip is now on the filesystem. Create folder for extraction
    Path folder = Files.createTempDirectory(testName);

    // unzip the file to folder
    try (ZipFile zipFile = new ZipFile(zipFilePath.toFile())) {
      zipFile.extractAll(folder.toString());
    }
    return folder.toString();
  }
}
