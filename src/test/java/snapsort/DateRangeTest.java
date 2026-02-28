package snapsort;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DateRangeTest {

  @Test
  void minBoundaryIsInclusive() {
    var range = new DateRange(LocalDateTime.of(2024, 5, 30, 0, 0), null);
    assertTrue(range.isInRange(LocalDateTime.of(2024, 5, 30, 0, 0)));
  }

  @Test
  void maxBoundaryIncludesEntireDay() {
    // max already converted via plusDays(1).atStartOfDay in Main -> 2024-05-31T00:00
    var range = new DateRange(null, LocalDateTime.of(2024, 5, 31, 0, 0));
    assertTrue(range.isInRange(LocalDateTime.of(2024, 5, 30, 23, 59, 59)));
    assertFalse(range.isInRange(LocalDateTime.of(2024, 5, 31, 0, 0)));
  }

  @Test
  void nullBoundsMatchEverything() {
    var range = new DateRange(null, null);
    assertTrue(range.isInRange(LocalDateTime.of(2000, 1, 1, 0, 0)));
    assertTrue(range.isInRange(LocalDateTime.of(2099, 12, 31, 23, 59)));
  }

  @Test
  void timestampBeforeMinIsExcluded() {
    var range = new DateRange(LocalDateTime.of(2024, 1, 1, 0, 0), null);
    assertFalse(range.isInRange(LocalDateTime.of(2023, 12, 31, 23, 59, 59)));
  }
}
