package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.JavaFxTestUtil.initializeToolkit;
import static seedu.address.testutil.JavaFxTestUtil.runOnFxThread;
import static seedu.address.testutil.TypicalClients.ALICE;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javafx.event.ActionEvent;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import seedu.address.logic.LogicManager;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.AddCommand;
import seedu.address.logic.commands.ClearCommand;
import seedu.address.logic.commands.ListCommand;
import seedu.address.model.ModelManager;
import seedu.address.model.PawPals;
import seedu.address.model.UserPrefs;
import seedu.address.storage.JsonPawPalsStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.ClientUtil;

/**
 * Tests feedback through the real command box, parser, logic and storage.
 */
public class FeedbackUiIntegrationTest {
    @TempDir
    public Path testFolder;

    @BeforeAll
    public static void setUpJavaFxToolkit() throws InterruptedException {
        initializeToolkit();
    }

    @Test
    public void constructor_nullCommandExecutor_throwsNullPointerException() throws Exception {
        runOnFxThread(() -> {
            assertThrows(NullPointerException.class, () -> new CommandBox(null));
            return null;
        });
    }

    @Test
    public void enter_errorsAndCorrections_updatesFeedbackAndInputIndependently() throws Exception {
        runOnFxThread(() -> {
            MainWindow window = createWindow(testFolder.resolve("pawPals.json"));
            try {
                assertFeedback(window, "Info", "Enter a command. Use help to view available commands.");
                enter(window, "unknown");
                assertFeedback(window, "Error", Messages.MESSAGE_UNKNOWN_COMMAND);
                assertEquals("unknown", input(window).getText());
                assertTrue(input(window).getStyleClass().contains(CommandBox.ERROR_STYLE_CLASS));

                enter(window, "unknown");
                assertEquals(1, input(window).getStyleClass().stream()
                        .filter(CommandBox.ERROR_STYLE_CLASS::equals).count());
                input(window).setText("list");
                assertFalse(input(window).getStyleClass().contains(CommandBox.ERROR_STYLE_CLASS));
                assertFeedback(window, "Error", Messages.MESSAGE_UNKNOWN_COMMAND);
                input(window).fireEvent(new ActionEvent());
                assertFeedback(window, "Info", ListCommand.MESSAGE_SUCCESS);
                assertEquals("", input(window).getText());

                String duplicate = ClientUtil.getAddCommand(ALICE);
                enter(window, duplicate);
                assertFeedback(window, "Error", AddCommand.MESSAGE_DUPLICATE_CLIENT);
                assertEquals(duplicate, input(window).getText());

                enter(window, "clear");
                assertFeedback(window, "Success", ClearCommand.MESSAGE_SUCCESS);
                assertEquals("", input(window).getText());
                assertFalse(input(window).getStyleClass().contains(CommandBox.ERROR_STYLE_CLASS));
                enter(window, "list");
                assertFeedback(window, "Info", ListCommand.MESSAGE_SUCCESS);
                enter(window, "unknown");
                assertFeedback(window, "Error", Messages.MESSAGE_UNKNOWN_COMMAND);
            } finally {
                window.getPrimaryStage().close();
            }
            return null;
        });
    }

    @Test
    public void enter_saveFailure_displaysErrorInsteadOfCommandSuccess() throws Exception {
        Path invalidFile = Files.createDirectory(testFolder.resolve("directory"));
        runOnFxThread(() -> {
            MainWindow window = createWindow(invalidFile);
            try {
                enter(window, "list");
                assertEquals("Error", status(window).getText());
                assertTrue(feedback(window).getText().startsWith("Could not save data"));
                assertEquals("list", input(window).getText());
                assertTrue(input(window).getStyleClass().contains(CommandBox.ERROR_STYLE_CLASS));
            } finally {
                window.getPrimaryStage().close();
            }
            return null;
        });
    }

    private MainWindow createWindow(Path dataFile) {
        PawPals data = new PawPals();
        data.addClient(ALICE);
        ModelManager model = new ModelManager(data, new UserPrefs());
        StorageManager storage = new StorageManager(new JsonPawPalsStorage(dataFile),
                new JsonUserPrefsStorage(testFolder.resolve("userPrefs.json")));
        MainWindow window = new MainWindow(new Stage(), new LogicManager(model, storage), dataFile);
        window.fillInnerParts();
        return window;
    }

    private static void enter(MainWindow window, String command) {
        input(window).setText(command);
        input(window).fireEvent(new ActionEvent());
    }

    private static TextField input(MainWindow window) {
        return (TextField) window.getPrimaryStage().getScene().lookup("#commandTextField");
    }

    private static TextArea feedback(MainWindow window) {
        return (TextArea) window.getPrimaryStage().getScene().lookup("#resultDisplay");
    }

    private static Label status(MainWindow window) {
        return (Label) window.getPrimaryStage().getScene().lookup("#feedbackStatus");
    }

    private static void assertFeedback(MainWindow window, String expectedStatus, String expectedMessage) {
        assertEquals(expectedStatus, status(window).getText());
        assertEquals(expectedMessage, feedback(window).getText());
    }
}
