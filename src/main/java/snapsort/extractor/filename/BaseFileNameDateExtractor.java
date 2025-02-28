package snapsort.extractor.filename;

import snapsort.TimeStampSource;
import snapsort.TimeStampWithSource;
import snapsort.extractor.DateExtractor;
import snapsort.files.FileTypeUtil;
import lombok.extern.slf4j.Slf4j;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Collections;
import java.util.List;

@Slf4j
public class BaseFileNameDateExtractor implements DateExtractor {
  private final TimeStampSource source;
  private final List<DateTimeFormatter> formatters;

  protected BaseFileNameDateExtractor(TimeStampSource source, DateTimeFormatter... formatters) {
    this.source = source;
    this.formatters = List.of(formatters);
  }

  @Override
  public List<TimeStampWithSource> extractDates(Path file) {
    TimeStampWithSource res = extractSingleDate(file);
    if (res == null) {
      return Collections.emptyList();
    }
    return List.of(res);
  }

  private TimeStampWithSource extractSingleDate(Path file) {
    String name = FileTypeUtil.withoutExtension(file);
    LocalDateTime dateTime = parse(name);
    if (dateTime != null) {
      return new TimeStampWithSource(dateTime, source);
    }
    return null;
  }

  protected LocalDateTime parse(String input) {
    if (input == null) {
      return null;
    }
    for (DateTimeFormatter formatter : formatters) {
      try {
        return LocalDateTime.parse(input, formatter);
      } catch (DateTimeParseException e) {
        if (log.isDebugEnabled()) {
          log.debug("Failed to parse date " + input + " with formatter " + formatter, e);
        }
      }
    }
    return null;
  }
}
