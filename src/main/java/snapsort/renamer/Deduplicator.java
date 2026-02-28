package snapsort.renamer;

import snapsort.TimeStampSource;
import snapsort.TimeStampWithSource;
import snapsort.files.FileInfo;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
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
    return res.files().stream().sorted(Comparator.comparing(FileInfo::path)).toList();
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
      collisions = true;
      // Conflict, add one second to the second file, 2 seconds to the third file, etc.
      int secondsDelta = 0;
      for (FileInfo file : files) {
        if (secondsDelta == 0) {
          res.add(file);
        } else {
          LocalDateTime shifted = file.timestamp().getTime().plusSeconds(secondsDelta);
          TimeStampWithSource newTimestamp = new TimeStampWithSource(shifted, TimeStampSource.COLLISION_AVOIDANCE);
          res.add(new FileInfo(file.path(), newTimestamp));
        }
        secondsDelta++;
      }
    }
    return new DeduplicateResult(collisions, res);
  }

  private static Map<LocalDateTime, List<FileInfo>> groupByDate(List<FileInfo> files) {
    return files.stream()
        .collect(Collectors.groupingBy(
            file -> file.timestamp().getTime(),
            TreeMap::new,
            Collectors.toList()
        ));
  }
}
