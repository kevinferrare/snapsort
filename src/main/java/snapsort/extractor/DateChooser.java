package snapsort.extractor;

import snapsort.DateChooserConfiguration;
import snapsort.DateRange;
import snapsort.TimeStampWithSource;
import snapsort.extractor.filename.FinalFileNameDateExtractor;
import snapsort.extractor.filename.FromCameraFileNameDateExtractor;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Slf4j
@ApplicationScoped
public class DateChooser {
  private static final String CLIENT_PROXY_SUFFIX = "_ClientProxy";

  private final List<DateExtractor> extractors;
  private final DateRange dateRange;
  private final MedianDateSelector medianDateSelector;
  private final boolean readFilesystemDateModified;

  @Inject
  public DateChooser(FinalFileNameDateExtractor finalFileNameDateExtractor,
      ExifDateExtractor exifDateExtractor,
      FromCameraFileNameDateExtractor fromCameraFileNameDateExtractor,
      FileDateExtractor fileDateExtractor,
      DateRange dateRange,
      DateChooserConfiguration dateChooserConfiguration,
      MedianDateSelector medianDateSelector) {
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
    this.readFilesystemDateModified = dateChooserConfiguration.isReadFilesystemDateModified();
    this.dateRange = dateRange;
    this.medianDateSelector = medianDateSelector;
  }

  public TimeStampWithSource computeTimestamp(Path file) {
    log.info("Computing timestamp for file {}", file);
    String extractorUsed = null;
    List<TimeStampWithSource> extractedDates = Collections.emptyList();
    for (DateExtractor extractor : extractors) {
      List<TimeStampWithSource> candidateDates = extractor.extractDates(file);
      if (CollectionUtils.isNotEmpty(candidateDates)) {
        extractorUsed = extractorDisplayName(extractor);
        extractedDates = candidateDates;
        // Once an extractor returns dates, we ignore later extractors.
        break;
      }
    }
    List<TimeStampWithSource> dates = extractedDates.stream()
        // filter the list for this extractor only
        // avoids files that would be excluded by this filter to be included because of other extractors
        .filter(date -> isInRange(file, date))
        .toList();
    TimeStampWithSource result = medianDateSelector.selectDate(dates);
    if (result == null) {
      log.error("Could not find a date for file {}. rootCause={}", file,
          determineRootCause(extractorUsed, extractedDates, dates));
    }
    return result;
  }

  private String determineRootCause(String extractorUsed, List<TimeStampWithSource> extractedDates,
      List<TimeStampWithSource> datesInRange) {
    if (extractorUsed == null) {
      return "NO_EXTRACTOR_RETURNED_A_DATE"
          + " (checked=" + extractors.stream().map(this::extractorDisplayName).toList()
          + ", readFilesystemDateModified=" + readFilesystemDateModified
          + ", hint=enable --read-filesystem-date-modified to use file last-modified as fallback)";
    }
    if (CollectionUtils.isEmpty(datesInRange)) {
      return "ALL_DATES_OUT_OF_RANGE"
          + " (extractor=" + extractorUsed
          + ", extractedCount=" + extractedDates.size()
          + ", acceptedRange=" + dateRange
          + ", extracted=" + extractedDates + ")";
    }
    return "MEDIAN_SELECTION_RETURNED_NULL"
        + " (extractor=" + extractorUsed
        + ", inRangeCount=" + datesInRange.size()
        + ", inRangeDates=" + datesInRange + ")";
  }

  private String extractorDisplayName(DateExtractor extractor) {
    String simpleName = extractor.getClass().getSimpleName();
    if (simpleName.endsWith(CLIENT_PROXY_SUFFIX)) {
      return simpleName.substring(0, simpleName.length() - CLIENT_PROXY_SUFFIX.length());
    }
    return simpleName;
  }

  private boolean isInRange(Path file, TimeStampWithSource date) {
    LocalDateTime timestamp = date.getTime();
    boolean inRange = dateRange.isInRange(timestamp);
    if (!inRange) {
      log.info("Date {} excluded for file {} because it is not in range {}", date, file, dateRange);
    }
    return inRange;
  }

}
