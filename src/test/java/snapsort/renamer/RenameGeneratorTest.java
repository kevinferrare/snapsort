package snapsort.renamer;

import org.junit.jupiter.api.Test;
import snapsort.TimeStampSource;
import snapsort.TimeStampWithSource;
import snapsort.files.FileInfo;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RenameGeneratorTest {

  private final RenameGenerator generator = new RenameGenerator();

  private static FileInfo file(String name, int year, int month, int day, int hour, int minute, int second) {
    return new FileInfo(
        Path.of(name),
        new TimeStampWithSource(LocalDateTime.of(year, month, day, hour, minute, second), TimeStampSource.EXIF_DATE_TIME)
    );
  }

  @Test
  void singleFile_correctNameAndFolder() {
    FileInfo info = file("photo.jpg", 2025, 6, 15, 10, 30, 45);
    List<RenamedFile> result = generator.generateRenamedFileNames(List.of(info));

    assertEquals(1, result.size());
    assertEquals("2025-06-15 10.30.45.jpg", result.getFirst().newName());
    assertEquals("2025/20250615_", result.getFirst().newFolder());
  }

  @Test
  void jpegExtension_normalizedToJpg() {
    FileInfo info = file("photo.jpeg", 2025, 1, 1, 0, 0, 0);
    List<RenamedFile> result = generator.generateRenamedFileNames(List.of(info));

    assertEquals("2025-01-01 00.00.00.jpg", result.getFirst().newName());
  }

  @Test
  void pngExtension_keptAsPng() {
    FileInfo info = file("image.png", 2025, 3, 20, 14, 5, 0);
    List<RenamedFile> result = generator.generateRenamedFileNames(List.of(info));

    assertEquals("2025-03-20 14.05.00.png", result.getFirst().newName());
  }

  @Test
  void multipleFiles_outputSortedByFolderThenName() {
    FileInfo later = file("b.jpg", 2025, 12, 25, 18, 0, 0);
    FileInfo earlier = file("a.jpg", 2025, 1, 1, 6, 0, 0);
    List<RenamedFile> result = generator.generateRenamedFileNames(List.of(later, earlier));

    assertEquals(2, result.size());
    assertEquals("2025/20250101_", result.get(0).newFolder());
    assertEquals("2025/20251225_", result.get(1).newFolder());
  }

  @Test
  void emptyInput_emptyList() {
    assertTrue(generator.generateRenamedFileNames(Collections.emptyList()).isEmpty());
  }
}
