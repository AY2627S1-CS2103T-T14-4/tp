package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.TypicalClients.ALICE;
import static seedu.address.testutil.TypicalClients.BENSON;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_CLIENT;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_CLIENT;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

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
import seedu.address.model.ReadOnlyPawPals;
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

public class DoneCommandIntegrationTest {
    private static final Pet MILO = new Pet(new PetName("Milo"), new Species("Dog"), new Breed("Poodle"))
            .withNote(NoteType.SENS, new PetNote("Sensitive paws"));
    private static final Pet LUNA = new Pet(new PetName("Luna"), new Species("Cat"), new Breed("Persian"));
    private static final Appointment TARGET = new Appointment(new AppointmentDate("29-02-2024"),
            new AppointmentTime("14:00"), "Call on arrival",
            new LinkedHashSet<>(List.of(MILO.getName(), LUNA.getName())))
            .withEndTime(new AppointmentTime("15:30"));
    private static final Appointment OTHER = new Appointment(new AppointmentDate("01-03-2024"),
            new AppointmentTime("16:00"));
    private static final Appointment FUTURE = new Appointment(new AppointmentDate("31-12-9999"),
            new AppointmentTime("23:59"));
    private static final CompletedSession OLD_SESSION = new CompletedSession(new Appointment(
            new AppointmentDate("10-02-2024"), new AppointmentTime("14:00"), "Old visit",
            Set.of(new PetName("Historical pet"))), new SessionNotes("Previous visit"),
            Instant.parse("2024-02-10T08:00:00Z"));
    private static final Client SELECTED = new ClientBuilder(BENSON).withPets(MILO, LUNA)
            .withAppointments(TARGET, OTHER, FUTURE).withHistory(OLD_SESSION).build();

    @TempDir
    public Path testFolder;

    private Model model;
    private LogicManager logic;

    @BeforeEach
    public void setUp() {
        PawPals data = new PawPals();
        data.addClient(ALICE);
        data.addClient(SELECTED);
        model = new ModelManager(data, new UserPrefs());
        logic = new LogicManager(model, createStorage());
    }

    @Test
    public void execute_afterReloadAndFind_savesFullSnapshotAndPreservesAllOtherData() throws Exception {
        logic.execute("list");
        restart();
        logic.execute("find Benson");
        Instant before = Instant.now();

        CommandResult result = logic.execute("done nt/  Teddy cut, short ears  c/1 t/14:00 d/29-02-2024");

        Instant after = Instant.now();
        Client completed = model.getFilteredClientList().getFirst();
        CompletedSession session = completed.getHistory().getFirst();
        assertEquals(TARGET, session.getAppointment());
        assertEquals(new SessionNotes("Teddy cut, short ears"), session.getNotes());
        assertFalse(session.getLoggedAt().isBefore(before));
        assertFalse(session.getLoggedAt().isAfter(after));
        Client expected = new ClientBuilder(SELECTED).withAppointments(OTHER, FUTURE)
                .withHistory(session, OLD_SESSION).build();
        assertEquals(List.of(expected), model.getFilteredClientList());
        assertEquals(List.of(ALICE, expected), createStorage().readPawPals().orElseThrow().getClientList());
        assertEquals(List.of(MILO.getName(), LUNA.getName()), List.copyOf(createStorage().readPawPals().orElseThrow()
                .getClientList().get(1).getHistory().getFirst().getAppointment().getPetNames()));
        assertEquals("Completed session: Benson Meier d/29-02-2024 t/14:00 et/15:30"
                + " pn/Milo pn/Luna nt/Teddy cut, short ears", result.getFeedbackToUser());
    }

