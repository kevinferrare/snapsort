package snapsort.extractor.filename;

import snapsort.TimeStampSource;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@ApplicationScoped
public class FromCameraFileNameDateExtractor extends BaseFileNameDateExtractor {
  public FromCameraFileNameDateExtractor() {
    super(TimeStampSource.CAMERA_FILE_NAME, DateTimeFormatter.ofPattern("yyyyMMdd HHmmss"));
  }

  @Override
  protected LocalDateTime parse(String fileName) {
    String formatted = removePrefix(fileName);
    return super.parse(formatted);
  }

  private static String removePrefix(String fileName) {
    // File format is IMG_20160804_100935.jpg or VID_20160804_100935.jpg or 20160804_100935.jpg
    // So remove the IMG / VID part before parsing
    String[] split = fileName.split("_");
    if (split.length < 2) {
      return null;
    }
    int dateIndex = 1;
    if (isNumber(split[0])) {
      dateIndex = 0;
    }
    String date = split[dateIndex];
    String time = split[dateIndex + 1];
    if (date.length() != 8 || time.length() != 6) {
      return null;
    }
    return date + " " + time;
  }

  private static boolean isNumber(String s) {
    return s.chars().allMatch(Character::isDigit);
  }
}
