package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.commands.AppointmentCommand;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.PawPals;
import seedu.address.model.ReadOnlyPawPals;
import seedu.address.model.UserPrefs;
import seedu.address.model.appointment.Appointment;
import seedu.address.model.appointment.AppointmentDate;
import seedu.address.model.appointment.AppointmentTime;
import seedu.address.model.history.CompletedSession;
import seedu.address.model.history.SessionNotes;
import seedu.address.model.pet.PetName;
import seedu.address.storage.JsonPawPalsStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;

/**
 * Tests appointment commands through dispatch, model updates, automatic saving, and loading from disk.
 */
public class AppointmentIntegrationTest {
    @TempDir
    public Path temporaryFolder;

    private final Model model = new ModelManager();
    private final String futureDate = LocalDate.now().plusYears(1).format(DateTimeFormatter.ofPattern("dd-MM-uuuu"));
    private final Appointment appointment =
            new Appointment(new AppointmentDate(futureDate), new AppointmentTime("12:34"));
    private Path dataFile;
    private Logic logic;

    @BeforeEach
    public void setUp() throws Exception {
        dataFile = temporaryFolder.resolve("pawPals.json");
        StorageManager storage = new StorageManager(new JsonPawPalsStorage(dataFile),
                new JsonUserPrefsStorage(temporaryFolder.resolve("userPrefs.json")));
        logic = new LogicManager(model, storage);
        logic.execute("addc n/Alice Test p/91234567 e/alice@example.com a/10 Dover Road");
        logic.execute("addc n/Bob Test p/92345678 e/bob@example.com a/20 Dover Road");
    }

    @Test
    public void execute_validAppointment_savesUnderSelectedClient() throws Exception {
        String feedback = logic.execute(appointmentCommand(1)).getFeedbackToUser();

        assertEquals("Added appointment: Alice Test d/" + futureDate + " t/12:34", feedback);
        ReadOnlyPawPals saved = readSavedPawPals();
        assertEquals(List.of(appointment), saved.getClientList().get(0).getAppointments());
        assertTrue(saved.getClientList().get(1).getAppointments().isEmpty());
        assertEquals(model.getPawPals(), saved);
    }

    @Test
    public void execute_sameSlotForDifferentClients_rejectsAndPreservesSavedData() throws Exception {
        logic.execute(appointmentCommand(1));
        ReadOnlyPawPals original = new PawPals(model.getPawPals());
        String savedJson = Files.readString(dataFile);

        CommandException exception = assertThrows(CommandException.class, () -> logic.execute(appointmentCommand(2)));

        ReadOnlyPawPals saved = readSavedPawPals();
        assertEquals("Appointment overlaps with Alice Test's appointment: d/" + futureDate + " t/12:34.",
                exception.getMessage());
        assertEquals(List.of(appointment), saved.getClientList().get(0).getAppointments());
        assertTrue(saved.getClientList().get(1).getAppointments().isEmpty());
        assertEquals(original, model.getPawPals());
        assertEquals(original, saved);
        assertEquals(savedJson, Files.readString(dataFile));
    }

    @Test
    public void execute_invalidAppointments_preservesModelAndSavedFile() throws Exception {
        ReadOnlyPawPals original = new PawPals(model.getPawPals());
        String savedJson = Files.readString(dataFile);
        for (String input : new String[] {"appt c/1 d/" + futureDate,
            "appt c/0 d/" + futureDate + " t/12:34",
            "appt c/1 d/31-02-2028 t/12:34",
            "appt c/1 d/" + futureDate + " t/24:00",
            appointmentCommand(1) + " c/1",
            appointmentCommand(1) + " d/" + futureDate,
            appointmentCommand(1) + " t/12:34"}) {
            assertThrows(ParseException.class, () -> logic.execute(input));
            assertEquals(original, model.getPawPals());
            assertEquals(savedJson, Files.readString(dataFile));
        }
        assertThrows(CommandException.class, () -> logic.execute(appointmentCommand(3)));
        assertEquals(original, model.getPawPals());
        assertEquals(savedJson, Files.readString(dataFile));
    }

