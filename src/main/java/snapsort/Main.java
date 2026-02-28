package snapsort;

import io.quarkus.runtime.Quarkus;
import io.quarkus.runtime.QuarkusApplication;
import io.quarkus.runtime.annotations.QuarkusMain;
import io.quarkus.picocli.runtime.annotations.TopCommand;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import picocli.CommandLine;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

@QuarkusMain
@CommandLine.Command(mixinStandardHelpOptions = true)
@TopCommand
@Dependent
@Slf4j
public class Main implements QuarkusApplication, Runnable {
  @Inject
  private CommandLine.IFactory factory;

  @CommandLine.Option(names = { "--input-folders" }, description = "Input folders list, separated by commas", required = true)
  private String inputFolders;

  @CommandLine.Option(names = { "--output-folder" }, description = "Output folder", required = true)
  private String outputFolder;

  @CommandLine.Option(names = { "--date-min" }, description = "Min date of range of acceptable dates")
  private String dateMin;

  @CommandLine.Option(names = { "--date-max" }, description = "Max date of range of acceptable dates")
  private String dateMax;

  @CommandLine.Option(names = { "--write" }, description = "If no given, will do a dry run, no files will be written", defaultValue = "false")
  private boolean write;

  @CommandLine.Option(names = { "--read-filesystem-date-modified" },
      description = "Allow renaming according to filesystem dates modified (as a last resort)", defaultValue = "false")
  private boolean readFilesystemDateModified;

  @Inject
  private SnapsortOrchestrator orchestrator;

  private CommandLine commandLine;

  public static void main(String... args) {
    Quarkus.run(Main.class, args);
  }

  @Override
  public int run(String... args) {
    commandLine = new CommandLine(this, factory);
    return commandLine.execute(args);
  }

  @Override
  public void run() {
    List<Path> inputFolderList = parseInputFolders();
    if (inputFolderList.isEmpty()) {
      errorWithHelp("No input folder specified.");
      return;
    }
    if (StringUtils.isAllBlank(outputFolder)) {
      errorWithHelp("No output folder specified.");
      return;
    }
    orchestrator.execute(inputFolderList, Paths.get(outputFolder), write);
  }

  private void errorWithHelp(String error) {
    log.error(error);
    commandLine.usage(System.out);
  }

  private List<Path> parseInputFolders() {
    return Arrays.stream(StringUtils.defaultIfEmpty(inputFolders, "").split(",")).filter(StringUtils::isNoneBlank)
        .map(Paths::get)
        .map(Path::toAbsolutePath)
        .toList();
  }
}
