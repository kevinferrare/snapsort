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

  private Path copyResourceFileToTempFolder(String resourceName, Path tempDir) throws IOException {
    String fileName = Path.of(resourceName).getFileName().toString();
    Path resourceContainerFolder = Files.createDirectory(tempDir.resolve("zip-resource"));
    Path filePath = resourceContainerFolder.resolve(fileName);
    try (InputStream resourceStream = this.getClass().getResourceAsStream(resourceName)) {
      Files.copy(resourceStream, filePath);
    }
    return filePath;
  }

  public String extractTestFiles(String zipName, Path tempDir) throws IOException {
    // copy the zip file to a temporary directory so that the zip lib can extract it
    Path zipFilePath = copyResourceFileToTempFolder(zipName, tempDir);

    // Create folder for extraction inside the managed temp dir
    Path folder = Files.createDirectory(tempDir.resolve("input"));

    // unzip the file to folder
    try (ZipFile zipFile = new ZipFile(zipFilePath.toFile())) {
      zipFile.extractAll(folder.toString());
    }
    return folder.toString();
  }
}
