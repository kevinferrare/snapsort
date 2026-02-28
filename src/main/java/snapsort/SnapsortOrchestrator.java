package snapsort;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import snapsort.files.FileInfo;
import snapsort.files.FileLister;
import snapsort.renamer.Deduplicator;
import snapsort.renamer.RenameGenerator;
import snapsort.renamer.RenamedFile;
import snapsort.renamer.Renamer;

import java.nio.file.Path;
import java.util.List;

/**
 * Orchestrates the snapsort pipeline: list files -> deduplicate dates -> generate names -> rename.
 */
@ApplicationScoped
@Slf4j
public class SnapsortOrchestrator {

  @Inject
  private FileLister fileLister;

  @Inject
  private Deduplicator deduplicator;

  @Inject
  private RenameGenerator renameGenerator;

  @Inject
  private Renamer renamer;

  public void execute(List<Path> inputFolders, Path outputFolder, boolean write) {
    log.info("Input folders: {} (write={})", inputFolders, write);
    List<FileInfo> files = fileLister.listFiles(inputFolders);
    log.info("Found {} files and choose the following dates:", files.size());
    files.forEach(file -> log.info(file.toString()));
    log.info("Deduplicating dates");
    List<FileInfo> deduplicatedFiles = deduplicator.deduplicateDates(files);
    deduplicatedFiles.forEach(file -> log.info(file.toString()));
    log.info("Generating new names");
    List<RenamedFile> renamedFiles = renameGenerator.generateRenamedFileNames(deduplicatedFiles);
    renamer.renameFiles(renamedFiles, outputFolder, write);
  }
}
