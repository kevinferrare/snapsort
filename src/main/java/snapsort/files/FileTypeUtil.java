package snapsort.files;

import org.apache.commons.io.FilenameUtils;

import java.nio.file.Path;
import java.util.Locale;
import java.util.Set;

public class FileTypeUtil {
  private static final Set<String> SUPPORTED_EXTENSIONS = Set.of(
      "mkv", "jpeg", "jpg", "avi", "mp4", "mov", "png", "webp", "gif");

  public static boolean isJpegFile(Path file) {
    String extension = getLowercaseExtension(file);
    return "jpeg".equals(extension) || "jpg".equals(extension);
  }

  public static boolean isSupportedExtension(Path file) {
    String extension = getLowercaseExtension(file);
    return SUPPORTED_EXTENSIONS.contains(extension);
  }

  public static String withoutExtension(Path file) {
    String fileName = file.getFileName().toString();
    return FilenameUtils.removeExtension(fileName);
  }

  public static String getLowercaseExtension(Path file) {
    return FilenameUtils.getExtension(file.getFileName().toString().toLowerCase(Locale.ROOT));
  }
}