    @Test
    public void execute_clientOnlyAppointment_keepsEmptyPetLinksAndNoEndTimeAfterReload() throws Exception {
        CommandResult result = logic.execute("done c/2 d/01-03-2024 t/16:00 nt/x");
        Client restored = createStorage().readPawPals().orElseThrow().getClientList().get(1);
        assertEquals(List.of(TARGET, FUTURE), restored.getAppointments());
        assertEquals(OTHER, restored.getHistory().getFirst().getAppointment());
        assertEquals(List.of(MILO, LUNA), restored.getPets());
        assertEquals(new SessionNotes("x"), restored.getHistory().getFirst().getNotes());
        assertEquals("Completed session: Benson Meier d/01-03-2024 t/16:00 nt/x", result.getFeedbackToUser());
    }

    @Test
    public void execute_repeatedCompletionAfterRestart_preservesHistoryAndSavedFile() throws Exception {
        logic.execute("done c/2 d/29-02-2024 t/14:00 nt/Original notes");
        restart();
        logic.execute("find Benson");
        ReadOnlyPawPals original = new PawPals(model.getPawPals());
        String saved = Files.readString(dataFile());
        CommandException exception = assertThrows(CommandException.class, () -> logic.execute(
                "done c/1 d/29-02-2024 t/14:00 nt/Replacement notes"));
        assertEquals("Benson Meier already has a completed session at 29-02-2024 14:00.", exception.getMessage());
        assertUnchanged(original, saved);
    }

    @Test
    public void execute_invalidInputs_preserveMemoryFilterAndSavedFile() throws Exception {
        logic.execute("find Benson");
        ReadOnlyPawPals original = new PawPals(model.getPawPals());
        String saved = Files.readString(dataFile());
        for (String input : new String[] {"done", "done c/1 d/29-02-2024 t/14:00",
            "done extra c/1 d/29-02-2024 t/14:00 nt/Notes", "done c/0 d/29-02-2024 t/14:00 nt/Notes",
            "done c/1 c/1 d/29-02-2024 t/14:00 nt/Notes", "done c/1 d/29-02-2024 t/14:00 nt/Notes nt/Notes",
            "done c/1 d/31-02-2024 t/14:00 nt/Notes", "done c/1 d/29-02-2024 t/24:00 nt/Notes",
            "done c/1 d/29-02-2024 t/14:00 nt/", "done c/1 d/29-02-2024 t/14:00 nt/" + "x".repeat(201)}) {
            assertThrows(ParseException.class, () -> logic.execute(input));
            assertUnchanged(original, saved);
        }
    }

    @Test
    public void execute_invalidIndexFutureOrMissingAppointment_preservesSavedData() throws Exception {
        logic.execute("find Benson");
        ReadOnlyPawPals original = new PawPals(model.getPawPals());
        String saved = Files.readString(dataFile());
        String[] inputs = {"done c/2 d/29-02-2024 t/14:00 nt/Notes",
            "done c/1 d/31-12-9999 t/23:59 nt/Notes", "done c/1 d/29-02-2024 t/15:30 nt/Notes"};
        String[] messages = {Messages.MESSAGE_INVALID_CLIENT_DISPLAYED_INDEX, Client.MESSAGE_APPOINTMENT_NOT_STARTED,
            "Benson Meier has no active appointment at 29-02-2024 15:30."};
        for (int i = 0; i < inputs.length; i++) {
            String input = inputs[i];
            CommandException exception = assertThrows(CommandException.class, () -> logic.execute(input));
            assertEquals(messages[i], exception.getMessage());
            assertUnchanged(original, saved);
        }
    }

    @Test
    public void execute_wrongDisplayedClient_doesNotCompleteAnotherClientsAppointment() throws Exception {
        logic.execute("find Alice");
        ReadOnlyPawPals original = new PawPals(model.getPawPals());
        String saved = Files.readString(dataFile());
        CommandException exception = assertThrows(CommandException.class, () -> logic.execute(
                "done c/1 d/29-02-2024 t/14:00 nt/Notes"));
        assertEquals("Alice Pauline has no active appointment at 29-02-2024 14:00.", exception.getMessage());
        assertEquals(original, model.getPawPals());
        assertEquals(List.of(ALICE), model.getFilteredClientList());
        assertEquals(saved, Files.readString(dataFile()));
    }

