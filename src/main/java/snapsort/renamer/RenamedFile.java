package snapsort.renamer;

import java.nio.file.Path;

public record RenamedFile(
    // New name of the file in its new folder
    String newName,
    // New folder name, to be created if needed
    String newFolder,
    // Source file to rename to newName and to move to newFolder
    Path currentFile) implements Comparable<RenamedFile> {

  @Override
  public int compareTo(RenamedFile o) {
    int folderComparison = newFolder.compareTo(o.newFolder);
    if (folderComparison != 0) {
      return folderComparison;
    }
    return newName.compareTo(o.newName);
  }
}
