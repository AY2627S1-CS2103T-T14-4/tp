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
import seedu.address.model.appointment.Appointment;
import seedu.address.model.appointment.AppointmentDate;
import seedu.address.model.appointment.AppointmentTime;
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
    public void execute_sameSlotForDifferentClients_savesBoth() throws Exception {
        logic.execute(appointmentCommand(1));
        logic.execute(appointmentCommand(2));

        ReadOnlyPawPals saved = readSavedPawPals();
        assertEquals(List.of(appointment), saved.getClientList().get(0).getAppointments());
        assertEquals(List.of(appointment), saved.getClientList().get(1).getAppointments());
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
        for (String input : new String[] {appointmentCommand(3), "appt c/1 d/01-01-2000 t/12:34"}) {
            assertThrows(CommandException.class, () -> logic.execute(input));
            assertEquals(original, model.getPawPals());
            assertEquals(savedJson, Files.readString(dataFile));
        }
    }

    @Test
    public void execute_duplicateAppointment_preservesModelAndSavedFile() throws Exception {
        logic.execute(appointmentCommand(1));
        ReadOnlyPawPals original = new PawPals(model.getPawPals());
        String savedJson = Files.readString(dataFile);

        CommandException exception = assertThrows(CommandException.class, () -> logic.execute(appointmentCommand(1)));

        assertEquals(AppointmentCommand.MESSAGE_DUPLICATE_APPOINTMENT, exception.getMessage());
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
        logic.execute("edit 1 p/93456789");

        ReadOnlyPawPals saved = readSavedPawPals();
        assertEquals("93456789", saved.getClientList().get(0).getPhone().value);
        assertEquals(List.of(appointment), saved.getClientList().get(0).getAppointments());
    }

    @Test
    public void execute_deleteAndClear_removeSavedAppointments() throws Exception {
        logic.execute(appointmentCommand(1));
        logic.execute(appointmentCommand(2));
        logic.execute("delc 1");

        ReadOnlyPawPals afterDelete = readSavedPawPals();
        assertEquals(1, afterDelete.getClientList().size());
        assertEquals("Bob Test", afterDelete.getClientList().get(0).getName().fullName);
        assertEquals(List.of(appointment), afterDelete.getClientList().get(0).getAppointments());

        logic.execute("clear");
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