    @Test
    public void execute_pastAppointment_savesAndCanBeCompletedAfterReload() throws Exception {
        String pastDate = LocalDate.now().minusDays(1).format(DateTimeFormatter.ofPattern("dd-MM-uuuu"));
        Appointment past = new Appointment(new AppointmentDate(pastDate), new AppointmentTime("14:00"))
                .withEndTime(new AppointmentTime("15:00")).withPetNames(Set.of(new PetName("Milo")));
        logic.execute("addp c/1 n/Milo s/Dog b/Poodle");
        logic.execute("find Alice");
        ReadOnlyPawPals original = new PawPals(model.getPawPals());

        String feedback = logic.execute("appt c/1 d/" + pastDate + " t/14:00 et/15:00 pn/milo").getFeedbackToUser();

        assertEquals("Added appointment: Alice Test d/" + pastDate + " t/14:00 et/15:00 pn/Milo\n"
                + AppointmentCommand.MESSAGE_PAST_APPOINTMENT, feedback);
        ReadOnlyPawPals saved = readSavedPawPals();
        assertEquals(List.of(past), saved.getClientList().getFirst().getAppointments());
        assertTrue(saved.getClientList().getFirst().getHistory().isEmpty());
        assertEquals(original.getClientList().get(1), saved.getClientList().get(1));
        assertEquals(List.of(saved.getClientList().getFirst()), model.getFilteredClientList());
        assertEquals(model.getPawPals(), saved);

        Model reloaded = new ModelManager(saved, new UserPrefs());
        Logic reloadedLogic = new LogicManager(reloaded, new StorageManager(new JsonPawPalsStorage(dataFile),
                new JsonUserPrefsStorage(temporaryFolder.resolve("userPrefs.json"))));
        reloadedLogic.execute("done c/1 d/" + pastDate + " t/14:00 nt/Teddy cut");

        ReadOnlyPawPals completed = readSavedPawPals();
        assertTrue(completed.getClientList().getFirst().getAppointments().isEmpty());
        assertEquals(1, completed.getClientList().getFirst().getHistory().size());
        CompletedSession session = completed.getClientList().getFirst().getHistory().getFirst();
        assertEquals(past, session.getAppointment());
        assertEquals(new SessionNotes("Teddy cut"), session.getNotes());
        assertEquals(saved.getClientList().getFirst().getPets(), completed.getClientList().getFirst().getPets());
        assertEquals(original.getClientList().get(1), completed.getClientList().get(1));
        assertEquals(reloaded.getPawPals(), completed);
    }

    @Test
    public void execute_pastAppointmentOverlap_preservesModelFilterAndSavedFile() throws Exception {
        String pastDate = LocalDate.now().minusDays(1).format(DateTimeFormatter.ofPattern("dd-MM-uuuu"));
        logic.execute("appt c/1 d/" + pastDate + " t/14:00 et/15:00");
        logic.execute("find Bob");
        ReadOnlyPawPals original = new PawPals(model.getPawPals());
        String savedJson = Files.readString(dataFile);

        CommandException exception = assertThrows(
                CommandException.class, () -> logic.execute("appt c/1 d/" + pastDate + " t/14:30 et/15:30"));

        assertEquals("Appointment overlaps with Alice Test's appointment: d/" + pastDate + " t/14:00 et/15:00.",
                exception.getMessage());
        assertEquals(original, model.getPawPals());
        assertEquals(List.of(original.getClientList().get(1)), model.getFilteredClientList());
        assertEquals(savedJson, Files.readString(dataFile));
    }

