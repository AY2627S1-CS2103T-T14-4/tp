package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.TypicalClients.ALICE;
import static seedu.address.testutil.TypicalClients.BENSON;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.commons.exceptions.DataLoadingException;
import seedu.address.commons.util.JsonUtil;
import seedu.address.logic.LogicManager;
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
import seedu.address.testutil.ClientBuilder;

public class CompletedHistoryStorageTest {
    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-10-21T06:30:00Z"),
            ZoneId.of("Asia/Singapore"));
    private static final Pet MILO = new Pet(new PetName("Milo"), new Species("Dog"), new Breed("Poodle"))
            .withNote(NoteType.SENS, new PetNote("Sensitive paws"));
    private static final Appointment APPOINTMENT = new Appointment(new AppointmentDate("20-10-2026"),
            new AppointmentTime("14:00"), "Call on arrival", Set.of(MILO.getName()))
            .withEndTime(new AppointmentTime("15:00"));
    private static final CompletedSession SESSION = new CompletedSession(APPOINTMENT,
            new SessionNotes("Teddy cut, short ears"), CLOCK.instant());

    @TempDir
    public Path testFolder;

    @Test
    public void saveAndReload_completionRetainsSnapshotAndRemovesActiveBooking() throws Exception {
        Client original = new ClientBuilder(ALICE).withPets(MILO).withAppointments(APPOINTMENT).build();
        PawPals pawPals = new PawPals();
        pawPals.addClient(original);
        pawPals.addClient(BENSON);
        Model model = new ModelManager(pawPals, new UserPrefs());

        model.setClient(original, original.completeAppointment(APPOINTMENT.getDate(), APPOINTMENT.getTime(),
                SESSION.getNotes(), CLOCK));
        createStorage().savePawPals(model.getPawPals());

        PawPals restored = new PawPals(createStorage().readPawPals().orElseThrow());
        assertEquals(new ClientBuilder(original).withAppointments().withHistory(SESSION).build(),
                restored.getClientList().getFirst());
        assertEquals(BENSON, restored.getClientList().get(1));
        assertFalse(restored.getClientList().getFirst().hasAppointment(APPOINTMENT));
        assertTrue(restored.getClientList().getFirst().hasCompletedAppointment(
                APPOINTMENT.getDate(), APPOINTMENT.getTime()));
    }

    @Test
    public void saveAndReload_retainsAllRecordsIncludingRepeatedSlotsAndDeletedPets() throws Exception {
        List<CompletedSession> history = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            history.add(new CompletedSession(APPOINTMENT, new SessionNotes("Visit " + i),
                    CLOCK.instant().plusSeconds(i)));
        }
        Client client = new Client(ALICE.getName(), ALICE.getPhone(), ALICE.getEmail(), ALICE.getAddress(),
                ALICE.getTags(), List.of(), List.of(), history);
        PawPals pawPals = new PawPals();
        pawPals.addClient(client);
        createStorage().savePawPals(pawPals);

        Client restored = createStorage().readPawPals().orElseThrow().getClientList().getFirst();
        assertEquals(client, restored);
        assertEquals(5, restored.getHistory().size());
        assertEquals("Visit 4", restored.getHistory().getFirst().getNotes().toString());
        assertTrue(restored.getPets().isEmpty());
        assertEquals(Set.of(new PetName("Milo")), restored.getHistory().getFirst().getAppointment().getPetNames());
    }

    @Test
    public void readOlderFiles_withoutHistory_loadsEmptyHistoryAndRoundTrips() throws Exception {
        for (String filename : List.of("v1.3FormatPawPals.json", "typicalClientsPawPals.json")) {
            Path fixture = Paths.get("src", "test", "data", "JsonSerializablePawPalsTest", filename);
            PawPals oldData = new PawPals(new JsonPawPalsStorage(fixture).readPawPals().orElseThrow());
            assertTrue(oldData.getClientList().stream().allMatch(client -> client.getHistory().isEmpty()));
            createStorage().savePawPals(oldData);
            assertEquals(oldData, new PawPals(createStorage().readPawPals().orElseThrow()));
        }
    }

    @Test
    public void readInvalidHistory_rejectsFileRatherThanDroppingRecords() throws Exception {
        JsonAdaptedAppointment appointment = new JsonAdaptedAppointment(APPOINTMENT);
        List<List<JsonAdaptedCompletedSession>> invalidHistory = List.of(
                Arrays.asList((JsonAdaptedCompletedSession) null),
                List.of(new JsonAdaptedCompletedSession(null, "Notes", CLOCK.instant().toString())),
                List.of(new JsonAdaptedCompletedSession(appointment, "", CLOCK.instant().toString())),
                List.of(new JsonAdaptedCompletedSession(appointment, "Notes", "invalid")));
        for (List<JsonAdaptedCompletedSession> history : invalidHistory) {
            JsonAdaptedClient adapted = new JsonAdaptedClient(ALICE.getName().toString(), ALICE.getPhone().toString(),
                    ALICE.getEmail().toString(), ALICE.getAddress().toString(),
                    List.of(), List.of(), List.of(), history);
            JsonUtil.saveJsonFile(new JsonSerializablePawPals(List.of(adapted)), testFolder.resolve("pawPals.json"));
            assertThrows(DataLoadingException.class, () -> createStorage().readPawPals());
        }
    }

    @Test
    public void commands_editAddPetAndAppointment_preserveHistoryAfterReload() throws Exception {
        Model model = modelWithHistory();
        LogicManager logic = new LogicManager(model, createStorage());
        logic.execute("find Alice");
        // Equivalence Partition: client edit with c/ preserves completed history.
        logic.execute("edit c/1 n/Alice Updated p/98765432 e/updated@example.com a/New address t/vip");
        logic.execute("addp c/1 n/Luna s/Cat b/Persian");
        logic.execute("appt c/1 d/31-12-9999 t/12:00");

        Client restored = createStorage().readPawPals().orElseThrow().getClientList().getFirst();
        assertEquals("Alice Updated", restored.getName().toString());
        assertEquals(2, restored.getPets().size());
        assertEquals(1, restored.getAppointments().size());
        assertEquals(List.of(SESSION), restored.getHistory());
        assertEquals(MILO.getNotes(), restored.getPets().getFirst().getNotes());
        assertEquals(BENSON, createStorage().readPawPals().orElseThrow().getClientList().get(1));
    }

    @Test
    public void commands_deleteClientAndClear_removeOnlyAffectedHistoryAfterReload() throws Exception {
        Model model = modelWithHistory();
        Client benson = model.getPawPals().getClientList().get(1);
        Client bensonWithHistory = new ClientBuilder(benson).withHistory(SESSION).build();
        model.setClient(benson, bensonWithHistory);
        LogicManager logic = new LogicManager(model, createStorage());

        logic.execute("find Alice");
        // Confirm deletion must preserve the other client's saved history.
        logic.execute("delc c/1");
        logic.execute("y");
        assertEquals(List.of(bensonWithHistory), createStorage().readPawPals().orElseThrow().getClientList());

        StorageManager restartedStorage = createStorage();
        Model restored = new ModelManager(restartedStorage.readPawPals().orElseThrow(), new UserPrefs());
        // Confirmed clear removes the remaining client's persisted history after reload.
        LogicManager restartedLogic = new LogicManager(restored, restartedStorage);
        restartedLogic.execute("clear");
        restartedLogic.execute("y");
        assertTrue(createStorage().readPawPals().orElseThrow().getClientList().isEmpty());
    }

    private Model modelWithHistory() {
        PawPals pawPals = new PawPals();
        pawPals.addClient(new ClientBuilder(ALICE).withPets(MILO).withHistory(SESSION).build());
        pawPals.addClient(BENSON);
        return new ModelManager(pawPals, new UserPrefs());
    }

    private StorageManager createStorage() {
        return new StorageManager(new JsonPawPalsStorage(testFolder.resolve("pawPals.json")),
                new JsonUserPrefsStorage(testFolder.resolve("userPrefs.json")));
    }
}
