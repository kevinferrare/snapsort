package snapsort.extractor;

import snapsort.DateChooserConfiguration;
import snapsort.DateRange;
import snapsort.TimeStampWithSource;
import snapsort.extractor.filename.FinalFileNameDateExtractor;
import snapsort.extractor.filename.FromCameraFileNameDateExtractor;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.zone.ZoneRules;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@ApplicationScoped
public class DateChooser {
  private final List<DateExtractor> extractors;
  private final DateRange dateRange;

  @Inject
  public DateChooser(FinalFileNameDateExtractor finalFileNameDateExtractor,
      ExifDateExtractor exifDateExtractor,
      FromCameraFileNameDateExtractor fromCameraFileNameDateExtractor,
      FileDateExtractor fileDateExtractor,
      DateRange dateRange,
      DateChooserConfiguration dateChooserConfiguration) {
    // List is ordered, if one extractor finds something we take it and ignore what the other have to say
    this.extractors = new ArrayList<>(List.of(
        // If file is in final name format, it is the most reliable source,
        // means it has already been processed before or renamed by user and we trust it
        finalFileNameDateExtractor,
        // exif is the second most reliable source
        exifDateExtractor,
        // camera file name is the third most reliable source
        fromCameraFileNameDateExtractor
    ));
    if (dateChooserConfiguration.isReadFilesystemDateModified()) {
      // last resort, file date, murky but better than nothing
      this.extractors.add(fileDateExtractor);
    }
    this.dateRange = dateRange;
  }

  @SneakyThrows
  public TimeStampWithSource computeTimestamp(Path file) {
    log.info("Computing timestamp for file {}", file);
    List<TimeStampWithSource> dates = extractors.stream()
        .map(extractor -> extractor.extractDates(file))
        .filter(CollectionUtils::isNotEmpty)
        // take the first non-empty list
        .findAny()
        .orElseGet(Collections::emptyList)
        .stream()
        // filter the list for this extractor only
        // avoids files that would be excluded by this filter to be included because of other extractors
        .filter(date -> isInRange(file, date))
        .toList();
    TimeStampWithSource result = findClosestDates(dates);
    if (result == null) {
      log.error("Could not find a date for file {}", file);
    }
    return result;
  }

  private boolean isInRange(Path file, TimeStampWithSource date) {
    LocalDateTime timestamp = date.getTime();
    boolean inRange = dateRange.isInRange(timestamp);
    if (!inRange) {
      log.info("Date {} excluded for file {} because it is not in range {}", date, file, dateRange);
    }
    return inRange;
  }

  private static TimeStampWithSource findClosestDates(List<TimeStampWithSource> dates) {
    if (dates.isEmpty()) {
      return null;
    }
    if (dates.size() == 1) {
      return dates.getFirst();
    }
    Set<TimeStampWithSource> hashedDates = new HashSet<>(dates);
    // Compute mean timestamp and remove date that is the furthest from the mean
    do {
      Double epochSecondsMean = computeAverageEpoch(hashedDates);
      if (epochSecondsMean == null) {
        return null;
      }
      long seconds = (long)(double)epochSecondsMean;
      double fractionalSeconds = epochSecondsMean - seconds;
      int nanos = (int)(fractionalSeconds * 1_000_000_000);
      LocalDateTime mean = LocalDateTime.ofEpochSecond(seconds, nanos, ZoneOffset.UTC);
      TimeStampWithSource furthest = findFurthestToTarget(hashedDates, mean);
      log.debug("Removing furthest date {} from dates {} as it is the furthest from {}", furthest, hashedDates, mean);
      hashedDates.remove(furthest);
    } while (hashedDates.size() > 1);
    return hashedDates.stream().findAny().orElse(null);
  }

  private static Double computeAverageEpoch(Collection<TimeStampWithSource> dates) {
    return dates.stream()
        .map(TimeStampWithSource::getTime)
        .map(d -> d.toEpochSecond(ZoneOffset.UTC))
        .reduce(Long::sum)
        .map(l -> (double)l)
        .map(d -> d / dates.size()).orElse(null);
  }

  private static TimeStampWithSource findFurthestToTarget(Collection<TimeStampWithSource> dates, LocalDateTime target) {
    return dates.stream()
        .max((d1, d2) -> compare(d1, d2, target))
        .orElse(null);
  }

  private static int compare(TimeStampWithSource d1, TimeStampWithSource d2, LocalDateTime date) {
    return Long.compare(diff(d1.getTime(), date), diff(d2.getTime(), date));
  }

  private static long diff(LocalDateTime d1, LocalDateTime d2) {
    ZoneRules rules = ZoneId.systemDefault().getRules();
    long s1 = d1.toEpochSecond(rules.getOffset(d1));
    long s2 = d2.toEpochSecond(rules.getOffset(d2));
    return Math.abs(s1 - s2);
  }

}
