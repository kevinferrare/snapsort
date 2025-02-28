package snapsort;

import io.quarkus.test.junit.main.LaunchResult;
import io.quarkus.test.junit.main.QuarkusMainLauncher;

import java.util.ArrayList;
import java.util.List;

public class ApplicationLauncher {
  private QuarkusMainLauncher launcher;

  private String inputFolders;
  private String outputFolder;
  private String dateMin;
  private String dateMax;
  private boolean write;
  private boolean readFilesystemDateModified;

  public ApplicationLauncher(QuarkusMainLauncher launcher) {
    this.launcher = launcher;
  }

  public ApplicationLauncher withInputFolders(String inputFolders) {
    this.inputFolders = inputFolders;
    return this;
  }

  public ApplicationLauncher withOutputFolder(String outputFolder) {
    this.outputFolder = outputFolder;
    return this;
  }

  public ApplicationLauncher withDateMin(String dateMin) {
    this.dateMin = dateMin;
    return this;
  }

  public ApplicationLauncher withDateMax(String dateMax) {
    this.dateMax = dateMax;
    return this;
  }

  public ApplicationLauncher withWrite(boolean write) {
    this.write = write;
    return this;
  }

  public ApplicationLauncher withReadFilesystemDateModified(boolean readFilesystemDateModified) {
    this.readFilesystemDateModified = readFilesystemDateModified;
    return this;
  }

  public LaunchResult run() {
    List<String> args = new ArrayList<>();
    if (inputFolders != null) {
      args.add("--input-folders");
      args.add(inputFolders);
    }
    if (outputFolder != null) {
      args.add("--output-folder");
      args.add(outputFolder);
    }
    if (dateMin != null) {
      args.add("--date-min");
      args.add(dateMin);
    }
    if (dateMax != null) {
      args.add("--date-max");
      args.add(dateMax);
    }
    if (write) {
      args.add("--write");
    }
    if (readFilesystemDateModified) {
      args.add("--read-filesystem-date-modified");
    }
    return launcher.launch(args.toArray(new String[0]));
  }

}
