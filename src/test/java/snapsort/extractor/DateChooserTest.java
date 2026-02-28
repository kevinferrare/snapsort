package snapsort.extractor;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import snapsort.DateChooserConfiguration;
import snapsort.DateRange;
import snapsort.TimeStampSource;
import snapsort.TimeStampWithSource;
import snapsort.extractor.filename.FinalFileNameDateExtractor;
import snapsort.extractor.filename.FromCameraFileNameDateExtractor;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DateChooserTest {

  private static final Path DUMMY = Path.of("test.jpg");

  @Mock
  private FinalFileNameDateExtractor finalExtractor;
  @Mock
  private ExifDateExtractor exifExtractor;
  @Mock
  private FromCameraFileNameDateExtractor cameraExtractor;
  @Mock
  private FileDateExtractor fileExtractor;

  private static TimeStampWithSource ts(int year, int month, int day, int hour, int minute) {
    return new TimeStampWithSource(
        LocalDateTime.of(year, month, day, hour, minute, 0),
        TimeStampSource.EXIF_DATE_TIME
    );
  }

  private DateChooser chooser(boolean readFileDate) {
    return chooser(readFileDate, new DateRange(null, null));
  }

  private DateChooser chooser(boolean readFileDate, DateRange dateRange) {
    DateChooserConfiguration config = new DateChooserConfiguration();
    config.setReadFilesystemDateModified(readFileDate);
    return new DateChooser(finalExtractor, exifExtractor, cameraExtractor, fileExtractor,
        dateRange, config, new MedianDateSelector());
  }

  @Test
  void firstExtractorReturnsDates_usesThoseIgnoresLater() {
    TimeStampWithSource expected = ts(2025, 6, 15, 10, 0);
    when(finalExtractor.extractDates(DUMMY)).thenReturn(List.of(expected));

    TimeStampWithSource result = chooser(false).computeTimestamp(DUMMY);

    assertSame(expected, result);
    verify(exifExtractor, never()).extractDates(any());
  }

  @Test
  void firstExtractorEmpty_secondReturnsDates_usesSecond() {
    when(finalExtractor.extractDates(DUMMY)).thenReturn(Collections.emptyList());
    TimeStampWithSource expected = ts(2025, 6, 15, 12, 0);
    when(exifExtractor.extractDates(DUMMY)).thenReturn(List.of(expected));

    TimeStampWithSource result = chooser(false).computeTimestamp(DUMMY);

    assertSame(expected, result);
  }

  @Test
  void allExtractorsEmpty_returnsNull() {
    when(finalExtractor.extractDates(DUMMY)).thenReturn(Collections.emptyList());
    when(exifExtractor.extractDates(DUMMY)).thenReturn(Collections.emptyList());
    when(cameraExtractor.extractDates(DUMMY)).thenReturn(Collections.emptyList());

    assertNull(chooser(false).computeTimestamp(DUMMY));
  }

  @Test
  void datesOutsideDateRange_returnsNull() {
    TimeStampWithSource outOfRange = ts(2020, 1, 1, 10, 0);
    when(finalExtractor.extractDates(DUMMY)).thenReturn(List.of(outOfRange));

    DateRange range = new DateRange(
        LocalDateTime.of(2025, 1, 1, 0, 0),
        LocalDateTime.of(2026, 1, 1, 0, 0));

    assertNull(chooser(false, range).computeTimestamp(DUMMY));
  }

  @Test
  void mixOfInRangeAndOutOfRange_filtersAndReturnsMedianOfSurvivors() {
    TimeStampWithSource outOfRange = ts(2020, 1, 1, 10, 0);
    TimeStampWithSource inRange1 = ts(2025, 6, 15, 10, 0);
    TimeStampWithSource inRange2 = ts(2025, 6, 15, 14, 0);
    when(finalExtractor.extractDates(DUMMY)).thenReturn(List.of(outOfRange, inRange1, inRange2));

    DateRange range = new DateRange(
        LocalDateTime.of(2025, 1, 1, 0, 0),
        LocalDateTime.of(2026, 1, 1, 0, 0));

    TimeStampWithSource result = chooser(false, range).computeTimestamp(DUMMY);

    assertSame(inRange1, result);
  }

  @Test
  void readFilesystemDateModifiedFalse_fileExtractorNotCalled() {
    when(finalExtractor.extractDates(DUMMY)).thenReturn(Collections.emptyList());
    when(exifExtractor.extractDates(DUMMY)).thenReturn(Collections.emptyList());
    when(cameraExtractor.extractDates(DUMMY)).thenReturn(Collections.emptyList());

    chooser(false).computeTimestamp(DUMMY);

    verify(fileExtractor, never()).extractDates(any());
  }

  @Test
  void readFilesystemDateModifiedTrue_fileExtractorCalled() {
    when(finalExtractor.extractDates(DUMMY)).thenReturn(Collections.emptyList());
    when(exifExtractor.extractDates(DUMMY)).thenReturn(Collections.emptyList());
    when(cameraExtractor.extractDates(DUMMY)).thenReturn(Collections.emptyList());
    TimeStampWithSource fileDate = ts(2025, 3, 1, 8, 0);
    when(fileExtractor.extractDates(DUMMY)).thenReturn(List.of(fileDate));

    TimeStampWithSource result = chooser(true).computeTimestamp(DUMMY);

    assertSame(fileDate, result);
  }
}
