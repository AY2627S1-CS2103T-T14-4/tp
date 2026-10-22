package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.JavaFxTestUtil.initializeToolkit;
import static seedu.address.testutil.JavaFxTestUtil.runOnFxThread;
import static seedu.address.testutil.TypicalClients.ALICE;
import static seedu.address.testutil.TypicalClients.BENSON;

import java.nio.file.Files;
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
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import seedu.address.logic.LogicManager;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.PawPals;
import seedu.address.model.UserPrefs;
import seedu.address.model.appointment.Appointment;
import seedu.address.model.appointment.AppointmentDate;
import seedu.address.model.appointment.AppointmentTime;
import seedu.address.storage.JsonPawPalsStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.ClientBuilder;

/**
 * Tests rescheduling through the real command box, feedback display and observable client-list controls.
 */
public class EditAppointmentUiIntegrationTest {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-uuuu");
    private static final String BOOKING_DETAILS = "Call on arrival";
    private static final double WINDOW_WIDTH = 900;
    private static final double WINDOW_HEIGHT = 700;

    @TempDir
    public Path testFolder;

    @BeforeAll
    public static void setUpJavaFxToolkit() throws InterruptedException {
        initializeToolkit();
    }

    @Test
    public void enter_rescheduleAfterFind_refreshesRowAndFeedbackWithoutResettingFilter() throws Exception {
        runOnFxThread(() -> {
            String date = LocalDate.now().plusDays(1).format(DATE_FORMATTER);
            MainWindow window = createWindow(date);
            try {
                enter(window, "find " + BENSON.getName());
                assertTrue(labels(window).contains("Appointment: " + date + " 14:00 (" + BOOKING_DETAILS + ")"));

                enter(window, "editappt c/1 d/" + date + " t/14:00 ns/18:00");

                List<String> labels = labels(window);
                assertTrue(labels.contains("Appointment: " + date + " 18:00 (" + BOOKING_DETAILS + ")"));
                assertFalse(labels.contains("Appointment: " + date + " 14:00 (" + BOOKING_DETAILS + ")"));
                assertFalse(labels.contains(ALICE.getName().fullName));
                assertTrue(labels.contains(BENSON.getName().fullName));
                assertEquals("", input(window).getText());
                assertFalse(input(window).getStyleClass().contains(CommandBox.ERROR_STYLE_CLASS));
                assertEquals("Rescheduled appointment: " + BENSON.getName() + " " + date + " 14:00 → " + date
                        + " 18:00 et/19:30", feedback(window).getText());
                Appointment saved = new JsonPawPalsStorage(dataFile()).readPawPals().orElseThrow()
                        .getClientList().get(1).getAppointments().getFirst();
                assertEquals(new AppointmentTime("19:30"), saved.getEndTime().orElseThrow());
            } finally {
                window.getPrimaryStage().close();
            }
            return null;
        });
    }

    @Test
    public void enter_invalidReschedule_retainsInputAndBookingThenAllowsCorrection() throws Exception {
        runOnFxThread(() -> {
            String date = LocalDate.now().plusDays(1).format(DATE_FORMATTER);
            MainWindow window = createWindow(date);
            try {
                enter(window, "find " + BENSON.getName());
                String saved = Files.readString(dataFile());
                String target = "editappt c/1 d/" + date + " t/14:00";
                for (String suffix : new String[] {" ns/24:00", " ne/14:00"}) {
                    enter(window, target + suffix);
                    assertEquals(target + suffix, input(window).getText());
                    assertTrue(input(window).getStyleClass().contains(CommandBox.ERROR_STYLE_CLASS));
                    assertEquals(suffix.contains("24:00") ? AppointmentTime.MESSAGE_CONSTRAINTS
                            : Appointment.MESSAGE_END_NOT_AFTER_START, feedback(window).getText());
                    assertTrue(labels(window).contains("Appointment: " + date + " 14:00 (" + BOOKING_DETAILS + ")"));
                    assertFalse(labels(window).contains(ALICE.getName().fullName));
                    assertEquals(saved, Files.readString(dataFile()));
                }

                input(window).setText(target + " ne/15:45");
                assertFalse(input(window).getStyleClass().contains(CommandBox.ERROR_STYLE_CLASS));
                input(window).fireEvent(new ActionEvent());
                assertEquals("", input(window).getText());
                assertEquals("Rescheduled appointment: " + BENSON.getName() + " " + date + " 14:00 → " + date
                        + " 14:00 et/15:45", feedback(window).getText());
            } finally {
                window.getPrimaryStage().close();
            }
            return null;
        });
    }

    private MainWindow createWindow(String date) {
        Appointment appointment = new Appointment(new AppointmentDate(date), new AppointmentTime("14:00"),
                BOOKING_DETAILS).withEndTime(new AppointmentTime("15:30"));
        PawPals data = new PawPals();
        data.addClient(ALICE);
        data.addClient(new ClientBuilder(BENSON).withAppointments(appointment).build());
        Model model = new ModelManager(data, new UserPrefs());
        StorageManager storage = new StorageManager(new JsonPawPalsStorage(dataFile()),
                new JsonUserPrefsStorage(testFolder.resolve("userPrefs.json")));
        MainWindow window = new MainWindow(new Stage(), new LogicManager(model, storage), dataFile());
        window.fillInnerParts();
        layout(window);
        return window;
    }

    private void enter(MainWindow window, String command) {
        input(window).setText(command);
        input(window).fireEvent(new ActionEvent());
        layout(window);
    }

    private void layout(MainWindow window) {
        Parent root = window.getPrimaryStage().getScene().getRoot();
        root.resize(WINDOW_WIDTH, WINDOW_HEIGHT);
        root.applyCss();
        root.layout();
    }

    private TextField input(MainWindow window) {
        return (TextField) window.getPrimaryStage().getScene().lookup("#commandTextField");
    }

    private TextArea feedback(MainWindow window) {
        return (TextArea) window.getPrimaryStage().getScene().lookup("#resultDisplay");
    }

    private List<String> labels(MainWindow window) {
        return labels(window.getClientListPanel().getRoot());
    }

    private List<String> labels(Node node) {
        List<String> texts = new ArrayList<>();
        if (node instanceof Label label) {
            texts.add(label.getText());
        }
        if (node instanceof Parent parent) {
            parent.getChildrenUnmodifiable().forEach(child -> texts.addAll(labels(child)));
        }
        return texts;
    }

    private Path dataFile() {
        return testFolder.resolve("pawPals.json");
    }
}
