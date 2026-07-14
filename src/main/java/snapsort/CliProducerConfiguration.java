package snapsort;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import picocli.CommandLine;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * CDI producer for CLI-driven configuration beans ({@link DateChooserConfiguration}, {@link DateRange}).
 */
@ApplicationScoped
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
    LocalDate min = getDate(parseResult, "date-min");
    LocalDate max = getDate(parseResult, "date-max");
    if (min != null && max != null && max.isBefore(min)) {
      throw new IllegalArgumentException("Invalid date range: --date-max must be on or after --date-min");
    }
    return new DateRange(convert(min), convertMax(max));
  }

  private static LocalDateTime convert(LocalDate date) {
    return Optional.ofNullable(date).map(LocalDate::atStartOfDay).orElse(null);
  }

  private static LocalDateTime convertMax(LocalDate date) {
    return Optional.ofNullable(date).map(d -> d.plusDays(1).atStartOfDay()).orElse(null);
  }

  private static LocalDate getDate(CommandLine.ParseResult parseResult, String optionCode) {
    CommandLine.Model.OptionSpec option = parseResult.matchedOption(optionCode);
    if (option == null) {
      return null;
    }
    return (LocalDate) option.getValue();
  }
}
