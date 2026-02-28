package snapsort;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Optional;

/**
 * CDI producer for CLI-driven configuration beans ({@link DateChooserConfiguration}, {@link DateRange}).
 */
@ApplicationScoped
@Slf4j
public class CliProducerConfiguration {

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
  DateRange dateRange(CommandLine.ParseResult parseResult) {
    LocalDate min = parseDate(parseResult, "date-min");
    LocalDate max = parseDate(parseResult, "date-max");
    return new DateRange(convert(min), convertMax(max));
  }

  private static LocalDateTime convert(LocalDate date) {
    return Optional.ofNullable(date).map(LocalDate::atStartOfDay).orElse(null);
  }

  private static LocalDateTime convertMax(LocalDate date) {
    return Optional.ofNullable(date).map(d -> d.plusDays(1).atStartOfDay()).orElse(null);
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
