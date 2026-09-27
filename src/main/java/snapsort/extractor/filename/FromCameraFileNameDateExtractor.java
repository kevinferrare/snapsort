package snapsort.extractor.filename;

import snapsort.TimeStampSource;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.format.DateTimeFormatter;

@ApplicationScoped
public class FromCameraFileNameDateExtractor extends BaseFileNameDateExtractor {

  public FromCameraFileNameDateExtractor() {
    super(
        TimeStampSource.CAMERA_FILE_NAME,
        DateTimeFormatter.ofPattern("yyyyMMdd_HHmmssSSS"), // Pixel
        DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"), // stock/Samsung/Xiaomi camera, screenshots
        DateTimeFormatter.ofPattern("yyyy_MM_dd_HH_mm_ss_SSS"), // LineageOS / OEM screenshots
        DateTimeFormatter.ofPattern("yyyy_MM_dd_HH_mm_ss")); // fully separated, no millis
  }

  @Override
  protected String transformFileName(String fileName) {
    // drop any surrounding alphabetic prefix/suffix, e.g. IMG_/Screenshot_/..._ChromeBeta
    return super.transformFileName(fileName)
        .replaceFirst("^[A-Za-z]+_", "")
        .replaceFirst("_[A-Za-z]+$", "");
  }
}

