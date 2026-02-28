package snapsort.extractor.filename;

import org.junit.jupiter.api.Test;
import snapsort.TimeStampSource;
import snapsort.TimeStampWithSource;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FromCameraFileNameDateExtractorTest {

  private final FromCameraFileNameDateExtractor extractor = new FromCameraFileNameDateExtractor();

  @Test
  void imgPrefixExtractsCorrectDate() {
    List<TimeStampWithSource> result = extractor.extractDates(Path.of("IMG_20160804_100935.jpg"));

    assertEquals(1, result.size());
    assertEquals(LocalDateTime.of(2016, 8, 4, 10, 9, 35), result.getFirst().getTime());
    assertEquals(TimeStampSource.CAMERA_FILE_NAME, result.getFirst().getSource());
  }

  @Test
  void vidPrefixExtractsCorrectDate() {
    List<TimeStampWithSource> result = extractor.extractDates(Path.of("VID_20230115_183022.mp4"));

    assertEquals(1, result.size());
    assertEquals(LocalDateTime.of(2023, 1, 15, 18, 30, 22), result.getFirst().getTime());
    assertEquals(TimeStampSource.CAMERA_FILE_NAME, result.getFirst().getSource());
  }

  @Test
  void bareDateWithoutPrefixExtractsCorrectDate() {
    List<TimeStampWithSource> result = extractor.extractDates(Path.of("20160804_100935.jpg"));

    assertEquals(1, result.size());
    assertEquals(LocalDateTime.of(2016, 8, 4, 10, 9, 35), result.getFirst().getTime());
    assertEquals(TimeStampSource.CAMERA_FILE_NAME, result.getFirst().getSource());
  }

  @Test
  void singleSegmentFilenameReturnsEmptyList() {
    List<TimeStampWithSource> result = extractor.extractDates(Path.of("photo.jpg"));

    assertTrue(result.isEmpty());
  }

  @Test
  void twoSegmentNonDateFilenameReturnsEmptyList() {
    List<TimeStampWithSource> result = extractor.extractDates(Path.of("holiday_photo.jpg"));

    assertTrue(result.isEmpty());
  }

  @Test
  void wrongDateLengthReturnsEmptyList() {
    List<TimeStampWithSource> result = extractor.extractDates(Path.of("IMG_2016_100935.jpg"));

    assertTrue(result.isEmpty());
  }
}
