package snapsort.extractor.filename;

import org.junit.jupiter.api.Test;
import snapsort.TimeStampSource;
import snapsort.TimeStampWithSource;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

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
  void spaceSeparatedDateExtractsCorrectDate() {
    List<TimeStampWithSource> result = extractor.extractDates(Path.of("20160804 100935.jpg"));

    assertEquals(1, result.size());
    assertEquals(LocalDateTime.of(2016, 8, 4, 10, 9, 35), result.getFirst().getTime());
    assertEquals(TimeStampSource.CAMERA_FILE_NAME, result.getFirst().getSource());
  }

  @Test
  void lineageOsFormatExtractsCorrectDate() {
    List<TimeStampWithSource> result =
        extractor.extractDates(Path.of("2026-04-20-13-35-05-684.jpg"));

    assertEquals(1, result.size());
    assertEquals(LocalDateTime.of(2026, 4, 20, 13, 35, 5, 684_000_000), result.getFirst().getTime());
    assertEquals(TimeStampSource.CAMERA_FILE_NAME, result.getFirst().getSource());
  }

  @Test
  void lineageOsFormatWithImgPrefixExtractsCorrectDate() {
    List<TimeStampWithSource> result =
        extractor.extractDates(Path.of("IMG_2026-04-20-13-35-05-684.jpg"));

    assertEquals(1, result.size());
    assertEquals(LocalDateTime.of(2026, 4, 20, 13, 35, 5, 684_000_000), result.getFirst().getTime());
    assertEquals(TimeStampSource.CAMERA_FILE_NAME, result.getFirst().getSource());
  }

  @Test
  void pixelFormatExtractsCorrectDate() {
    List<TimeStampWithSource> result =
        extractor.extractDates(Path.of("PXL_20210328_205805123.jpg"));

    assertEquals(1, result.size());
    assertEquals(LocalDateTime.of(2021, 3, 28, 20, 58, 5, 123_000_000), result.getFirst().getTime());
    assertEquals(TimeStampSource.CAMERA_FILE_NAME, result.getFirst().getSource());
  }

  @Test
  void screenshotFormatExtractsCorrectDate() {
    List<TimeStampWithSource> result =
        extractor.extractDates(Path.of("Screenshot_20230101-123456.png"));

    assertEquals(1, result.size());
    assertEquals(LocalDateTime.of(2023, 1, 1, 12, 34, 56), result.getFirst().getTime());
    assertEquals(TimeStampSource.CAMERA_FILE_NAME, result.getFirst().getSource());
  }

  @Test
  void samsungScreenshotWithAppNameSuffixExtractsCorrectDate() {
    List<TimeStampWithSource> result =
        extractor.extractDates(Path.of("Screenshot_20230101-123456_ChromeBeta.jpg"));

    assertEquals(1, result.size());
    assertEquals(LocalDateTime.of(2023, 1, 1, 12, 34, 56), result.getFirst().getTime());
    assertEquals(TimeStampSource.CAMERA_FILE_NAME, result.getFirst().getSource());
  }

  @Test
  void dashScreenshotWithMillisExtractsCorrectDate() {
    List<TimeStampWithSource> result =
        extractor.extractDates(Path.of("Screenshot_2023-01-01-12-34-56-123.png"));

    assertEquals(1, result.size());
    assertEquals(LocalDateTime.of(2023, 1, 1, 12, 34, 56, 123_000_000), result.getFirst().getTime());
    assertEquals(TimeStampSource.CAMERA_FILE_NAME, result.getFirst().getSource());
  }

  @Test
  void dotSeparatedTimeExtractsCorrectDate() {
    List<TimeStampWithSource> result = extractor.extractDates(Path.of("2026-03-21 01.58.15.jpg"));

    assertEquals(1, result.size());
    assertEquals(LocalDateTime.of(2026, 3, 21, 1, 58, 15), result.getFirst().getTime());
    assertEquals(TimeStampSource.CAMERA_FILE_NAME, result.getFirst().getSource());
  }

  @Test
  void colonSeparatedTimeExtractsCorrectDate() {
    assumeFalse(System.getProperty("os.name", "").toLowerCase().contains("win"),
        "Windows does not allow ':' in file names");
    List<TimeStampWithSource> result = extractor.extractDates(Path.of("2026-03-21 01:58:15.jpg"));

    assertEquals(1, result.size());
    assertEquals(LocalDateTime.of(2026, 3, 21, 1, 58, 15), result.getFirst().getTime());
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
