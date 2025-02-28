package snapsort.extractor;

import snapsort.TimeStampSource;
import snapsort.TimeStampWithSource;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Slf4j
@ApplicationScoped
public class FileDateExtractor implements DateExtractor {
  @SneakyThrows
  @Override
  public List<TimeStampWithSource> extractDates(Path file) {
    LocalDateTime dateTime =
        Files.getLastModifiedTime(file).toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    return List.of(new TimeStampWithSource(dateTime, TimeStampSource.FILE_LAST_MODIFIED));
  }
}
