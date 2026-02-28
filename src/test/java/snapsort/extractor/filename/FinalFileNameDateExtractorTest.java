package snapsort.extractor.filename;

import org.junit.jupiter.api.Test;
import snapsort.TimeStampSource;
import snapsort.TimeStampWithSource;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FinalFileNameDateExtractorTest {

  private final FinalFileNameDateExtractor extractor = new FinalFileNameDateExtractor();

  @Test
  void dotFormatExtractsCorrectDate() {
    List<TimeStampWithSource> result = extractor.extractDates(Path.of("2024-01-15 10.30.45.jpg"));

    assertEquals(1, result.size());
    assertEquals(LocalDateTime.of(2024, 1, 15, 10, 30, 45), result.getFirst().getTime());
    assertEquals(TimeStampSource.FINAL_FILE_NAME, result.getFirst().getSource());
  }

  @Test
  void dashVariantExtractsCorrectDate() {
    List<TimeStampWithSource> result = extractor.extractDates(Path.of("2024-01-15 10-30-45.jpg"));

    assertEquals(1, result.size());
    assertEquals(LocalDateTime.of(2024, 1, 15, 10, 30, 45), result.getFirst().getTime());
    assertEquals(TimeStampSource.FINAL_FILE_NAME, result.getFirst().getSource());
  }

  @Test
  void colonVariantExtractsCorrectDate() {
    List<TimeStampWithSource> result = extractor.extractDates(Path.of("2024-01-15 10:30:45.jpg"));

    assertEquals(1, result.size());
    assertEquals(LocalDateTime.of(2024, 1, 15, 10, 30, 45), result.getFirst().getTime());
    assertEquals(TimeStampSource.FINAL_FILE_NAME, result.getFirst().getSource());
  }

  @Test
  void invalidFilenameReturnsEmptyList() {
    List<TimeStampWithSource> result = extractor.extractDates(Path.of("random_photo.jpg"));

    assertTrue(result.isEmpty());
  }
}
