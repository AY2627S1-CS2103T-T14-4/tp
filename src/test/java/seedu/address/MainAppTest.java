package seedu.address;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import seedu.address.commons.exceptions.DataLoadingException;
import seedu.address.model.Model;
import seedu.address.model.ReadOnlyPawPals;
import seedu.address.model.ReadOnlyUserPrefs;
import seedu.address.model.UserPrefs;
import seedu.address.storage.Storage;

public class MainAppTest {

    @Test
    public void initModelManager_dataLoadFails_preservesBackupAndStartsEmpty() {
        MainApp app = new MainApp();
        Path dataFile = Path.of("data.json");
        Storage storage = failingStorage(dataFile, false);

        Model model = app.initModelManager(storage, new UserPrefs());

        assertTrue(model.getClientList().isEmpty());
    }

    @Test
    public void initModelManager_backupFails_stillStartsEmpty() {
        MainApp app = new MainApp();
        Storage storage = failingStorage(Path.of("data.json"), true);

        Model model = app.initModelManager(storage, new UserPrefs());

        assertTrue(model.getClientList().isEmpty());
    }

    private Storage failingStorage(Path dataFile, boolean backupFails) {
        return new Storage() {
            @Override
            public Path getUserPrefsFilePath() {
                return Path.of("preferences.json");
            }

            @Override
            public Optional<UserPrefs> readUserPrefs() {
                return Optional.empty();
            }

            @Override
            public void saveUserPrefs(ReadOnlyUserPrefs userPrefs) {
            }

            @Override
            public Path getPawPalsFilePath() {
                return dataFile;
            }

            @Override
            public Optional<ReadOnlyPawPals> readPawPals() throws DataLoadingException {
                throw new DataLoadingException(new IllegalArgumentException("invalid data"));
            }

            @Override
            public Path backupCorruptedData() throws IOException {
                if (backupFails) {
                    throw new IOException("backup failed");
                }
                return Path.of("data.json.bak");
            }

            @Override
            public void savePawPals(ReadOnlyPawPals pawPals) {
            }
        };
    }
}
