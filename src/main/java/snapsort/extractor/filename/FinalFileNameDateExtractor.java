package snapsort.extractor.filename;

import snapsort.TimeStampSource;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.extern.slf4j.Slf4j;

import java.time.format.DateTimeFormatter;

@Slf4j
@ApplicationScoped
public class FinalFileNameDateExtractor extends BaseFileNameDateExtractor {
  // Used only for formatting output file names; parsing normalizes separators, see BaseFileNameDateExtractor#parse
  public static final DateTimeFormatter FILE_NAME_TARGET_FORMATTER =
      DateTimeFormatter.ofPattern("yyyy-MM-dd HH.mm.ss");
  private static final DateTimeFormatter FILE_NAME_PARSE_FORMATTER =
      DateTimeFormatter.ofPattern("yyyy_MM_dd_HH_mm_ss");

  public FinalFileNameDateExtractor() {
    super(TimeStampSource.FINAL_FILE_NAME, FILE_NAME_PARSE_FORMATTER);
  }
}
