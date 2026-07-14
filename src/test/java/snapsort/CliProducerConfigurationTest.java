package snapsort;

import org.junit.jupiter.api.Test;
import picocli.CommandLine;
import snapsort.cli.converter.IsoLocalDateConverter;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class CliProducerConfigurationTest {

  private final CliProducerConfiguration producer = new CliProducerConfiguration();

  private static CommandLine.ParseResult parse(String... args) {
    @CommandLine.Command
    class Stub {
      @CommandLine.Option(names = "--date-min", converter = IsoLocalDateConverter.class)
      LocalDate dateMin;
      @CommandLine.Option(names = "--date-max", converter = IsoLocalDateConverter.class)
      LocalDate dateMax;
      @CommandLine.Option(names = "--read-filesystem-date-modified", defaultValue = "false")
      boolean readFilesystemDateModified;
    }
    return new CommandLine(new Stub()).parseArgs(args);
  }

  @Test
  void dateRange_bothDatesProvided() {
    CommandLine.ParseResult result = parse("--date-min", "2024-01-15", "--date-max", "2024-06-01");
    DateRange range = producer.dateRange(result);

    assertEquals(LocalDateTime.of(2024, 1, 15, 0, 0), range.min());
    // max is exclusive: next day at midnight
    assertEquals(LocalDateTime.of(2024, 6, 2, 0, 0), range.max());
  }

  @Test
  void dateRange_noDatesProvided() {
    CommandLine.ParseResult result = parse();
    DateRange range = producer.dateRange(result);

    assertNull(range.min());
    assertNull(range.max());
  }

  @Test
  void dateRange_invalidDate_failsAtParsing() {
    assertThrows(CommandLine.ParameterException.class, () -> parse("--date-min", "not-a-date"));
  }

  @Test
  void dateRange_maxBeforeMin_throws() {
    CommandLine.ParseResult result = parse("--date-min", "2024-06-01", "--date-max", "2024-01-15");
    assertThrows(IllegalArgumentException.class, () -> producer.dateRange(result));
  }

  @Test
  void dateChooserConfiguration_flagSet() {
    CommandLine.ParseResult result = parse("--read-filesystem-date-modified");
    DateChooserConfiguration config = producer.dateChooserConfiguration(result);

    assertTrue(config.isReadFilesystemDateModified());
  }

  @Test
  void dateChooserConfiguration_flagNotSet() {
    CommandLine.ParseResult result = parse();
    DateChooserConfiguration config = producer.dateChooserConfiguration(result);

    assertFalse(config.isReadFilesystemDateModified());
  }
}
