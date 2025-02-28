package snapsort;

import io.quarkus.runtime.Quarkus;
import io.quarkus.runtime.QuarkusApplication;
import io.quarkus.runtime.annotations.QuarkusMain;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import picocli.CommandLine;
import snapsort.files.FileInfo;
import snapsort.files.FileLister;
import snapsort.renamer.Deduplicator;
import snapsort.renamer.RenameGenerator;
import snapsort.renamer.RenamedFile;
import snapsort.renamer.Renamer;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@QuarkusMain
@CommandLine.Command(mixinStandardHelpOptions = true)
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
  private FileLister fileLister;

  @Inject
  private Deduplicator deduplicator;

  @Inject
  private RenameGenerator renameGenerator;

  @Inject
  private Renamer renamer;

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
    log.info("Input folders: {} ({}), write {}", inputFolders, inputFolderList, write);
    List<FileInfo> files = fileLister.listFiles(inputFolderList);
    log.info("Found {} files and choose the following dates:", files.size());
    files.forEach(file -> log.info(file.toString()));
    log.info("Deduplicating dates");
    List<FileInfo> deduplicatedFiles = deduplicator.deduplicateDates(files);
    deduplicatedFiles.forEach(file -> log.info(file.toString()));
    log.info("Generating new names");
    List<RenamedFile> renamedFiles = renameGenerator.generateRenamedFileNames(deduplicatedFiles);
    Path destination = Paths.get(outputFolder);
    renamer.renameFiles(renamedFiles, destination, write);
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

  @Produces
  @ApplicationScoped
  DateChooserConfiguration dateChooserConfiguration(CommandLine.ParseResult parseResult) {
    DateChooserConfiguration res = new DateChooserConfiguration();
    CommandLine.Model.OptionSpec option = parseResult.matchedOption("read-filesystem-date-modified");
    if (option != null) {
      res.setReadFilesystemDateModified(option.getValue());
    }
    return res;
  }

  @Produces
  @ApplicationScoped
  DateRange dataSource(CommandLine.ParseResult parseResult) {
    LocalDate min = parseDate(parseResult, "date-min");
    LocalDate max = parseDate(parseResult, "date-max");
    return new DateRange(convert(min), convert(max));
  }

  private static LocalDateTime convert(LocalDate date) {
    return Optional.ofNullable(date).map(LocalDate::atStartOfDay).orElse(null);
  }

  private static LocalDate parseDate(CommandLine.ParseResult parseResult, String optionCode) {
    CommandLine.Model.OptionSpec option = parseResult.matchedOption(optionCode);
    if (option == null) {
      return null;
    }
    String value = option.getValue().toString();
    try {
      return LocalDate.parse(value, DateTimeFormatter.ISO_DATE);
    } catch (DateTimeParseException e) {
      log.error("Could not parse date from option {}: {}", optionCode, value);
      throw new IllegalArgumentException("Invalid date format", e);
    }
  }
}
