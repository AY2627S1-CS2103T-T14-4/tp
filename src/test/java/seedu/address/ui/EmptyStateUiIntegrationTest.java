package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.EmptyStateFixtures.CLIENT_WITH_APPOINTMENT;
import static seedu.address.testutil.EmptyStateFixtures.EMPTY_CLIENT;
import static seedu.address.testutil.JavaFxTestUtil.initializeToolkit;
import static seedu.address.testutil.JavaFxTestUtil.runOnFxThread;

import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import seedu.address.logic.LogicManager;
import seedu.address.model.ModelManager;
import seedu.address.model.PawPals;
import seedu.address.model.UserPrefs;
import seedu.address.model.client.Client;
import seedu.address.storage.JsonPawPalsStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.ClientUtil;

/**
 * Tests empty-state transitions through commands and observable JavaFX client cards.
 */
public class EmptyStateUiIntegrationTest {
    @TempDir
    public Path testFolder;

    @BeforeAll
    public static void setUpJavaFxToolkit() throws InterruptedException {
        initializeToolkit();
    }

    @Test
    public void enter_addSearchListDeleteAndClear_updatesEmptyListState() throws Exception {
        runOnFxThread(() -> {
            MainWindow window = createWindow();
            try {
                assertPlaceholder(window, "No clients yet. Use addc to create a client.");
                enter(window, "find Nobody");
                assertPlaceholder(window, "No clients yet. Use addc to create a client.");
                enter(window, ClientUtil.getAddCommand(EMPTY_CLIENT));
                assertEquals(1, listView(window).getItems().size());

                for (String search : List.of("find Nobody", "findp Nobody")) {
                    enter(window, search);
                    assertPlaceholder(window, "No matching clients. Try another search or use list.");
                    assertEquals("Info", ((Label) window.getPrimaryStage().getScene()
                            .lookup("#feedbackStatus")).getText());
                    enter(window, "list");
                    assertEquals(1, listView(window).getItems().size());
                }

                enter(window, "delc 1");
                assertPlaceholder(window, "No clients yet. Use addc to create a client.");
                enter(window, ClientUtil.getAddCommand(EMPTY_CLIENT));
                enter(window, "find Nobody");
                enter(window, "clear");
                assertPlaceholder(window, "No clients yet. Use addc to create a client.");
            } finally {
                window.getPrimaryStage().close();
            }
            return null;
        });
    }

    @Test
    public void enter_petAndNoteChanges_refreshesCardEmptyStates() throws Exception {
        runOnFxThread(() -> {
            MainWindow window = createWindow(EMPTY_CLIENT);
            try {
                assertTrue(labels(window).contains("No pets recorded"));
                enter(window, "addp c/1 n/Milo s/Dog b/Poodle");
                assertFalse(labels(window).contains("No pets recorded"));
                assertTrue(labels(window).contains("No notes recorded"));
                enter(window, "note c/1 pn/Milo ty/sens nt/Sensitive paws");
                assertFalse(labels(window).contains("No notes recorded"));
                assertTrue(labels(window).contains("⚠ Sens: Sensitive paws"));
                enter(window, "delnote c/1 pn/Milo ty/sens");
                assertTrue(labels(window).contains("No notes recorded"));
                assertFalse(labels(window).contains("⚠ Sens: Sensitive paws"));
                enter(window, "delp c/1 pn/Milo");
                assertTrue(labels(window).contains("No pets recorded"));
                assertFalse(labels(window).contains("No notes recorded"));
            } finally {
                window.getPrimaryStage().close();
            }
            return null;
        });
    }

    @Test
    public void enter_appointmentCompletedOrCancelled_updatesAppointmentAndHistoryEmptyStates() throws Exception {
        runOnFxThread(() -> {
            MainWindow window = createWindow(CLIENT_WITH_APPOINTMENT);
            try {
                enter(window, "schedule c/1");
                assertFalse(labels(window).contains("No active appointments"));
                assertTrue(labels(window).contains("No completed sessions"));
                enter(window, "done c/1 d/01-01-2020 t/10:00 nt/Trim completed");
                assertTrue(labels(window).contains("No active appointments"));
                assertFalse(labels(window).contains("No completed sessions"));
                assertTrue(labels(window).contains("01-01-2020 10:00 · No pets linked"));
                assertTrue(labels(window).contains("Notes: Trim completed"));
                assertEquals("Success", ((Label) window.getPrimaryStage().getScene()
                        .lookup("#feedbackStatus")).getText());
            } finally {
                window.getPrimaryStage().close();
            }

            MainWindow cancellationWindow = createWindow(EMPTY_CLIENT);
            try {
                String date = LocalDate.now().plusDays(1).format(DateTimeFormatter.ofPattern("dd-MM-uuuu"));
                enter(cancellationWindow, "appt c/1 d/" + date + " t/10:00");
                assertFalse(labels(cancellationWindow).contains("No active appointments"));
                enter(cancellationWindow, "cancel c/1 d/" + date + " t/10:00");
                assertTrue(labels(cancellationWindow).contains("No active appointments"));
                assertTrue(labels(cancellationWindow).contains("No completed sessions"));
            } finally {
                cancellationWindow.getPrimaryStage().close();
            }
            return null;
        });
    }

    private MainWindow createWindow(Client... clients) {
        PawPals data = new PawPals();
        for (Client client : clients) {
            data.addClient(client);
        }
        Path dataFile = testFolder.resolve("pawPals.json");
        StorageManager storage = new StorageManager(new JsonPawPalsStorage(dataFile),
                new JsonUserPrefsStorage(testFolder.resolve("userPrefs.json")));
        MainWindow window = new MainWindow(new Stage(),
                new LogicManager(new ModelManager(data, new UserPrefs()), storage), dataFile);
        window.fillInnerParts();
        layout(window);
        return window;
    }

    private static void enter(MainWindow window, String command) {
        TextField input = (TextField) window.getPrimaryStage().getScene().lookup("#commandTextField");
        input.setText(command);
        input.fireEvent(new ActionEvent());
        assertEquals("", input.getText(), "Command should succeed: " + command);
        layout(window);
    }

    private static void layout(MainWindow window) {
        Parent root = window.getPrimaryStage().getScene().getRoot();
        root.resize(450, 600);
        root.applyCss();
        root.layout();
    }

    private static ListView<?> listView(MainWindow window) {
        return (ListView<?>) window.getClientListPanel().getRoot().lookup("#clientListView");
    }

    private static void assertPlaceholder(MainWindow window, String expected) {
        assertTrue(listView(window).getItems().isEmpty());
        assertEquals(expected, ((Label) listView(window).getPlaceholder()).getText());
    }

    private static List<String> labels(MainWindow window) {
        return labels(window.getClientListPanel().getRoot());
    }

    private static List<String> labels(Node node) {
        List<String> texts = new ArrayList<>();
        if (!node.isVisible()) {
            return texts;
        }
        if (node instanceof Label label) {
            texts.add(label.getText());
        }
        if (node instanceof Parent parent) {
            parent.getChildrenUnmodifiable().forEach(child -> texts.addAll(labels(child)));
        }
        return texts;
    }
}
