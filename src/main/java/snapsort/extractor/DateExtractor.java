package snapsort.extractor;

import snapsort.TimeStampWithSource;

import java.nio.file.Path;
import java.util.List;

public interface DateExtractor {
  List<TimeStampWithSource> extractDates(Path file);
}
