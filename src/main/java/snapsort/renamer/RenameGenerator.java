package snapsort.renamer;

import snapsort.extractor.filename.FinalFileNameDateExtractor;
import snapsort.files.FileInfo;
import snapsort.files.FileTypeUtil;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.List;

@Slf4j
@ApplicationScoped
public class RenameGenerator {
  private static final DateTimeFormatter YEAR_FOLDER_NAME_FORMAT = DateTimeFormatter.ofPattern("yyyy");
  private static final DateTimeFormatter DAY_FOLDER_NAME_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");

  public List<RenamedFile> generateRenamedFileNames(Collection<FileInfo> files) {
    return files.stream().map(RenameGenerator::generateRenamedFile).sorted().toList();
  }

  private static RenamedFile generateRenamedFile(FileInfo fileInfo) {
    LocalDateTime timestamp = fileInfo.getTimestamp().getTime();
    String timeString = FinalFileNameDateExtractor.FILE_NAME_TARGET_FORMATTER.format(timestamp);
    String extension = generateExtension(fileInfo);
    String newName = timeString + "." + extension;
    String folderName =
        YEAR_FOLDER_NAME_FORMAT.format(timestamp) + "/" + DAY_FOLDER_NAME_FORMAT.format(timestamp) + "_";
    return new RenamedFile(newName, folderName, fileInfo.getPath());
  }

  private static String generateExtension(FileInfo fileInfo) {
    String currentExtension = FileTypeUtil.getLowercaseExtension(fileInfo.getPath());
    if ("jpeg".equals(currentExtension)) {
      return "jpg";
    }
    return currentExtension;
  }
}
