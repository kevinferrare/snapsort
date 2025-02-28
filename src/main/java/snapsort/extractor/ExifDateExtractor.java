package snapsort.extractor;

import snapsort.TimeStampSource;
import snapsort.TimeStampWithSource;
import snapsort.files.FileTypeUtil;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import one.util.streamex.StreamEx;
import org.apache.commons.imaging.Imaging;
import org.apache.commons.imaging.common.ImageMetadata;
import org.apache.commons.imaging.common.RationalNumber;
import org.apache.commons.imaging.formats.jpeg.JpegImageMetadata;
import org.apache.commons.imaging.formats.tiff.TiffField;
import org.apache.commons.imaging.formats.tiff.TiffImageMetadata;
import org.apache.commons.imaging.formats.tiff.constants.ExifTagConstants;
import org.apache.commons.imaging.formats.tiff.constants.GpsTagConstants;
import org.apache.commons.imaging.formats.tiff.constants.TiffTagConstants;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Slf4j
@ApplicationScoped
public class ExifDateExtractor implements DateExtractor {
  private static final DateTimeFormatter EXIF_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy:MM:dd HH:mm:ss");
  private static final DateTimeFormatter GPS_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy:MM:dd");

  @Override
  public List<TimeStampWithSource> extractDates(Path file) {
    if (!FileTypeUtil.isJpegFile(file)) {
      log.debug("File {} is not a jpeg file", file);
      return Collections.emptyList();
    }
    log.debug("Extracting exif data from file {}", file);
    try {
      ImageMetadata metadata = extractExif(file);

      List<TimeStampWithSource> res = Optional.ofNullable(metadata)
          .filter(JpegImageMetadata.class::isInstance)
          .map(JpegImageMetadata.class::cast)
          .map(JpegImageMetadata::getExif)
          .map(ExifDateExtractor::extractDate)
          .orElseGet(Collections::emptyList);
      if (res.isEmpty()) {
        log.warn("No usable date found in exif data of file {}", file);
      }
      return res;
    } catch (IOException e) {
      log.error("Error reading exif data from file {}", file, e);
      return Collections.emptyList();
    }
  }

  @SneakyThrows
  private static List<TimeStampWithSource> extractDate(TiffImageMetadata tiffImageMetadata) {
    tiffImageMetadata.getAllFields().forEach(f -> log.debug("- Field: {}", f));
    TiffField gpsTimestampField = tiffImageMetadata.findField(GpsTagConstants.GPS_TAG_GPS_TIME_STAMP);
    TiffField gpsDateField = tiffImageMetadata.findField(GpsTagConstants.GPS_TAG_GPS_DATE_STAMP);
    TiffField dateTimeField = tiffImageMetadata.findField(TiffTagConstants.TIFF_TAG_DATE_TIME);
    TiffField dateTimeDigitizedField = tiffImageMetadata.findField(ExifTagConstants.EXIF_TAG_DATE_TIME_DIGITIZED);
    TiffField dateTimeOriginalField = tiffImageMetadata.findField(ExifTagConstants.EXIF_TAG_DATE_TIME_ORIGINAL);
    return StreamEx.of(
        extractGpsDate(gpsTimestampField, gpsDateField),
        extractDate(dateTimeField, TimeStampSource.EXIF_DATE_TIME),
        extractDate(dateTimeDigitizedField, TimeStampSource.EXIF_DATE_TIME_DIGITIZED),
        extractDate(dateTimeOriginalField, TimeStampSource.EXIF_DATE_TIME_ORIGINAL)
    ).nonNull().toList();
  }

  @SneakyThrows
  private static TimeStampWithSource extractGpsDate(TiffField gpsTimestampField, TiffField gpsDateField) {
    if (gpsTimestampField == null || gpsDateField == null) {
      return null;
    }
    RationalNumber[] gpsTime = (RationalNumber[])gpsTimestampField.getValue();
    if (gpsTime.length != 3) {
      return null;
    }
    LocalTime time = LocalTime.of(gpsTime[0].intValue(), gpsTime[1].intValue(), gpsTime[2].intValue());
    String gpsDate = (String)gpsDateField.getValue();
    LocalDate date = LocalDate.parse(gpsDate, GPS_DATE_FORMATTER);
    return new TimeStampWithSource(LocalDateTime.of(date, time), TimeStampSource.EXIF_GPS_DATE_TIME);
  }

  private static TimeStampWithSource extractDate(TiffField field, TimeStampSource source) {
    if (field == null) {
      return null;
    }
    LocalDateTime dateTime = parseDateTime(field.getValueDescription());
    return new TimeStampWithSource(dateTime, source);
  }

  private static LocalDateTime parseDateTime(String dateTime) {
    if (!dateTime.startsWith("'") || !dateTime.endsWith("'")) {
      return null;
    }
    String unquotedDateTime = dateTime.substring(1, dateTime.length() - 1);
    return LocalDateTime.parse(unquotedDateTime, EXIF_TIME_FORMATTER);
  }

  private static ImageMetadata extractExif(Path file) throws IOException {
    try {
      return Imaging.getMetadata(file.toFile());
    } catch (IllegalArgumentException e) {
      log.error("Error reading exif data from file {}", file, e);
      return null;
    }

  }
}
