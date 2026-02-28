package snapsort.renamer;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RenamerTest {

  private final Renamer renamer = new Renamer();

  private static RenamedFile renamed(Path source, String folder, String name) {
    return new RenamedFile(name, folder, source);
  }

  @Test
  void dryRun_noFilesMovedNoDirectoriesCreated(@TempDir Path src, @TempDir Path dest) throws IOException {
    Path file = Files.createFile(src.resolve("photo.jpg"));
    RenamedFile rf = renamed(file, "2025/20250615_", "2025-06-15 10.30.45.jpg");

    renamer.renameFiles(List.of(rf), dest, false);

    assertTrue(Files.exists(file), "original file should still exist");
    assertFalse(Files.exists(dest.resolve("2025/20250615_")), "target folder should not be created");
  }

  @Test
  void writeMode_fileMovedToCorrectSubdirectory(@TempDir Path src, @TempDir Path dest) throws IOException {
    Path file = Files.createFile(src.resolve("photo.jpg"));
    RenamedFile rf = renamed(file, "2025/20250615_", "2025-06-15 10.30.45.jpg");

    renamer.renameFiles(List.of(rf), dest, true);

    assertFalse(Files.exists(file), "original should be gone");
    assertTrue(Files.exists(dest.resolve("2025/20250615_/2025-06-15 10.30.45.jpg")));
  }

  @Test
  void destinationDoesNotExist_returnsWithoutMoving(@TempDir Path src) throws IOException {
    Path file = Files.createFile(src.resolve("photo.jpg"));
    Path nonExistent = src.resolve("no-such-dir");
    RenamedFile rf = renamed(file, "2025/", "out.jpg");

    renamer.renameFiles(List.of(rf), nonExistent, true);

    assertTrue(Files.exists(file), "original file should remain");
  }

  @Test
  void targetFileAlreadyExists_skipsWithoutOverwrite(@TempDir Path src, @TempDir Path dest) throws IOException {
    Path file = Files.createFile(src.resolve("photo.jpg"));
    Files.writeString(file, "original");
    Path targetDir = Files.createDirectories(dest.resolve("2025/20250615_"));
    Path existing = Files.createFile(targetDir.resolve("2025-06-15 10.30.45.jpg"));
    Files.writeString(existing, "existing");

    RenamedFile rf = renamed(file, "2025/20250615_", "2025-06-15 10.30.45.jpg");
    renamer.renameFiles(List.of(rf), dest, true);

    assertTrue(Files.exists(file), "original should still exist");
    assertEquals("existing", Files.readString(existing), "existing file should not be overwritten");
  }

  @Test
  void createsNestedDirectoriesWhenNeeded(@TempDir Path src, @TempDir Path dest) throws IOException {
    Path file = Files.createFile(src.resolve("video.mp4"));
    RenamedFile rf = renamed(file, "2025/20250101_", "2025-01-01 00.00.00.mp4");

    renamer.renameFiles(List.of(rf), dest, true);

    assertTrue(Files.isDirectory(dest.resolve("2025/20250101_")));
    assertTrue(Files.exists(dest.resolve("2025/20250101_/2025-01-01 00.00.00.mp4")));
  }
}
