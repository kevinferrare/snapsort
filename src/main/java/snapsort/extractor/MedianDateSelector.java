package snapsort.extractor;

import jakarta.enterprise.context.ApplicationScoped;
import snapsort.TimeStampWithSource;

import java.util.List;

@ApplicationScoped
public class MedianDateSelector {

  public TimeStampWithSource selectDate(List<TimeStampWithSource> dates) {
    if (dates.isEmpty()) {
      return null;
    }
    if (dates.size() == 1) {
      return dates.getFirst();
    }
    List<TimeStampWithSource> sorted = dates.stream()
        .sorted((a, b) -> a.getTime().compareTo(b.getTime()))
        .toList();
    int medianIndex = (sorted.size() - 1) / 2;
    return sorted.get(medianIndex);
  }
}
