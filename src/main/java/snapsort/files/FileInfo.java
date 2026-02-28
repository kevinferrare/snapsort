package snapsort.files;

import snapsort.TimeStampWithSource;

import java.nio.file.Path;

public record FileInfo(Path path, TimeStampWithSource timestamp) {
}
