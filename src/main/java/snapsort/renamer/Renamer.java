package snapsort.renamer;

import jakarta.enterprise.context.ApplicationScoped;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
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

  private void renameFile(RenamedFile renamedFile, Path destination, boolean write) {
    Path folder = destination.resolve(renamedFile.newFolder());
    if (write && !Files.exists(folder)) {
      try {
        log.info("Creating folder {}", folder);
        Files.createDirectories(folder);
      } catch (IOException e) {
        log.error("Could not create folder {}", folder, e);
        return;
      }
    }
    Path newFile = folder.resolve(renamedFile.newName());
    if (Files.exists(newFile)) {
      log.error("File {} already exists", newFile);
      return;
    }
    Path oldFile = renamedFile.currentFile();
    String operation = write ? "Renaming" : "Would rename";
    log.info("{} {} to {}", operation, oldFile, newFile);
    if (write) {
      try {
        Files.move(oldFile, newFile);
      } catch (IOException e) {
        log.error("Could not move {} to {}", oldFile, newFile, e);
      }
    }
  }

}

