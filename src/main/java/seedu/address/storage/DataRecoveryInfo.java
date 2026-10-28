package seedu.address.storage;

import java.nio.file.Path;
import java.util.Optional;

/**
 * Describes a data file that could not be loaded and the backup created for recovery.
 */
public record DataRecoveryInfo(Path dataFilePath, Optional<Path> backupFilePath, String loadingError) {
}
