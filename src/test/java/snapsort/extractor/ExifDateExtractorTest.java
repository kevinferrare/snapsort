package snapsort.extractor;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import snapsort.TimeStampWithSource;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ExifDateExtractorTest {

  private final ExifDateExtractor extractor = new ExifDateExtractor();

  @Test
  void nonJpegFileReturnsEmptyList(@TempDir Path tempDir) throws Exception {
    Path file = tempDir.resolve("video.mp4");
    Files.writeString(file, "dummy");

    List<TimeStampWithSource> result = extractor.extractDates(file);

    assertTrue(result.isEmpty());
  }

  @Test
  void pngFileReturnsEmptyList(@TempDir Path tempDir) throws Exception {
    Path file = tempDir.resolve("image.png");
    Files.writeString(file, "dummy");

    List<TimeStampWithSource> result = extractor.extractDates(file);

    assertTrue(result.isEmpty());
  }

  @Test
  void corruptJpegReturnsEmptyList(@TempDir Path tempDir) throws Exception {
    Path file = tempDir.resolve("corrupt.jpg");
    Files.writeString(file, "not a real jpeg");

    List<TimeStampWithSource> result = extractor.extractDates(file);

    assertTrue(result.isEmpty());
  }
}
