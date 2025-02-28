package snapsort;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class TimeStampWithSource {
  private LocalDateTime time;
  private TimeStampSource source;
}
