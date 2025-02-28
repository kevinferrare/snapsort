package snapsort.files;

import snapsort.TimeStampWithSource;
import snapsort.extractor.DateChooser;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import one.util.streamex.StreamEx;
import org.apache.commons.io.file.PathUtils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Stream;

@Slf4j
@ApplicationScoped
public class FileLister {
  private final DateChooser dateChooser;

  @Inject
  public FileLister(DateChooser dateChooser) {
    this.dateChooser = dateChooser;
  }

  public List<FileInfo> listFiles(List<Path> folders) {
    return StreamEx.of(folders).flatCollection(this::listFiles).sorted().toList();
  }

  @SneakyThrows
  public List<FileInfo> listFiles(Path folder) {
    log.info("Analyzing files in folder {}", folder);
    if (!Files.exists(folder) || !Files.isDirectory(folder)) {
      log.error("Folder {} does not exist or is not a directory", folder);
      return Collections.emptyList();
    }
    try (Stream<Path> files = Files.walk(folder)) {
      return files.filter(FileLister::isSupportedFile).map(this::parseFromFile).filter(Objects::nonNull).toList();
    }
  }

  @SneakyThrows
  private static boolean isSupportedFile(Path file) {
    if (!Files.isRegularFile(file)) {
      return false;
    }
    if (PathUtils.sizeOf(file) == 0) {
      log.warn("File is empty, skipping {}", file);
      return false;
    }
    if (!FileTypeUtil.isSupportedExtension(file)) {
      log.warn("Extension unsupported, skipping {}", file);
      return false;
    }
    String fileName = file.getFileName().toString().toLowerCase(Locale.ENGLISH);
    if (fileName.startsWith(".pending-") || fileName.startsWith(".trashed-")) {
      // android seems to prefix files with these names and they are not valid
      log.warn("File prefixed by annotations that means they probably shouldn't be included {}", file);
      return false;
    }
    return true;
  }

  @SneakyThrows
  private FileInfo parseFromFile(Path file) {
    FileInfo fileInfo = new FileInfo();
    fileInfo.setPath(file);
    TimeStampWithSource timeStampWithSource = dateChooser.computeTimestamp(file);
    if (timeStampWithSource == null) {
      return null;
    }
    fileInfo.setTimestamp(timeStampWithSource);
    return fileInfo;
  }
}