    @Test
    public void execute_completedHistoryAfterReload_doesNotReserveRemainingTimeInterval() throws Exception {
        Clock clock = Clock.fixed(Instant.parse("2026-12-12T06:00:00Z"), ZoneId.of("Asia/Singapore"));
        Appointment active = new Appointment(new AppointmentDate("12-12-2026"), new AppointmentTime("14:00"),
                "Booking details", TARGET.getPetNames()).withEndTime(new AppointmentTime("15:30"));
        Client withActive = SELECTED.copyWithAppointment(active);
        model.setClient(SELECTED, withActive);
        Appointment next = new Appointment(active.getDate(), new AppointmentTime("14:15"))
                .withEndTime(new AppointmentTime("15:00"));
        assertThrows(CommandException.class, ()
                -> new AppointmentCommand(INDEX_FIRST_CLIENT, next, clock).execute(model));

        new DoneCommand(INDEX_SECOND_CLIENT, active.getDate(), active.getTime(), new SessionNotes("Visit notes"), clock)
                .execute(model);
        createStorage().savePawPals(model.getPawPals());
        Model restored = new ModelManager(createStorage().readPawPals().orElseThrow(), new UserPrefs());
        assertTrue(restored.findAppointmentConflict(next).isEmpty());
        new AppointmentCommand(INDEX_FIRST_CLIENT, next, clock).execute(restored);
        createStorage().savePawPals(restored.getPawPals());

        ReadOnlyPawPals saved = createStorage().readPawPals().orElseThrow();
        assertEquals(List.of(next), saved.getClientList().getFirst().getAppointments());
        assertEquals(List.of(TARGET, OTHER, FUTURE), saved.getClientList().get(1).getAppointments());
        assertEquals(new CompletedSession(active, new SessionNotes("Visit notes"), clock.instant()),
                saved.getClientList().get(1).getHistory().getFirst());
    }

    @Test
    public void execute_saveFailure_keepsCoherentInMemoryCompletionAndReportsError() throws Exception {
        logic.execute("find Benson");
        String saved = Files.readString(dataFile());
        JsonPawPalsStorage failing = new JsonPawPalsStorage(dataFile()) {
            @Override
            public void savePawPals(ReadOnlyPawPals data) throws IOException {
                throw new IOException("Simulated save failure");
            }
        };
        LogicManager failingLogic = new LogicManager(model,
                new StorageManager(failing, new JsonUserPrefsStorage(testFolder.resolve("userPrefs.json"))));
        CommandException exception = assertThrows(CommandException.class, () -> failingLogic.execute(
                "done c/1 d/29-02-2024 t/14:00 nt/Notes"));
        assertEquals(String.format(LogicManager.FILE_OPS_ERROR_FORMAT, "Simulated save failure"),
                exception.getMessage());
        assertEquals(List.of(OTHER, FUTURE), model.getFilteredClientList().getFirst().getAppointments());
        assertEquals(TARGET, model.getFilteredClientList().getFirst().getHistory().getFirst().getAppointment());
        assertEquals(saved, Files.readString(dataFile()));
        assertEquals(SELECTED, createStorage().readPawPals().orElseThrow().getClientList().get(1));
    }

    private void assertUnchanged(ReadOnlyPawPals original, String saved) throws Exception {
        assertEquals(original, model.getPawPals());
        assertEquals(List.of(original.getClientList().get(1)), model.getFilteredClientList());
        assertEquals(saved, Files.readString(dataFile()));
        assertEquals(original, createStorage().readPawPals().orElseThrow());
    }

    private void restart() throws Exception {
        model = new ModelManager(createStorage().readPawPals().orElseThrow(), new UserPrefs());
        logic = new LogicManager(model, createStorage());
    }

    private Path dataFile() {
        return testFolder.resolve("pawPals.json");
    }

    private StorageManager createStorage() {
        return new StorageManager(new JsonPawPalsStorage(dataFile()),
                new JsonUserPrefsStorage(testFolder.resolve("userPrefs.json")));
    }
}
