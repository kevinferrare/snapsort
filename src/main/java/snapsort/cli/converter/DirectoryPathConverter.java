package snapsort.cli.converter;

import picocli.CommandLine;

import java.nio.file.Files;
import java.nio.file.Path;

abstract class DirectoryPathConverter implements CommandLine.ITypeConverter<Path> {
  private final String argumentLabel;

  protected DirectoryPathConverter(String argumentLabel) {
    this.argumentLabel = argumentLabel;
  }

  @Override
  public Path convert(String value) {
    String trimmed = value == null ? "" : value.trim();
    if (trimmed.isEmpty()) {
      throw new CommandLine.TypeConversionException(argumentLabel + " must not be empty.");
    }
    Path path = Path.of(trimmed).toAbsolutePath().normalize();
    if (!Files.exists(path)) {
      throw new CommandLine.TypeConversionException(argumentLabel + " does not exist: " + path);
    }
    if (!Files.isDirectory(path)) {
      throw new CommandLine.TypeConversionException(argumentLabel + " is not a directory: " + path);
    }
    return path;
  }
}