package snapsort.files;

import snapsort.TimeStampWithSource;
import lombok.Data;

import java.nio.file.Path;
import java.util.Comparator;

@Data
public class FileInfo implements Comparable<FileInfo> {
  private Path path;
  private TimeStampWithSource timestamp;

  @Override
  public int compareTo(FileInfo o) {
    return Comparator.comparing(FileInfo::getPath).compare(this, o);
  }
}
