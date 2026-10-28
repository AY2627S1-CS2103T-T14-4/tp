package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.JavaFxTestUtil.initializeToolkit;
import static seedu.address.testutil.JavaFxTestUtil.runOnFxThread;

import java.nio.file.Path;
import java.util.Optional;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.io.TempDir;

import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.DialogPane;
import javafx.stage.Stage;
import javafx.stage.Window;
import seedu.address.logic.LogicManager;
import seedu.address.model.ModelManager;
import seedu.address.model.PawPals;
import seedu.address.model.UserPrefs;
import seedu.address.storage.DataRecoveryInfo;
import seedu.address.storage.JsonPawPalsStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class UiManagerTest {

    @TempDir
    public Path testFolder;

    @BeforeAll
    public static void setUpJavaFxToolkit() throws InterruptedException {
        initializeToolkit();
    }

    @Test
    @Order(1)
    public void dataRecoveryWarningContent_withBackup_includesReasonAndBackupPath() {
        DataRecoveryInfo recoveryInfo = new DataRecoveryInfo(
                Path.of("pawpals.json"), Optional.of(Path.of("pawpals.json.bak")),
                "Phone numbers should only contain digits");

        String content = UiManager.getDataRecoveryWarningContent(recoveryInfo);

        assertTrue(content.contains("Reason: Phone numbers should only contain digits"));
        assertTrue(content.contains("A backup was created at:\n" + Path.of("pawpals.json.bak")));
    }

    @Test
    @Order(2)
    public void dataRecoveryWarningContent_withoutBackup_mentionsBackupFailure() {
        DataRecoveryInfo recoveryInfo = new DataRecoveryInfo(
                Path.of("pawpals.json"), Optional.empty(), "Invalid JSON");

        String content = UiManager.getDataRecoveryWarningContent(recoveryInfo);

        assertTrue(content.contains("Reason: Invalid JSON"));
        assertTrue(content.contains("PawPals could not create a backup of the rejected file."));
    }

    @Test
    @Order(3)
    public void start_withoutRecovery_showsMainWindow() throws Exception {
        runOnFxThread(() -> {
            Stage stage = new Stage();
            UiManager uiManager = new UiManager(createLogic(), testFolder.resolve("pawpals.json"), Optional.empty());

            uiManager.start(stage);

            assertTrue(stage.isShowing());
            stage.close();
            return null;
        });
    }

    @Test
    @Order(4)
    public void start_withRecovery_keepsWindowOpen() throws Exception {
        runOnFxThread(() -> {
            Stage stage = new Stage();
            DataRecoveryInfo recoveryInfo = new DataRecoveryInfo(testFolder.resolve("pawpals.json"),
                    Optional.of(testFolder.resolve("pawpals.json.bak")), "Invalid phone number");
            UiManager uiManager = new UiManager(createLogic(), recoveryInfo.dataFilePath(),
                    Optional.of(recoveryInfo));
            Platform.runLater(() -> clickDialogButton("Continue"));

            uiManager.start(stage);

            assertTrue(stage.isShowing());
            stage.close();
            return null;
        });
    }

    @Test
    @Order(5)
    public void showAlertDialogAndWait_withAlertDetails_closesAlert() throws Exception {
        runOnFxThread(() -> {
            Stage stage = new Stage();
            UiManager uiManager = new UiManager(createLogic(), testFolder.resolve("pawpals.json"), Optional.empty());
            uiManager.start(stage);
            Platform.runLater(UiManagerTest::closeVisibleDialog);

            uiManager.showAlertDialogAndWait(javafx.scene.control.Alert.AlertType.INFORMATION,
                    "Title", "Header", "Content");

            stage.close();
            return null;
        });
    }

    @Test
    @Order(6)
    public void start_withRecovery_exitClosesStage() throws Exception {
        runOnFxThread(() -> {
            Stage stage = new Stage();
            DataRecoveryInfo recoveryInfo = new DataRecoveryInfo(testFolder.resolve("pawpals.json"),
                    Optional.empty(), "Invalid JSON");
            UiManager uiManager = new UiManager(createLogic(), recoveryInfo.dataFilePath(),
                    Optional.of(recoveryInfo));
            Platform.runLater(() -> clickDialogButton("Exit"));

            uiManager.start(stage);

            assertTrue(!stage.isShowing());
            return null;
        });
    }

    private LogicManager createLogic() {
        Path dataFile = testFolder.resolve("pawpals.json");
        StorageManager storage = new StorageManager(new JsonPawPalsStorage(dataFile),
                new JsonUserPrefsStorage(testFolder.resolve("preferences.json")));
        return new LogicManager(new ModelManager(new PawPals(), new UserPrefs()), storage);
    }

    private static void clickDialogButton(String text) {
        Window.getWindows().stream()
                .filter(Window::isShowing)
                .map(Window::getScene)
                .filter(scene -> scene != null)
                .flatMap(scene -> scene.getRoot().lookupAll(".button").stream())
                .filter(node -> node instanceof Button)
                .map(node -> (Button) node)
                .filter(button -> text.equals(button.getText()))
                .findFirst()
                .ifPresent(Button::fire);
    }

    private static void closeVisibleDialog() {
        Window.getWindows().stream()
                .filter(Window::isShowing)
                .filter(window -> window.getScene() != null
                        && window.getScene().getRoot() instanceof DialogPane)
                .findFirst()
                .ifPresent(Window::hide);
    }
}
