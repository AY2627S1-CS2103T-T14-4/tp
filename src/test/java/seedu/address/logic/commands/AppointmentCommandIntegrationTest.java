package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static seedu.address.testutil.TypicalClients.ALICE;
import static seedu.address.testutil.TypicalClients.BENSON;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.LogicManager;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.PawPals;
import seedu.address.model.UserPrefs;
import seedu.address.model.appointment.Appointment;
import seedu.address.model.appointment.AppointmentDate;
import seedu.address.model.appointment.AppointmentTime;
import seedu.address.model.client.Client;
import seedu.address.model.pet.Breed;
import seedu.address.model.pet.NoteType;
import seedu.address.model.pet.Pet;
import seedu.address.model.pet.PetName;
import seedu.address.model.pet.PetNote;
import seedu.address.model.pet.Species;
import seedu.address.storage.JsonPawPalsStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;

/**
 * Tests appointment input through parsing, command execution and automatic JSON persistence.
 */
public class AppointmentCommandIntegrationTest {
    private static final String FUTURE_DATE = LocalDate.now().plusDays(1)
            .format(DateTimeFormatter.ofPattern("dd-MM-uuuu"));

    @TempDir
    public Path testFolder;

    private Model model;
    private LogicManager logic;
    private Client selectedClient;

    @BeforeEach
    public void setUp() {
        Pet pet = new Pet(new PetName("Milo"), new Species("Dog"), new Breed("Poodle"))
                .withNote(NoteType.SENS, new PetNote("Sensitive paws"));
        Appointment existing = new Appointment(new AppointmentDate("29-02-2024"),
                new AppointmentTime("09:00"), "Previous visit");
        selectedClient = BENSON.withPet(pet).copyWithAppointment(existing);
        PawPals pawPals = new PawPals();
        pawPals.addClient(ALICE);
        pawPals.addClient(selectedClient);
        model = new ModelManager(pawPals, new UserPrefs());
        logic = new LogicManager(model, createStorage());
    }

    @Test
    public void execute_afterFind_savesEndTimeAndPreservesOtherDataAfterReload() throws Exception {
        logic.execute("find Benson");
        Appointment appointment = new Appointment(new AppointmentDate(FUTURE_DATE), new AppointmentTime("12:00"))
                .withEndTime(new AppointmentTime("13:30"));

        CommandResult result = logic.execute("appt c/1 d/" + FUTURE_DATE + " t/12:00 et/13:30");

        Client expected = selectedClient.copyWithAppointment(appointment);
        assertEquals("Added appointment: " + selectedClient.getName()
                + " d/" + FUTURE_DATE + " t/12:00 et/13:30", result.getFeedbackToUser());
        assertEquals(List.of(expected), model.getFilteredClientList());
        Model restartedModel = new ModelManager(createStorage().readPawPals().orElseThrow(), new UserPrefs());
        assertEquals(List.of(ALICE, expected), restartedModel.getPawPals().getClientList());
        assertEquals(appointment, restartedModel.getPawPals().getClientList().get(1)
                .getAppointment(appointment.getDate(), appointment.getTime()).orElseThrow());
    }

    @Test
    public void execute_withoutEndTime_preservesExistingFeedbackAndReloadsWithoutEndTime() throws Exception {
        Appointment appointment = new Appointment(new AppointmentDate(FUTURE_DATE), new AppointmentTime("12:00"));

        CommandResult result = logic.execute("appt c/2 d/" + FUTURE_DATE + " t/12:00");

        assertEquals("Added appointment: " + selectedClient.getName()
                + " d/" + FUTURE_DATE + " t/12:00", result.getFeedbackToUser());
        assertEquals(List.of(ALICE, selectedClient.copyWithAppointment(appointment)),
                createStorage().readPawPals().orElseThrow().getClientList());
    }

    @Test
    public void execute_invalidEndTimes_leavesMemoryAndSavedDataUnchanged() throws Exception {
        logic.execute("find Benson");
        String saved = Files.readString(testFolder.resolve("pawPals.json"));
        for (String endTime : new String[] {"", "24:00", "12:00", "11:59", "01:00", "13:00 et/14:00"}) {
            String command = "appt c/1 d/" + FUTURE_DATE + " t/12:00 et/" + endTime;
            assertThrows(ParseException.class, () -> logic.execute(command));
            assertEquals(List.of(ALICE, selectedClient), model.getPawPals().getClientList());
            assertEquals(List.of(selectedClient), model.getFilteredClientList());
            assertEquals(saved, Files.readString(testFolder.resolve("pawPals.json")));
        }
    }

    @Test
    public void execute_invalidDisplayedIndexWithEndTime_leavesMemoryAndSavedDataUnchanged() throws Exception {
        logic.execute("find Benson");
        String saved = Files.readString(testFolder.resolve("pawPals.json"));
        String command = "appt c/2 d/" + FUTURE_DATE + " t/12:00 et/13:30";

        CommandException exception = assertThrows(CommandException.class, () -> logic.execute(command));

        assertEquals(Messages.MESSAGE_INVALID_CLIENT_DISPLAYED_INDEX, exception.getMessage());
        assertEquals(List.of(ALICE, selectedClient), model.getPawPals().getClientList());
        assertEquals(List.of(selectedClient), model.getFilteredClientList());
        assertEquals(saved, Files.readString(testFolder.resolve("pawPals.json")));
    }

    private StorageManager createStorage() {
        return new StorageManager(new JsonPawPalsStorage(testFolder.resolve("pawPals.json")),
                new JsonUserPrefsStorage(testFolder.resolve("userPrefs.json")));
    }
}
