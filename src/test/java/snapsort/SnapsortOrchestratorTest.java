package snapsort;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import snapsort.files.FileInfo;
import snapsort.files.FileLister;
import snapsort.renamer.Deduplicator;
import snapsort.renamer.RenameGenerator;
import snapsort.renamer.RenamedFile;
import snapsort.renamer.Renamer;

import java.nio.file.Path;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SnapsortOrchestratorTest {

  @Mock
  private FileLister fileLister;

  @Mock
  private Deduplicator deduplicator;

  @Mock
  private RenameGenerator renameGenerator;

  @Mock
  private Renamer renamer;

  @InjectMocks
  private SnapsortOrchestrator orchestrator;

  @Test
  void execute_delegatesFullPipeline() {
    List<Path> inputFolders = List.of(Path.of("/photos"));
    Path outputFolder = Path.of("/output");
    FileInfo fileInfo = mock(FileInfo.class);
    FileInfo deduplicated = mock(FileInfo.class);
    RenamedFile renamed = mock(RenamedFile.class);

    when(fileLister.listFiles(inputFolders)).thenReturn(List.of(fileInfo));
    when(deduplicator.deduplicateDates(List.of(fileInfo))).thenReturn(List.of(deduplicated));
    when(renameGenerator.generateRenamedFileNames(List.of(deduplicated))).thenReturn(List.of(renamed));

    orchestrator.execute(inputFolders, outputFolder, true);

    verify(fileLister).listFiles(inputFolders);
    verify(deduplicator).deduplicateDates(List.of(fileInfo));
    verify(renameGenerator).generateRenamedFileNames(List.of(deduplicated));
    verify(renamer).renameFiles(List.of(renamed), outputFolder, true);
  }

  @Test
  void execute_dryRun_passesWriteFalse() {
    List<Path> inputFolders = List.of(Path.of("/in"));
    Path outputFolder = Path.of("/out");

    when(fileLister.listFiles(anyList())).thenReturn(List.of());
    when(deduplicator.deduplicateDates(any())).thenReturn(List.of());
    when(renameGenerator.generateRenamedFileNames(any())).thenReturn(List.of());

    orchestrator.execute(inputFolders, outputFolder, false);

    verify(renamer).renameFiles(List.of(), outputFolder, false);
  }
}
