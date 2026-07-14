package snapsort.cli.converter;

import picocli.CommandLine;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class IsoLocalDateConverter implements CommandLine.ITypeConverter<LocalDate> {

  @Override
  public LocalDate convert(String value) {
    String trimmed = value == null ? "" : value.trim();
    if (trimmed.isEmpty()) {
      throw new CommandLine.TypeConversionException("Date must not be empty. Expected format: yyyy-MM-dd");
    }
    try {
      return LocalDate.parse(trimmed, DateTimeFormatter.ISO_DATE);
    } catch (DateTimeParseException e) {
      throw new CommandLine.TypeConversionException("Invalid date '" + value + "'. Expected format: yyyy-MM-dd");
    }
  }
}