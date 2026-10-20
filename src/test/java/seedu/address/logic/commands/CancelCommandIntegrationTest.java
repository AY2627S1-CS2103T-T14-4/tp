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
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.LogicManager;
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
import seedu.address.testutil.ClientBuilder;

/**
 * Tests cancellation through command dispatch, filtering, automatic saving and reloading.
 */
public class CancelCommandIntegrationTest {
    private static final String FUTURE_DATE = LocalDate.now().plusDays(1)
            .format(DateTimeFormatter.ofPattern("dd-MM-uuuu"));
    private static final Appointment FUTURE = new Appointment(new AppointmentDate(FUTURE_DATE),
            new AppointmentTime("14:00"), "Full groom", Set.of(new PetName("Milo")))
            .withEndTime(new AppointmentTime("15:30"));
    private static final Appointment PAST = new Appointment(new AppointmentDate("29-02-2024"),
            new AppointmentTime("09:00"), "Previous visit", Set.of(new PetName("Milo")))
            .withEndTime(new AppointmentTime("10:00"));

    @TempDir
    public Path testFolder;

    private Model model;
    private LogicManager logic;
    private Client alice;
    private Client benson;

    @BeforeEach
    public void setUp() {
        Pet pet = new Pet(new PetName("Milo"), new Species("Dog"), new Breed("Poodle"))
                .withNote(NoteType.SENS, new PetNote("Sensitive paws"));
        alice = new ClientBuilder(ALICE).withTags("regular").withPets(pet).withAppointments(FUTURE).build();
        benson = new ClientBuilder(BENSON).withPets(pet).withAppointments(PAST).build();
        PawPals pawPals = new PawPals();
        pawPals.addClient(alice);
        pawPals.addClient(benson);
        model = new ModelManager(pawPals, new UserPrefs());
        logic = new LogicManager(model, createStorage());
    }

    @Test
    public void execute_afterFindAndReload_cancelsPastAppointmentAndPreservesOtherData() throws Exception {
        logic.execute("list");
        StorageManager restartedStorage = createStorage();
        model = new ModelManager(restartedStorage.readPawPals().orElseThrow(), new UserPrefs());
        logic = new LogicManager(model, restartedStorage);
        logic.execute("find Benson");

        CommandResult result = logic.execute("cancel c/1 d/29-02-2024 t/09:00");

        Client expected = new ClientBuilder(benson).withAppointments().build();
        assertEquals("Cancelled appointment: Benson Meier d/29-02-2024 t/09:00", result.getFeedbackToUser());
        assertEquals(List.of(expected), model.getFilteredClientList());
        assertEquals(List.of(alice, expected), model.getPawPals().getClientList());
        Model restored = new ModelManager(createStorage().readPawPals().orElseThrow(), new UserPrefs());
        assertEquals(List.of(alice, expected), restored.getPawPals().getClientList());

        LogicManager restoredLogic = new LogicManager(restored, createStorage());
        assertThrows(CommandException.class, () -> restoredLogic.execute("cancel c/2 d/29-02-2024 t/09:00"));
        assertEquals(List.of(alice, expected), createStorage().readPawPals().orElseThrow().getClientList());
    }

    @Test
    public void execute_cancelFutureAppointment_allowsAnotherClientToBookSlotAfterReload() throws Exception {
        logic.execute("cancel c/1 d/" + FUTURE_DATE + " t/14:00");
        Client expectedAlice = new ClientBuilder(alice).withAppointments().build();
        assertEquals(List.of(expectedAlice, benson), createStorage().readPawPals().orElseThrow().getClientList());

        StorageManager restartedStorage = createStorage();
        Model restored = new ModelManager(restartedStorage.readPawPals().orElseThrow(), new UserPrefs());
        LogicManager restoredLogic = new LogicManager(restored, restartedStorage);
        restoredLogic.execute("find Benson");
        restoredLogic.execute("appt c/1 d/" + FUTURE_DATE + " t/14:00");

        Appointment rebooked = new Appointment(FUTURE.getDate(), FUTURE.getTime());
        Client expectedBenson = new ClientBuilder(benson).withAppointments(PAST, rebooked).build();
        assertEquals(List.of(expectedBenson), restored.getFilteredClientList());
        assertEquals(List.of(expectedAlice, expectedBenson),
                createStorage().readPawPals().orElseThrow().getClientList());
    }

    @Test
    public void execute_invalidInput_leavesMemoryFilterAndSavedDataUnchanged() throws Exception {
        logic.execute("find Benson");
        String saved = Files.readString(testFolder.resolve("pawPals.json"));
        String[] invalidCommands = {
            "cancel", "cancel c/1 d/29-02-2024", "cancel extra c/1 d/29-02-2024 t/09:00",
            "cancel c/1 c/1 d/29-02-2024 t/09:00", "cancel c/1 d/29-02-2024 d/29-02-2024 t/09:00",
            "cancel c/1 d/29-02-2024 t/09:00 t/09:00", "cancel c/0 d/29-02-2024 t/09:00",
            "cancel c/1 d/31-02-2024 t/09:00", "cancel c/1 d/29-02-2024 t/24:00"
        };
        for (String command : invalidCommands) {
            assertThrows(ParseException.class, () -> logic.execute(command));
            assertDataUnchanged(saved);
        }
    }

    @Test
    public void execute_invalidIndexOrMissingAppointment_leavesMemoryFilterAndSavedDataUnchanged() throws Exception {
        logic.execute("find Benson");
        String saved = Files.readString(testFolder.resolve("pawPals.json"));
        for (String command : new String[] {"cancel c/2 d/29-02-2024 t/09:00",
            "cancel c/1 d/29-02-2024 t/10:00", "cancel c/1 d/" + FUTURE_DATE + " t/14:00"}) {
            assertThrows(CommandException.class, () -> logic.execute(command));
            assertDataUnchanged(saved);
        }
    }

    private void assertDataUnchanged(String saved) throws Exception {
        assertEquals(List.of(alice, benson), model.getPawPals().getClientList());
        assertEquals(List.of(benson), model.getFilteredClientList());
        assertEquals(saved, Files.readString(testFolder.resolve("pawPals.json")));
        assertEquals(List.of(alice, benson), createStorage().readPawPals().orElseThrow().getClientList());
    }

    private StorageManager createStorage() {
        return new StorageManager(new JsonPawPalsStorage(testFolder.resolve("pawPals.json")),
                new JsonUserPrefsStorage(testFolder.resolve("userPrefs.json")));
    }
}
