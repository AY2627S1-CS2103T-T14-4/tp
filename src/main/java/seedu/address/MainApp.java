package seedu.address;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.logging.Logger;

import javafx.application.Application;
import javafx.stage.Stage;
import seedu.address.commons.core.LogsCenter;
import seedu.address.commons.exceptions.DataLoadingException;
import seedu.address.commons.util.StringUtil;
import seedu.address.logic.Logic;
import seedu.address.logic.LogicManager;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.PawPals;
import seedu.address.model.ReadOnlyPawPals;
import seedu.address.model.ReadOnlyUserPrefs;
import seedu.address.model.UserPrefs;
import seedu.address.model.util.SampleDataUtil;
import seedu.address.storage.JsonPawPalsStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.Storage;
import seedu.address.storage.StorageManager;
import seedu.address.ui.Ui;
import seedu.address.ui.UiManager;

/**
 * Runs the application.
 */
public class MainApp extends Application {

    public static final String VERSION = "V0.5.1";

    private static final Logger logger = LogsCenter.getLogger(MainApp.class);
    private static final Path USER_PREFS_FILE_PATH = Paths.get("preferences.json");
    private static final Path PAW_PALS_FILE_PATH = Paths.get("data", "pawpals.json");

    protected Ui ui;
    protected Logic logic;
    protected Storage storage;
    protected Model model;

    @Override
    public void init() throws Exception {
        logger.info("==============================[ Initializing PawPals ]==============================");
        super.init();

        JsonUserPrefsStorage userPrefsStorage = new JsonUserPrefsStorage(USER_PREFS_FILE_PATH);
        UserPrefs userPrefs = initPrefs(userPrefsStorage);
        JsonPawPalsStorage pawPalsStorage = new JsonPawPalsStorage(PAW_PALS_FILE_PATH);
        storage = new StorageManager(pawPalsStorage, userPrefsStorage);

        model = initModelManager(storage, userPrefs);

        logic = new LogicManager(model, storage);

        ui = new UiManager(logic, storage.getPawPalsFilePath());
    }

    /**
     * Returns a {@code ModelManager} with the data from {@code storage}'s PawPals data and {@code userPrefs}. <br>
     * The data from the sample PawPals data will be used instead if {@code storage}'s PawPals data is not found,
     * or an empty PawPals data will be used instead if errors occur when reading {@code storage}'s PawPals data.
     */
    private Model initModelManager(Storage storage, ReadOnlyUserPrefs userPrefs) {
        logger.info("Using data file : " + storage.getPawPalsFilePath());

        Optional<ReadOnlyPawPals> pawPalsOptional;
        ReadOnlyPawPals initialData;
        try {
            pawPalsOptional = storage.readPawPals();
            if (pawPalsOptional.isEmpty()) {
                logger.info("Creating a new data file " + storage.getPawPalsFilePath()
                        + " populated with sample client data.");
            }
            initialData = pawPalsOptional.orElseGet(SampleDataUtil::getSamplePawPals);
        } catch (DataLoadingException e) {
            logger.warning("Data file at " + storage.getPawPalsFilePath() + " could not be loaded."
                    + " Will be starting with empty client data.");
            initialData = new PawPals();
        }

        return new ModelManager(initialData, userPrefs);
    }

    /**
     * Returns a {@code UserPrefs} using the file at {@code storage}'s user prefs file path,
     * or a new {@code UserPrefs} with default configuration if errors occur when
     * reading from the file.
     */
    protected UserPrefs initPrefs(JsonUserPrefsStorage storage) {
        Path prefsFilePath = storage.getUserPrefsFilePath();
        logger.info("Using preference file : " + prefsFilePath);

        UserPrefs initializedPrefs;
        try {
            Optional<UserPrefs> prefsOptional = storage.readUserPrefs();
            if (prefsOptional.isEmpty()) {
                logger.info("Creating new preference file " + prefsFilePath);
            }
            initializedPrefs = prefsOptional.orElse(new UserPrefs());
        } catch (DataLoadingException e) {
            logger.warning("Preference file at " + prefsFilePath + " could not be loaded."
                    + " Using default preferences.");
            initializedPrefs = new UserPrefs();
        }

        //Update prefs file in case it was missing to begin with or there are new/unused fields
        try {
            storage.saveUserPrefs(initializedPrefs);
        } catch (IOException e) {
            logger.warning("Failed to save preference file : " + StringUtil.getDetails(e));
        }

        return initializedPrefs;
    }

    @Override
    public void start(Stage primaryStage) {
        logger.info("Starting PawPals " + MainApp.VERSION);
        ui.start(primaryStage);
    }

    @Override
    public void stop() {
        logger.info("============================== [ Stopping PawPals ] ================================");
        try {
            storage.saveUserPrefs(model.getUserPrefs());
        } catch (IOException e) {
            logger.severe("Failed to save preferences " + StringUtil.getDetails(e));
        }
    }
}
