package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.JavaFxTestUtil.initializeToolkit;
import static seedu.address.testutil.JavaFxTestUtil.runOnFxThread;
import static seedu.address.testutil.TypicalClients.ALICE;

import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Label;
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
import seedu.address.model.client.Client;
import seedu.address.model.pet.Breed;
import seedu.address.model.pet.Pet;
import seedu.address.model.pet.PetName;
import seedu.address.model.pet.Species;
import seedu.address.storage.JsonPawPalsStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.ClientBuilder;
import seedu.address.testutil.HistoryFixtures;

/** Exercises completion through the command box, observable list and real JSON storage. */
public class HistoryUiIntegrationTest {
    @TempDir
    public Path testFolder;

    @BeforeAll
    public static void setUpJavaFxToolkit() throws InterruptedException {
        initializeToolkit();
    }

    @Test
    public void enter_doneAfterFind_refreshesHistoryAndPreservesAllRecordsOnReload() throws Exception {
        runOnFxThread(() -> {
            String date = LocalDate.now().minusDays(1).format(DateTimeFormatter.ofPattern("dd-MM-uuuu"));
            Appointment booking = new Appointment(new AppointmentDate(date), new AppointmentTime("14:00"),
                    "Call on arrival", Set.of(new PetName("Milo")))
                    .withEndTime(new AppointmentTime("15:30"));
            Client owner = new ClientBuilder(HistoryFixtures.clientWithHistory(3)).withName("History Owner")
                    .withPets(new Pet(new PetName("Milo"), new Species("Dog"), new Breed("Poodle")))
                    .withAppointments(booking).build();
            PawPals data = new PawPals();
            data.addClient(ALICE);
            data.addClient(owner);
            Model model = new ModelManager(data, new UserPrefs());
            JsonPawPalsStorage storage = new JsonPawPalsStorage(testFolder.resolve("pawPals.json"));
            StorageManager manager = new StorageManager(storage,
                    new JsonUserPrefsStorage(testFolder.resolve("prefs.json")));
            MainWindow window = new MainWindow(new Stage(), new LogicManager(model, manager),
                    storage.getPawPalsFilePath());
            window.fillInnerParts();
            try {
                enter(window, "find History Owner");
                enter(window, "schedule c/1");
                assertTrue(labels(window).contains("Appointment: " + date + " 14:00–15:30 · Milo (Call on arrival)"));
                ClientListPanel originalPanel = window.getClientListPanel();
                enter(window, "done c/1 d/" + date + " t/14:00 nt/Fresh trim");
                assertSame(originalPanel, window.getClientListPanel());
                assertFalse(labels(window).stream().anyMatch(text -> text.startsWith("Appointment: ")));
                assertTrue(labels(window).contains(date + " 14:00–15:30 · Milo"));
                assertTrue(labels(window).contains("Notes: Fresh trim"));
                assertTrue(labels(window).contains("Notes: Oldest visit"));
                assertFalse(labels(window).contains(ALICE.getName().fullName));
                assertEquals(1, model.getFilteredClientList().size());
                Client completed = model.getFilteredClientList().getFirst();
                assertEquals(4, completed.getHistory().size());
                assertTrue(completed.getAppointments().isEmpty());
                Client reloaded = storage.readPawPals().orElseThrow().getClientList().get(1);
                assertEquals(completed, reloaded);
                assertTrue(reloaded.getHistory().contains(HistoryFixtures.OLDEST));
                enter(window, "editp c/1 pn/Milo n/Milo Junior");
                assertEquals("Milo Junior", model.getFilteredClientList().getFirst().getPets().getFirst()
                        .getName().toString());
                assertTrue(labels(window).contains(date + " 14:00–15:30 · Milo"));
                enter(window, "delp c/1 pn/Milo Junior");
                assertTrue(model.getFilteredClientList().getFirst().getPets().isEmpty());
                assertTrue(labels(window).contains(date + " 14:00–15:30 · Milo"));
            } finally {
                window.getPrimaryStage().close();
            }
            return null;
        });
    }

    private static void enter(MainWindow window, String command) {
        TextField input = (TextField) window.getPrimaryStage().getScene().lookup("#commandTextField");
        input.setText(command);
        input.fireEvent(new ActionEvent());
        Parent root = window.getPrimaryStage().getScene().getRoot();
        root.resize(900, 900);
        root.applyCss();
        root.layout();
    }

    private static List<String> labels(MainWindow window) {
        return labels(window.getClientListPanel().getRoot());
    }

    private static List<String> labels(Node node) {
        List<String> texts = new ArrayList<>();
        if (node instanceof Label label) {
            texts.add(label.getText());
        }
        if (node instanceof Parent parent) {
            parent.getChildrenUnmodifiable().forEach(child -> texts.addAll(labels(child)));
        }
        return texts;
    }
}