    @Test
    public void execute_duplicateAppointment_preservesModelAndSavedFile() throws Exception {
        logic.execute(appointmentCommand(1));
        ReadOnlyPawPals original = new PawPals(model.getPawPals());
        String savedJson = Files.readString(dataFile);

        CommandException exception = assertThrows(CommandException.class, () -> logic.execute(appointmentCommand(1)));

        assertEquals("Appointment overlaps with Alice Test's appointment: d/" + futureDate + " t/12:34.",
                exception.getMessage());
        assertEquals(original, model.getPawPals());
        assertEquals(savedJson, Files.readString(dataFile));
    }

    @Test
    public void execute_filteredAppointment_savesDisplayedClientAndKeepsFilter() throws Exception {
        logic.execute("find Bob");
        logic.execute(appointmentCommand(1));

        assertEquals(1, logic.getFilteredClientList().size());
        assertEquals("Bob Test", logic.getFilteredClientList().get(0).getName().fullName);
        ReadOnlyPawPals saved = readSavedPawPals();
        assertTrue(saved.getClientList().get(0).getAppointments().isEmpty());
        assertEquals(List.of(appointment), saved.getClientList().get(1).getAppointments());
        String savedJson = Files.readString(dataFile);
        assertThrows(CommandException.class, () -> logic.execute(appointmentCommand(2)));
        assertEquals(savedJson, Files.readString(dataFile));
        assertEquals(saved, model.getPawPals());
    }

    @Test
    public void execute_editClient_preservesSavedAppointments() throws Exception {
        logic.execute(appointmentCommand(1));
        // Equivalence Partition: client edit with c/ preserves saved appointments.
        logic.execute("edit c/1 p/93456789");

        ReadOnlyPawPals saved = readSavedPawPals();
        assertEquals("93456789", saved.getClientList().get(0).getPhone().value);
        assertEquals(List.of(appointment), saved.getClientList().get(0).getAppointments());
    }

    @Test
    public void execute_deleteAndClear_removeSavedAppointments() throws Exception {
        logic.execute(appointmentCommand(1));
        logic.execute("appt c/2 d/" + futureDate + " t/12:35");
        logic.execute("delc 1");

        ReadOnlyPawPals afterDelete = readSavedPawPals();
        assertEquals(1, afterDelete.getClientList().size());
        assertEquals("Bob Test", afterDelete.getClientList().get(0).getName().fullName);
        assertEquals(List.of(new Appointment(appointment.getDate(), new AppointmentTime("12:35"))),
                afterDelete.getClientList().get(0).getAppointments());

        logic.execute("clear");
        // Requesting clear preserves saved appointments until confirmation.
        assertEquals(afterDelete, readSavedPawPals());
        logic.execute("y");
        assertTrue(readSavedPawPals().getClientList().isEmpty());
        assertTrue(model.getPawPals().getClientList().isEmpty());
    }

    @Test
    public void execute_saveFailure_reportsErrorAndRetainsAppointmentInMemory() throws Exception {
        String savedJson = Files.readString(dataFile);
        JsonPawPalsStorage failingStorage = new JsonPawPalsStorage(dataFile) {
            @Override
            public void savePawPals(ReadOnlyPawPals pawPals) throws IOException {
                throw new IOException("Simulated save failure");
            }
        };
        Logic failingLogic = new LogicManager(model, new StorageManager(failingStorage,
                new JsonUserPrefsStorage(temporaryFolder.resolve("userPrefs.json"))));

        CommandException exception = assertThrows(
                CommandException.class, () -> failingLogic.execute(appointmentCommand(1)));

        assertEquals(String.format(LogicManager.FILE_OPS_ERROR_FORMAT, "Simulated save failure"),
                exception.getMessage());
        assertEquals(List.of(appointment), model.getPawPals().getClientList().get(0).getAppointments());
        assertEquals(savedJson, Files.readString(dataFile));
    }

    private String appointmentCommand(int displayedIndex) {
        return "appt c/" + displayedIndex + " d/" + futureDate + " t/12:34";
    }

    private ReadOnlyPawPals readSavedPawPals() throws Exception {
        return new JsonPawPalsStorage(dataFile).readPawPals().orElseThrow();
    }
}
