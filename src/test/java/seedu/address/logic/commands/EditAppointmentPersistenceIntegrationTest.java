package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static seedu.address.testutil.TypicalClients.ALICE;
import static seedu.address.testutil.TypicalClients.BENSON;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;

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
import seedu.address.model.history.CompletedSession;
import seedu.address.model.history.SessionNotes;
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
 * Tests rescheduling through parsing, execution, filtering, automatic saving and reloading.
 */
public class EditAppointmentPersistenceIntegrationTest {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-uuuu");
    private static final Pet MILO = new Pet(new PetName("Milo"), new Species("Dog"), new Breed("Poodle"))
            .withNote(NoteType.SENS, new PetNote("Sensitive paws"));
    private static final Pet LUNA = new Pet(new PetName("Luna"), new Species("Cat"), new Breed("Persian"));

    @TempDir
    public Path testFolder;

    private String originalDate;
    private String newDate;
    private Appointment original;
    private Appointment otherAppointment;
    private Client hiddenClient;
    private Client selectedClient;
    private Model model;
    private LogicManager logic;

    @BeforeEach
    public void setUp() throws Exception {
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        originalDate = tomorrow.format(DATE_FORMATTER);
        newDate = tomorrow.plusDays(1).format(DATE_FORMATTER);
        original = booking(originalDate, "14:00", "15:30");
        otherAppointment = booking(newDate, "09:00", null);
        CompletedSession history = new CompletedSession(booking("29-02-2024", "09:00", "10:00"),
                new SessionNotes("Teddy cut"), Instant.parse("2024-02-29T02:00:00Z"));
        hiddenClient = new ClientBuilder(ALICE).withPets(MILO, LUNA)
                .withAppointments(booking(originalDate, "16:00", "17:00")).build();
        selectedClient = new ClientBuilder(BENSON).withTags("regular").withPets(MILO, LUNA)
                .withAppointments(otherAppointment, original).withHistory(history).build();
        PawPals data = new PawPals();
        data.addClient(hiddenClient);
        data.addClient(selectedClient);
        model = new ModelManager(data, new UserPrefs());
        logic = new LogicManager(model, createStorage());
        logic.execute("find " + selectedClient.getName());
    }

    @Test
    public void execute_filteredRescheduleAndReload_preservesDurationAndAllUneditedData() throws Exception {
        CommandResult result = logic.execute(target() + " nd/" + newDate + " ns/10:00");

        Appointment expected = booking(newDate, "10:00", "11:30");
        assertPersistedReplacement(expected);
        assertEquals("Rescheduled appointment: " + selectedClient.getName() + " " + originalDate + " 14:00 → " + newDate
                + " 10:00 et/11:30", result.getFeedbackToUser());
        assertEquals(List.of(MILO.getName(), LUNA.getName()), List.copyOf(reloadSelected().getAppointments()
                .get(1).getPetNames()));

        restart();
        logic.execute("find " + selectedClient.getName());
        assertThrows(CommandException.class, () -> logic.execute(target() + " ns/18:00"));
        assertPersistedReplacement(expected);
    }

    @Test
    public void execute_endOnlyChange_isReportedAndPersisted() throws Exception {
        CommandResult result = logic.execute(target() + " ne/15:45");

        assertEquals("Rescheduled appointment: " + selectedClient.getName() + " " + originalDate
                + " 14:00 → " + originalDate + " 14:00 et/15:45", result.getFeedbackToUser());
        assertPersistedReplacement(booking(originalDate, "14:00", "15:45"));
    }

    @Test
    public void execute_startOnlyBookingAndReload_keepsMissingEndThenAllowsAddingOne() throws Exception {
        Appointment startOnly = booking(originalDate, "14:00", null);
        Client updated = new ClientBuilder(selectedClient).withAppointments(otherAppointment, startOnly).build();
        model.setClient(selectedClient, updated);
        selectedClient = updated;

        logic.execute(target() + " ns/18:00");
        assertPersistedReplacement(booking(originalDate, "18:00", null));
        restart();
        logic.execute("find " + selectedClient.getName());
        logic.execute("editappt c/1 d/" + originalDate + " t/18:00 ne/19:00");
        assertPersistedReplacement(booking(originalDate, "18:00", "19:00"));
    }

