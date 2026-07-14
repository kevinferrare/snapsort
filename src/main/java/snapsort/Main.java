package snapsort;

import io.quarkus.runtime.Quarkus;
import io.quarkus.runtime.QuarkusApplication;
import io.quarkus.runtime.annotations.QuarkusMain;
import io.quarkus.picocli.runtime.annotations.TopCommand;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import picocli.CommandLine;
import snapsort.cli.converter.InputFolderPathConverter;
import snapsort.cli.converter.IsoLocalDateConverter;
import snapsort.cli.converter.OutputFolderPathConverter;

import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

@QuarkusMain
@CommandLine.Command(mixinStandardHelpOptions = true)
@TopCommand
@Dependent
public class Main implements QuarkusApplication, Runnable {
  private static final String JUL_LOG_MANAGER_PROPERTY = "java.util.logging.manager";
  private static final String JBOSS_LOG_MANAGER = "org.jboss.logmanager.LogManager";

  static {
    // Set JUL LogManager early to avoid Quarkus warning when java.util.logging.manager is missing.
    System.setProperty(JUL_LOG_MANAGER_PROPERTY,
        System.getProperty(JUL_LOG_MANAGER_PROPERTY, JBOSS_LOG_MANAGER));
  }

  @Inject
  private CommandLine.IFactory factory;

  @CommandLine.Option(names = { "--input-folders" }, description = "Input folders list, separated by commas", required = true,
      split = ",", converter = InputFolderPathConverter.class)
  private List<Path> inputFolders;


  @CommandLine.Option(names = { "--output-folder" }, description = "Output folder", required = true,
      converter = OutputFolderPathConverter.class)
  private Path outputFolder;

  @CommandLine.Option(names = { "--date-min" }, description = "Min date of range of acceptable dates (yyyy-MM-dd)",
      converter = IsoLocalDateConverter.class)
  private LocalDate dateMin;

  @CommandLine.Option(names = { "--date-max" }, description = "Max date of range of acceptable dates (yyyy-MM-dd)",
      converter = IsoLocalDateConverter.class)
  private LocalDate dateMax;

  @CommandLine.Option(names = { "--write" }, description = "If no given, will do a dry run, no files will be written", defaultValue = "false")
  private boolean write;

  @CommandLine.Option(names = { "--read-filesystem-date-modified" },
      description = "Allow renaming according to filesystem dates modified (as a last resort)", defaultValue = "false")
  private boolean readFilesystemDateModified;

  @Inject
  private SnapsortOrchestrator orchestrator;

  public static void main(String... args) {
    Quarkus.run(Main.class, args);
  }

  @Override
  public int run(String... args) {
    return new CommandLine(this, factory).execute(args);
  }

  @Override
  public void run() {
    orchestrator.execute(inputFolders, outputFolder, write);
  }
}
