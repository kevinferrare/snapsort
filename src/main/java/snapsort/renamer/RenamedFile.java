package snapsort.renamer;

import java.nio.file.Path;
import java.util.Comparator;

public record RenamedFile(
    // New name of the file in its new folder
    String newName,
    // New folder name, to be created if needed
    String newFolder,
    // Source file to rename to newName and to move to newFolder
    Path currentFile) {

  public static final Comparator<RenamedFile> BY_DESTINATION =
      Comparator.comparing(RenamedFile::newFolder).thenComparing(RenamedFile::newName);
}
