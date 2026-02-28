package snapsort;

import java.time.LocalDateTime;

public record DateRange(LocalDateTime min, LocalDateTime max) {

  public boolean isInRange(LocalDateTime timestamp) {
    return isAfterMin(timestamp) && isBeforeMax(timestamp);
  }

  public boolean isAfterMin(LocalDateTime timestamp) {
    return min == null || !timestamp.isBefore(min);
  }

  public boolean isBeforeMax(LocalDateTime timestamp) {
    return max == null || timestamp.isBefore(max);
  }
}
