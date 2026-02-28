package snapsort.extractor;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import snapsort.TimeStampSource;
import snapsort.TimeStampWithSource;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class FileDateExtractorTest {

  private final FileDateExtractor extractor = new FileDateExtractor();

  @Test
  void extractsLastModifiedDateFromFile(@TempDir Path tempDir) throws Exception {
    Path file = tempDir.resolve("test.jpg");
    Files.writeString(file, "dummy");

    List<TimeStampWithSource> result = extractor.extractDates(file);

    assertFalse(result.isEmpty());
    assertEquals(1, result.size());
    assertEquals(TimeStampSource.FILE_LAST_MODIFIED, result.getFirst().getSource());
    assertNotNull(result.getFirst().getTime());
  }
}
