package snapsort;

import lombok.Data;

/**
 * Date chooser configuration. Injectable from command line option.
 */
@Data
public class DateChooserConfiguration {
  private boolean readFilesystemDateModified;
}
