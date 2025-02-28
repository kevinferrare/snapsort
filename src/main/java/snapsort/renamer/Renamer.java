package snapsort.renamer;

import jakarta.enterprise.context.ApplicationScoped;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

@Slf4j
@ApplicationScoped
public class Renamer {

  public void renameFiles(Iterable<RenamedFile> renamedFiles, Path destination, boolean write) {
    Path normalizedDestination = destination.toAbsolutePath().normalize();
    if (!Files.exists(destination)) {
      log.error("Destination folder {} ({}) does not exist", destination, normalizedDestination);
      return;
    }
    if (write) {
      log.info("Renaming:");
    } else {
      log.info("Dry run, not renaming:");
    }
    for (RenamedFile renamedFile : renamedFiles) {
      renameFile(renamedFile, normalizedDestination, write);
    }
  }

  /**
   * Create a folder if it does not exist and its parents as well.
   *
   * @param folder
   * @return true if the folder exists or was created, false otherwise
   */
  private static boolean createFolder(File folder) {
    if (folder.exists()) {
      return true;
    }
    File parent = folder.getParentFile();
    // create parent if it does not exist
    if (!parent.exists() && !createFolder(parent)) {
      return false;
    }
    log.info("Creating folder {}", folder);
    return folder.mkdir();
  }

  private void renameFile(RenamedFile renamedFile, Path destination, boolean write) {
    Path folder = destination.resolve(renamedFile.newFolder());
    File folderFile = folder.toFile();
    if (write && !folderFile.exists() && !createFolder(folderFile)) {
      log.error("Could not create folder {}", folder);
      return;
    }
    Path newFile = folder.resolve(renamedFile.newName());
    if (newFile.toFile().exists()) {
      log.error("File {} already exists", newFile);
      return;
    }
    Path oldFile = renamedFile.currentFile();
    String operation = write ? "Renaming" : "Would rename";
    log.info("{} {} to {}", operation, oldFile, newFile);
    if (write) {
      boolean success = oldFile.toFile().renameTo(newFile.toFile());
      if (!success) {
        log.error("Could not move {} to {}", oldFile, newFile);
      }
    }
  }

}

