package seedu.address.ui;

import java.nio.file.Path;
import java.util.Optional;
import java.util.logging.Logger;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import seedu.address.MainApp;
import seedu.address.commons.core.LogsCenter;
import seedu.address.commons.util.StringUtil;
import seedu.address.logic.Logic;
import seedu.address.storage.DataRecoveryInfo;

/**
 * The manager of the UI component.
 */
public class UiManager implements Ui {

    public static final String ALERT_DIALOG_PANE_FIELD_ID = "alertDialogPane";

    private static final Logger logger = LogsCenter.getLogger(UiManager.class);
    private static final String ICON_APPLICATION = "/images/pawpals_32.png";

    private Logic logic;
    private Path dataFilePath;
    private Optional<DataRecoveryInfo> dataRecoveryInfo;
    private MainWindow mainWindow;

    /**
     * Creates a {@code UiManager} with the given {@code Logic} and the data file path
     * to show in the status bar.
     */
    public UiManager(Logic logic, Path dataFilePath, Optional<DataRecoveryInfo> dataRecoveryInfo) {
        this.logic = logic;
        this.dataFilePath = dataFilePath;
        this.dataRecoveryInfo = dataRecoveryInfo;
    }

    @Override
    public void start(Stage primaryStage) {
        logger.info("Starting UI...");

        //Set the application icon.
        primaryStage.getIcons().add(getImage(ICON_APPLICATION));

        try {
            mainWindow = new MainWindow(primaryStage, logic, dataFilePath);
            mainWindow.show(); //This should be called before creating other UI parts
            mainWindow.fillInnerParts();
            if (dataRecoveryInfo.isPresent() && !showDataRecoveryWarning(dataRecoveryInfo.get())) {
                primaryStage.close();
                Platform.exit();
            }

        } catch (Throwable e) {
            logger.severe(StringUtil.getDetails(e));
            showFatalErrorDialogAndShutdown("Fatal error during initializing", e);
        }
    }

    /**
     * Warns the user before an empty recovery model can overwrite a rejected data file.
     *
     * @return true if the user explicitly chooses to continue
     */
    private boolean showDataRecoveryWarning(DataRecoveryInfo recoveryInfo) {
        String backupMessage = recoveryInfo.backupFilePath()
                .map(path -> "A backup was created at:\n" + path)
                .orElse("PawPals could not create a backup of the rejected file.");
        ButtonType continueButton = new ButtonType("Continue");
        ButtonType exitButton = new ButtonType("Exit");
        Alert alert = new Alert(AlertType.WARNING, "", continueButton, exitButton);
        alert.getDialogPane().getStylesheets().add("view/DarkTheme.css");
        alert.initOwner(mainWindow.getPrimaryStage());
        alert.setTitle("Data recovery required");
        alert.setHeaderText("PawPals could not load " + recoveryInfo.dataFilePath());
        alert.setContentText("The application is using an empty client list. "
                + "Choose Continue to allow future commands to replace the rejected data file, "
                + "or Exit to close without saving.\n\nReason: " + recoveryInfo.loadingError()
                + "\n\n" + backupMessage);
        alert.getDialogPane().setId(ALERT_DIALOG_PANE_FIELD_ID);
        return alert.showAndWait().filter(continueButton::equals).isPresent();
    }

    private Image getImage(String imagePath) {
        return new Image(MainApp.class.getResourceAsStream(imagePath));
    }

    void showAlertDialogAndWait(Alert.AlertType type, String title, String headerText, String contentText) {
        showAlertDialogAndWait(mainWindow.getPrimaryStage(), type, title, headerText, contentText);
    }

    /**
     * Shows an alert dialog on {@code owner} with the given parameters.
     * This method only returns after the user has closed the alert dialog.
     */
    private static void showAlertDialogAndWait(Stage owner, AlertType type, String title, String headerText,
                                               String contentText) {
        final Alert alert = new Alert(type);
        alert.getDialogPane().getStylesheets().add("view/DarkTheme.css");
        alert.initOwner(owner);
        alert.setTitle(title);
        alert.setHeaderText(headerText);
        alert.setContentText(contentText);
        alert.getDialogPane().setId(ALERT_DIALOG_PANE_FIELD_ID);
        alert.showAndWait();
    }

    /**
     * Shows an error alert dialog with {@code title} and error message, {@code e},
     * and exits the application after the user has closed the alert dialog.
     */
    private void showFatalErrorDialogAndShutdown(String title, Throwable e) {
        logger.severe(title + " " + e.getMessage() + StringUtil.getDetails(e));
        showAlertDialogAndWait(Alert.AlertType.ERROR, title, e.getMessage(), e.toString());
        Platform.exit();
        System.exit(1);
    }

}
