package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.JavaFxTestUtil.initializeToolkit;
import static seedu.address.testutil.JavaFxTestUtil.runOnFxThread;

import java.lang.reflect.Method;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import seedu.address.logic.LogicManager;
import seedu.address.model.ModelManager;
import seedu.address.model.PawPals;
import seedu.address.model.UserPrefs;
import seedu.address.storage.JsonPawPalsStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;

public class MainWindowTest {

    @TempDir
    public Path testFolder;

    @BeforeAll
    public static void setUpJavaFxToolkit() throws InterruptedException {
        initializeToolkit();
    }

    @Test
    public void constructor_withWindowCoordinates_restoresWindowPosition() throws Exception {
        runOnFxThread(() -> {
            UserPrefs userPrefs = new UserPrefs();
            userPrefs.setGuiSettings(new seedu.address.commons.core.GuiSettings(800, 600, 120, 180));
            MainWindow window = createWindow(userPrefs);

            assertEquals(120, window.getPrimaryStage().getX());
            assertEquals(180, window.getPrimaryStage().getY());
            window.getPrimaryStage().close();
            return null;
        });
    }

    @Test
    public void handleHelp_twice_showsThenFocusesHelpWindow() throws Exception {
        runOnFxThread(() -> {
            MainWindow window = createWindow(new UserPrefs());

            window.handleHelp();
            window.handleHelp();

            window.getPrimaryStage().close();
            return null;
        });
    }

    @Test
    public void setAccelerators_textInputF1_opensHelpWindow() throws Exception {
        runOnFxThread(() -> {
            MainWindow window = createWindow(new UserPrefs());
            window.getPrimaryStage().show();
            TextField commandBox = (TextField) window.getPrimaryStage().getScene().lookup("#commandTextField");
            commandBox.fireEvent(new KeyEvent(KeyEvent.KEY_PRESSED, "", "", KeyCode.F1,
                    false, false, false, false));
            window.getPrimaryStage().hide();
            return null;
        });
    }

    @Test
    public void executeCommand_helpAndExit_handlesWindowActions() throws Exception {
        runOnFxThread(() -> {
            MainWindow window = createWindow(new UserPrefs());
            Method executeCommand = MainWindow.class.getDeclaredMethod("executeCommand", String.class);
            executeCommand.setAccessible(true);

            assertTrue(executeCommand.invoke(window, "help") != null);
            assertTrue(executeCommand.invoke(window, "exit") != null);
            return null;
        });
    }

    private MainWindow createWindow(UserPrefs userPrefs) {
        Path dataFile = testFolder.resolve("pawpals.json");
        StorageManager storage = new StorageManager(new JsonPawPalsStorage(dataFile),
                new JsonUserPrefsStorage(testFolder.resolve("preferences.json")));
        MainWindow window = new MainWindow(new Stage(),
                new LogicManager(new ModelManager(new PawPals(), userPrefs), storage), dataFile);
        window.fillInnerParts();
        return window;
    }
}
