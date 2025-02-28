package snapsort.renamer;

import snapsort.TimeStampSource;
import snapsort.TimeStampWithSource;
import snapsort.files.FileInfo;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@ApplicationScoped
public class Deduplicator {
  private record DeduplicateResult(boolean collisions, List<FileInfo> files) {
  }

  public List<FileInfo> deduplicateDates(List<FileInfo> files) {
    DeduplicateResult res = deduplicateDatesStep(files);
    while (res.collisions()) {
      res = deduplicateDatesStep(res.files());
    }
    return res.files().stream().sorted().toList();
  }

  private static DeduplicateResult deduplicateDatesStep(List<FileInfo> allFiles) {
    Map<LocalDateTime, List<FileInfo>> filesByDate = groupByDate(allFiles);
    List<FileInfo> res = new ArrayList<>();
    boolean collisions = false;
    for (Map.Entry<LocalDateTime, List<FileInfo>> entry : filesByDate.entrySet()) {
      List<FileInfo> files = entry.getValue();
      FileInfo first = files.getFirst();
      if (files.size() == 1) {
        // No conflict
        res.add(first);
        continue;
      }
      // Conflict, add one second to the second file, 2 seconds to the third file, etc.
      int secondsDelta = 0;
      for (FileInfo file : files) {
        LocalDateTime timestamp = file.getTimestamp().getTime();
        TimeStampWithSource newTimestamp =
            new TimeStampWithSource(timestamp.plusSeconds(secondsDelta), TimeStampSource.COLLISION_AVOIDANCE);
        file.setTimestamp(newTimestamp);
        res.add(file);
        secondsDelta++;
      }
    }
    return new DeduplicateResult(collisions, res);
  }

  private static Map<LocalDateTime, List<FileInfo>> groupByDate(List<FileInfo> files) {
    return files.stream()
        .collect(Collectors.groupingBy(file -> file.getTimestamp().getTime()));
  }
}
