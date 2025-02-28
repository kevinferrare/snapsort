package snapsort.extractor.filename;

import snapsort.TimeStampSource;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.extern.slf4j.Slf4j;

import java.time.format.DateTimeFormatter;

@Slf4j
@ApplicationScoped
public class FinalFileNameDateExtractor extends BaseFileNameDateExtractor {
  private static final String FILE_NAME_TARGET_FORMAT = "yyyy-MM-dd HH.mm.ss";

  public static final DateTimeFormatter FILE_NAME_TARGET_FORMATTER =
      DateTimeFormatter.ofPattern(FILE_NAME_TARGET_FORMAT);
  // Variants, dot can be a dash or a colon
  private static final DateTimeFormatter FILE_NAME_TARGET_ALTERNATIVE1_FORMATTER =
      DateTimeFormatter.ofPattern(FILE_NAME_TARGET_FORMAT.replace(".", "-"));
  private static final DateTimeFormatter FILE_NAME_TARGET_ALTERNATIVE2_FORMATTER =
      DateTimeFormatter.ofPattern(FILE_NAME_TARGET_FORMAT.replace(".", ":"));

  public FinalFileNameDateExtractor() {
    super(TimeStampSource.FINAL_FILE_NAME,
        FILE_NAME_TARGET_FORMATTER,
        FILE_NAME_TARGET_ALTERNATIVE1_FORMATTER,
        FILE_NAME_TARGET_ALTERNATIVE2_FORMATTER);
  }
}
