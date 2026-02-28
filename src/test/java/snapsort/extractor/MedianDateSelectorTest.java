package snapsort.extractor;

import org.junit.jupiter.api.Test;
import snapsort.TimeStampSource;
import snapsort.TimeStampWithSource;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MedianDateSelectorTest {

  private final MedianDateSelector selector = new MedianDateSelector();

  private static TimeStampWithSource ts(int hour, int minute) {
    return new TimeStampWithSource(
        LocalDateTime.of(2025, 1, 1, hour, minute, 0),
        TimeStampSource.EXIF_DATE_TIME
    );
  }

  @Test
  void findClosestDates_emptyList_returnsNull() {
    assertNull(selector.selectDate(Collections.emptyList()));
  }

  @Test
  void findClosestDates_singleDate_returnsThatDate() {
    TimeStampWithSource single = ts(10, 0);
    assertSame(single, selector.selectDate(List.of(single)));
  }

  @Test
  void findClosestDates_twoDates_returnsLowerMedian() {
    TimeStampWithSource early = ts(10, 0);
    TimeStampWithSource late = ts(20, 0);
    TimeStampWithSource result = selector.selectDate(List.of(late, early));
    assertSame(early, result);
  }

  @Test
  void findClosestDates_outlierRemoved_originalBugCase() {
    // [10:00, 14:00, 14:01] — median is 14:00, should return 14:00 (not 10:00)
    TimeStampWithSource outlier = ts(10, 0);
    TimeStampWithSource close1 = ts(14, 0);
    TimeStampWithSource close2 = ts(14, 1);
    TimeStampWithSource result = selector.selectDate(List.of(outlier, close1, close2));
    assertSame(close1, result);
  }

  @Test
  void findClosestDates_evenCount_returnsLowerMedian() {
    // [10:00, 14:00, 19:01, 19:02] — lower median is 14:00
    TimeStampWithSource d1 = ts(10, 0);
    TimeStampWithSource d2 = ts(14, 0);
    TimeStampWithSource d3 = ts(19, 1);
    TimeStampWithSource d4 = ts(19, 2);
    TimeStampWithSource result = selector.selectDate(List.of(d3, d1, d4, d2));
    assertSame(d2, result);
  }

  @Test
  void findClosestDates_allIdentical_returnsThatDate() {
    TimeStampWithSource a = ts(12, 0);
    TimeStampWithSource b = ts(12, 0);
    TimeStampWithSource c = ts(12, 0);
    List<TimeStampWithSource> dates = List.of(a, b, c);
    TimeStampWithSource result = selector.selectDate(dates);
    // Should be the median element (index 1 after sort), which is b
    assertTrue(dates.contains(result));
    assertEquals(LocalDateTime.of(2025, 1, 1, 12, 0, 0), result.getTime());
  }
}
