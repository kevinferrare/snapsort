package snapsort.files;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import snapsort.TimeStampSource;
import snapsort.TimeStampWithSource;
import snapsort.extractor.DateChooser;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FileListerTest {

  @Mock
  private DateChooser dateChooser;

  @InjectMocks
  private FileLister fileLister;

  private static TimeStampWithSource ts() {
    return new TimeStampWithSource(
        LocalDateTime.of(2025, 6, 15, 10, 0, 0),
        TimeStampSource.EXIF_DATE_TIME
    );
  }

  @Test
  void supportedFileWithTimestamp_included(@TempDir Path dir) throws IOException {
    Path jpg = Files.createFile(dir.resolve("photo.jpg"));
    Files.writeString(jpg, "data");
    when(dateChooser.computeTimestamp(jpg)).thenReturn(ts());

    List<FileInfo> result = fileLister.listFiles(dir);

    assertEquals(1, result.size());
    assertEquals(jpg, result.getFirst().path());
  }

  @Test
  void unsupportedExtension_excluded(@TempDir Path dir) throws IOException {
    Path txt = Files.createFile(dir.resolve("notes.txt"));
    Files.writeString(txt, "data");

    List<FileInfo> result = fileLister.listFiles(dir);

    assertTrue(result.isEmpty());
  }

  @Test
  void emptyFile_excluded(@TempDir Path dir) throws IOException {
    Files.createFile(dir.resolve("empty.jpg"));

    List<FileInfo> result = fileLister.listFiles(dir);

    assertTrue(result.isEmpty());
  }

  @Test
  void pendingPrefixFile_excluded(@TempDir Path dir) throws IOException {
    Path pending = Files.createFile(dir.resolve(".pending-photo.jpg"));
    Files.writeString(pending, "data");

    List<FileInfo> result = fileLister.listFiles(dir);

    assertTrue(result.isEmpty());
  }

  @Test
  void trashedPrefixFile_excluded(@TempDir Path dir) throws IOException {
    Path trashed = Files.createFile(dir.resolve(".trashed-photo.jpg"));
    Files.writeString(trashed, "data");

    List<FileInfo> result = fileLister.listFiles(dir);

    assertTrue(result.isEmpty());
  }

  @Test
  void dateChooserReturnsNull_fileExcluded(@TempDir Path dir) throws IOException {
    Path jpg = Files.createFile(dir.resolve("photo.jpg"));
    Files.writeString(jpg, "data");
    when(dateChooser.computeTimestamp(jpg)).thenReturn(null);

    List<FileInfo> result = fileLister.listFiles(dir);

    assertTrue(result.isEmpty());
  }

  @Test
  void nonExistentFolder_emptyList() {
    List<FileInfo> result = fileLister.listFiles(Path.of("/non/existent/path"));

    assertTrue(result.isEmpty());
  }

  @Test
  void multipleFolders_resultsFlattenedAndSorted(@TempDir Path dir1, @TempDir Path dir2) throws IOException {
    Path a = Files.createFile(dir1.resolve("b.jpg"));
    Files.writeString(a, "data");
    Path b = Files.createFile(dir2.resolve("a.jpg"));
    Files.writeString(b, "data");
    when(dateChooser.computeTimestamp(any())).thenReturn(ts());

    List<FileInfo> result = fileLister.listFiles(List.of(dir1, dir2));

    assertEquals(2, result.size());
    // sorted by path — compare file names
    assertTrue(result.get(0).path().compareTo(result.get(1).path()) <= 0);
  }
}
