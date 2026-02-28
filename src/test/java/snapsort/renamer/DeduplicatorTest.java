package snapsort.renamer;

import org.junit.jupiter.api.Test;
import snapsort.TimeStampSource;
import snapsort.TimeStampWithSource;
import snapsort.files.FileInfo;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DeduplicatorTest {

  private final Deduplicator deduplicator = new Deduplicator();

  @Test
  void firstFileInCollisionGroupKeepsOriginalSource() {
    FileInfo fileA = fileInfo("a.jpg", "2025-07-14T11:26:42", TimeStampSource.EXIF_DATE_TIME_ORIGINAL);
    FileInfo fileB = fileInfo("b.jpg", "2025-07-14T11:26:42", TimeStampSource.EXIF_DATE_TIME);

    List<FileInfo> result = deduplicator.deduplicateDates(List.of(fileA, fileB));

    FileInfo first = result.stream()
        .filter(f -> f.path().equals(Path.of("a.jpg")))
        .findFirst().orElseThrow();
    assertEquals(TimeStampSource.EXIF_DATE_TIME_ORIGINAL, first.timestamp().getSource(),
        "Unshifted file must keep its original source");
    FileInfo second = result.stream()
        .filter(f -> f.path().equals(Path.of("b.jpg")))
        .findFirst().orElseThrow();
    assertEquals(TimeStampSource.COLLISION_AVOIDANCE, second.timestamp().getSource(),
        "Shifted file must have COLLISION_AVOIDANCE source");
  }

  @Test
  void deduplicationShouldNotCreateNewCollisions() {
    FileInfo fileA = fileInfo("20250714_112641.jpg", "2025-07-14T11:26:42", TimeStampSource.EXIF_DATE_TIME);
    FileInfo fileB = fileInfo("20250714_112642(0).jpg", "2025-07-14T11:26:43", TimeStampSource.EXIF_DATE_TIME_ORIGINAL);
    FileInfo fileC = fileInfo("20250714_112642.jpg", "2025-07-14T11:26:42", TimeStampSource.EXIF_DATE_TIME);

    List<FileInfo> result = deduplicator.deduplicateDates(List.of(fileA, fileB, fileC));

    Set<LocalDateTime> timestamps = result.stream()
        .map(f -> f.timestamp().getTime())
        .collect(Collectors.toSet());
    assertEquals(3, timestamps.size(), "All timestamps must be unique, got: " + result);
  }

  private static FileInfo fileInfo(String name, String timestamp, TimeStampSource source) {
    return new FileInfo(Path.of(name), new TimeStampWithSource(LocalDateTime.parse(timestamp), source));
  }
}
