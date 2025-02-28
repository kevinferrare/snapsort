package snapsort;

import jakarta.inject.Inject;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class DateRange {
  private final LocalDateTime min;
  private final LocalDateTime max;

  @Inject
  public DateRange(LocalDateTime min, LocalDateTime max) {
    this.min = min;
    this.max = max;
  }

  public boolean isInRange(LocalDateTime timestamp) {
    return isAfterMin(timestamp) && isBeforeMax(timestamp);
  }

  public boolean isAfterMin(LocalDateTime timestamp) {
    return min == null || timestamp.isAfter(min);
  }

  public boolean isBeforeMax(LocalDateTime timestamp) {
    return max == null || timestamp.isBefore(max);
  }
}