    @Test
    public void execute_invalidSyntax_leavesMemoryFilterAndSavedFileUnchanged() throws Exception {
        String saved = Files.readString(dataFile());
        for (String input : new String[] {"editappt", target(), target() + " ns/24:00",
            target() + " nd/", target() + " ns/18:00 ns/19:00", target() + " pn/" + MILO.getName()}) {
            assertThrows(ParseException.class, () -> logic.execute(input));
            assertUnchanged(saved);
        }
    }

    @Test
    public void execute_invalidTargetSlotOrHiddenConflict_leavesMemoryFilterAndSavedFileUnchanged() throws Exception {
        String saved = Files.readString(dataFile());
        for (String input : new String[] {"editappt c/2 d/" + originalDate + " t/14:00 ns/18:00",
            "editappt c/1 d/" + originalDate + " t/13:00 ns/18:00", target() + " nd/01-01-2000",
            target() + " ns/23:00", target() + " ne/14:00", target() + " ne/13:59",
            target() + " ns/16:00", target() + " ne/16:30"}) {
            assertThrows(CommandException.class, () -> logic.execute(input));
            assertUnchanged(saved);
        }
    }

    @Test
    public void execute_rescheduleAndReload_freesOriginalSlotForAnotherClient() throws Exception {
        logic.execute(target() + " ns/18:00");
        restart();
        logic.execute("list");

        logic.execute("appt c/1 d/" + originalDate + " t/14:00 et/15:30");

        Appointment rebooked = new Appointment(original.getDate(), original.getTime())
                .withEndTime(new AppointmentTime("15:30"));
        assertEquals(hiddenClient.copyWithAppointment(rebooked),
                createStorage().readPawPals().orElseThrow().getClientList().getFirst());
        assertEquals(new ClientBuilder(selectedClient).withAppointments(otherAppointment,
                booking(originalDate, "18:00", "19:30")).build(), reloadSelected());
    }

    private void assertPersistedReplacement(Appointment expected) throws Exception {
        Client expectedClient = new ClientBuilder(selectedClient).withAppointments(otherAppointment, expected).build();
        assertEquals(List.of(expectedClient), model.getFilteredClientList());
        assertEquals(List.of(hiddenClient, expectedClient), model.getPawPals().getClientList());
        assertEquals(List.of(hiddenClient, expectedClient),
                createStorage().readPawPals().orElseThrow().getClientList());
    }

    private void assertUnchanged(String saved) throws Exception {
        assertEquals(List.of(selectedClient), model.getFilteredClientList());
        assertEquals(List.of(hiddenClient, selectedClient), model.getPawPals().getClientList());
        assertEquals(saved, Files.readString(dataFile()));
        assertEquals(List.of(hiddenClient, selectedClient),
                createStorage().readPawPals().orElseThrow().getClientList());
    }

    private Client reloadSelected() throws Exception {
        return createStorage().readPawPals().orElseThrow().getClientList().get(1);
    }

    private void restart() throws Exception {
        StorageManager storage = createStorage();
        model = new ModelManager(storage.readPawPals().orElseThrow(), new UserPrefs());
        logic = new LogicManager(model, storage);
    }

    private String target() {
        return "editappt c/1 d/" + originalDate + " t/14:00";
    }

    private Path dataFile() {
        return testFolder.resolve("pawPals.json");
    }

    private StorageManager createStorage() {
        return new StorageManager(new JsonPawPalsStorage(dataFile()),
                new JsonUserPrefsStorage(testFolder.resolve("userPrefs.json")));
    }

    private static Appointment booking(String date, String start, String end) {
        return new Appointment(new AppointmentDate(date), new AppointmentTime(start),
                Optional.ofNullable(end).map(AppointmentTime::new), "Call on arrival",
                new LinkedHashSet<>(List.of(MILO.getName(), LUNA.getName())));
    }
}
