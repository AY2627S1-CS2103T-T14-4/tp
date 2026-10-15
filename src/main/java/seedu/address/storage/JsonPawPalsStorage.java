package seedu.address.storage;

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;
import java.util.logging.Logger;

import seedu.address.commons.core.LogsCenter;
import seedu.address.commons.exceptions.DataLoadingException;
import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.commons.util.FileUtil;
import seedu.address.commons.util.JsonUtil;
import seedu.address.model.ReadOnlyPawPals;

/**
 * A class to access PawPals data stored as a JSON file on the hard disk.
 */
public class JsonPawPalsStorage {

    private static final Logger logger = LogsCenter.getLogger(JsonPawPalsStorage.class);

    private Path filePath;

    public JsonPawPalsStorage(Path filePath) {
        this.filePath = filePath;
    }

    public Path getPawPalsFilePath() {
        return filePath;
    }

    /**
     * Returns PawPals data as a {@link ReadOnlyPawPals}.
     * Returns {@code Optional.empty()} if storage file is not found.
     *
     * @throws DataLoadingException if loading the data from storage failed.
     */
    public Optional<ReadOnlyPawPals> readPawPals() throws DataLoadingException {
        return readPawPals(filePath);
    }

    /**
     * Similar to {@link #readPawPals()}.
     *
     * @param filePath location of the data. Cannot be null.
     * @throws DataLoadingException if loading the data from storage failed.
     */
    public Optional<ReadOnlyPawPals> readPawPals(Path filePath) throws DataLoadingException {
        requireNonNull(filePath);

        Optional<JsonSerializablePawPals> jsonPawPals = JsonUtil.readJsonFile(
                filePath, JsonSerializablePawPals.class);
        if (!jsonPawPals.isPresent()) {
            return Optional.empty();
        }

        try {
            return Optional.of(jsonPawPals.get().toModelType());
        } catch (IllegalValueException ive) {
            logger.info("Illegal values found in " + filePath + ": " + ive.getMessage());
            throw new DataLoadingException(ive);
        }
    }

    /**
     * Saves the given {@link ReadOnlyPawPals} to the storage.
     * @param pawPals cannot be null.
     * @throws IOException if there was any problem writing to the file.
     */
    public void savePawPals(ReadOnlyPawPals pawPals) throws IOException {
        savePawPals(pawPals, filePath);
    }

    /**
     * Similar to {@link #savePawPals(ReadOnlyPawPals)}.
     *
     * @param filePath location of the data. Cannot be null.
     */
    public void savePawPals(ReadOnlyPawPals pawPals, Path filePath) throws IOException {
        requireNonNull(pawPals);
        requireNonNull(filePath);

        FileUtil.createIfMissing(filePath);
        JsonUtil.saveJsonFile(new JsonSerializablePawPals(pawPals), filePath);
    }

}
