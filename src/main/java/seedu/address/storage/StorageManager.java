package seedu.address.storage;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;
import java.util.logging.Logger;

import seedu.address.commons.core.LogsCenter;
import seedu.address.commons.exceptions.DataLoadingException;
import seedu.address.model.ReadOnlyPawPals;
import seedu.address.model.ReadOnlyUserPrefs;
import seedu.address.model.UserPrefs;

/**
 * Manages storage of PawPals data in local storage.
 */
public class StorageManager implements Storage {

    private static final Logger logger = LogsCenter.getLogger(StorageManager.class);
    private JsonPawPalsStorage pawPalsStorage;
    private JsonUserPrefsStorage userPrefsStorage;

    /**
     * Creates a {@code StorageManager} with the given PawPals data and user prefs storage.
     */
    public StorageManager(JsonPawPalsStorage pawPalsStorage, JsonUserPrefsStorage userPrefsStorage) {
        this.pawPalsStorage = pawPalsStorage;
        this.userPrefsStorage = userPrefsStorage;
    }

    // ================ UserPrefs methods ==============================

    @Override
    public Path getUserPrefsFilePath() {
        return userPrefsStorage.getUserPrefsFilePath();
    }

    @Override
    public Optional<UserPrefs> readUserPrefs() throws DataLoadingException {
        return userPrefsStorage.readUserPrefs();
    }

    @Override
    public void saveUserPrefs(ReadOnlyUserPrefs userPrefs) throws IOException {
        userPrefsStorage.saveUserPrefs(userPrefs);
    }


    // ================ PawPals methods ==============================

    @Override
    public Path getPawPalsFilePath() {
        return pawPalsStorage.getPawPalsFilePath();
    }

    @Override
    public Optional<ReadOnlyPawPals> readPawPals() throws DataLoadingException {
        logger.fine("Attempting to read data from file: " + pawPalsStorage.getPawPalsFilePath());
        return pawPalsStorage.readPawPals();
    }

    @Override
    public Path backupCorruptedData() throws IOException {
        logger.fine("Backing up corrupted data file: " + pawPalsStorage.getPawPalsFilePath());
        return pawPalsStorage.backupCorruptedData();
    }

    @Override
    public void savePawPals(ReadOnlyPawPals pawPals) throws IOException {
        logger.fine("Attempting to write to data file: " + pawPalsStorage.getPawPalsFilePath());
        pawPalsStorage.savePawPals(pawPals);
    }

}
