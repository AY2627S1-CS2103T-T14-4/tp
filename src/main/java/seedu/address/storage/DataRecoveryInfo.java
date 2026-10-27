package seedu.address.storage;

import java.nio.file.Path;

/**
 * Describes a data file that could not be loaded and the backup created for recovery.
 */
public record DataRecoveryInfo(Path dataFilePath, Path backupFilePath, String loadingError) {
}
