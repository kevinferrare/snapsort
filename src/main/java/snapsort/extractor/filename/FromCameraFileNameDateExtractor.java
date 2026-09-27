package snapsort.extractor.filename;

import snapsort.TimeStampSource;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@ApplicationScoped
public class FromCameraFileNameDateExtractor extends BaseFileNameDateExtractor {
  private static final String[] KNOWN_PREFIXES = {"IMG_", "VID_"};

  public FromCameraFileNameDateExtractor() {
    super(
        TimeStampSource.CAMERA_FILE_NAME,
        DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"),
        DateTimeFormatter.ofPattern("yyyyMMdd HHmmss"),
        DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss-SSS"));
  }

  @Override
  protected LocalDateTime parse(String fileName) {
    return super.parse(removeKnownPrefix(fileName));
  }

  private static String removeKnownPrefix(String fileName) {
    for (String prefix : KNOWN_PREFIXES) {
      if (fileName.startsWith(prefix)) {
        return fileName.substring(prefix.length());
      }
    }
    return fileName;
  }
}
