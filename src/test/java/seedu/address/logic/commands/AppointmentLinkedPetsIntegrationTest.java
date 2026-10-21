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

public class AppointmentLinkedPetsIntegrationTest {
    private static final String FUTURE_DATE = LocalDate.now().plusYears(1)
            .format(DateTimeFormatter.ofPattern("dd-MM-uuuu"));
    private static final Pet MILO = new Pet(new PetName("Milo"), new Species("Dog"), new Breed("Poodle"))
            .withNote(NoteType.SENS, new PetNote("Sensitive paws"));
    private static final Pet FLUFFY = new Pet(new PetName("Sir Fluffy"), new Species("Cat"), new Breed("Persian"));
    private static final Pet LUNA = new Pet(new PetName("Luna"), new Species("Cat"), new Breed("Persian"));
    private static final Appointment PAST = new Appointment(new AppointmentDate("29-02-2024"),
            new AppointmentTime("09:00"), "Previous visit", Set.of(MILO.getName()));
    private static final CompletedSession HISTORY = new CompletedSession(PAST, new SessionNotes("Teddy cut"),
            Instant.parse("2024-02-29T02:00:00Z"));
    private static final Client SELECTED = new ClientBuilder(BENSON).withPets(MILO, FLUFFY)
            .withAppointments(PAST).withHistory(HISTORY).build();
    private static final Client OTHER_CLIENT = new ClientBuilder(ALICE).withPets(LUNA).build();

    @TempDir
    public Path testFolder;

    private Model model;
    private LogicManager logic;

    @BeforeEach
    public void setUp() {
        PawPals data = new PawPals();
        data.addClient(OTHER_CLIENT);
        data.addClient(SELECTED);
        model = new ModelManager(data, new UserPrefs());
        logic = new LogicManager(model, createStorage());
    }

    @Test
    public void execute_afterFind_roundTripsCanonicalPetNamesInInputOrderWithEndTime() throws Exception {
        logic.execute("find Benson");
        CommandResult result = logic.execute("appt pn/sir   FLUFFY c/1 et/15:00 d/" + FUTURE_DATE
                + " pn/mILO t/14:00 pn/Milo pn/Sir Fluffy");

        Appointment expected = slot("14:00", "15:00")
                .withPetNames(new LinkedHashSet<>(List.of(FLUFFY.getName(), MILO.getName())));
        Client updated = SELECTED.copyWithAppointment(expected);
        assertEquals("Added appointment: Benson Meier d/" + FUTURE_DATE
                + " t/14:00 et/15:00 pn/Sir Fluffy pn/Milo", result.getFeedbackToUser());
        assertEquals(List.of(updated), model.getFilteredClientList());

        Model restored = new ModelManager(createStorage().readPawPals().orElseThrow(), new UserPrefs());
        assertEquals(List.of(OTHER_CLIENT, updated), restored.getPawPals().getClientList());
        assertEquals(List.of(FLUFFY.getName(), MILO.getName()), List.copyOf(restored.getPawPals()
                .getClientList().get(1).getAppointments().getLast().getPetNames()));
        assertEquals(List.of(HISTORY), restored.getPawPals().getClientList().get(1).getHistory());
    }

    @Test
    public void execute_withoutPetNamesOrEndTime_doesNotInferPetsAndPreservesHistoryAfterReload() throws Exception {
        logic.execute("find Benson");
        CommandResult result = logic.execute("appt c/1 d/" + FUTURE_DATE + " t/14:00");
        Appointment expected = new Appointment(new AppointmentDate(FUTURE_DATE), new AppointmentTime("14:00"));
        assertEquals("Added appointment: Benson Meier d/" + FUTURE_DATE + " t/14:00", result.getFeedbackToUser());
        assertEquals(List.of(OTHER_CLIENT, SELECTED.copyWithAppointment(expected)),
                createStorage().readPawPals().orElseThrow().getClientList());
    }

    @Test
    public void execute_unknownOrOtherClientsPet_preservesAllDataFilterAndSavedFile() throws Exception {
        logic.execute("find Benson");
        ReadOnlyPawPals original = new PawPals(model.getPawPals());
        String saved = Files.readString(dataFile());
        for (String name : List.of("Unknown", "Luna")) {
            CommandException exception = assertThrows(CommandException.class, () -> logic.execute(
                    "appt c/1 d/" + FUTURE_DATE + " t/14:00 et/15:00 pn/Milo pn/" + name));
            assertEquals("This client has no pet named " + name + ".", exception.getMessage());
            assertUnchanged(original, List.of(SELECTED), saved);
        }
        logic.execute("find Alice");
        String savedAfterFind = Files.readString(dataFile());
        CommandException exception = assertThrows(CommandException.class, () -> logic.execute(
                "appt c/1 d/" + FUTURE_DATE + " t/14:00 pn/Milo"));
        assertEquals("This client has no pet named Milo.", exception.getMessage());
        assertUnchanged(original, List.of(OTHER_CLIENT), savedAfterFind);
    }

    @Test
    public void execute_malformedInputWithPetNames_preservesMemoryAndSavedFile() throws Exception {
        logic.execute("find Benson");
        ReadOnlyPawPals original = new PawPals(model.getPawPals());
        String saved = Files.readString(dataFile());
        String base = "appt c/1 d/" + FUTURE_DATE + " t/14:00 et/15:00";
        for (String input : List.of(base + " pn/", base + " pn/Milo pn/", base + " pn/M!lo",
                base + " pn/" + "x".repeat(41), base + " pn/Milo c/1", base + " pn/Milo et/16:00",
                "appt c/0 d/" + FUTURE_DATE + " t/14:00 pn/Milo",
                "appt c/1 d/" + FUTURE_DATE + " t/14:00 et/13:00 pn/Milo",
                "appt c/1 d/31-02-2027 t/14:00 pn/Milo")) {
            assertThrows(ParseException.class, () -> logic.execute(input));
            assertUnchanged(original, List.of(SELECTED), saved);
        }
        assertThrows(CommandException.class, () -> logic.execute(
                "appt c/2 d/" + FUTURE_DATE + " t/14:00 pn/Milo"));
        assertUnchanged(original, List.of(SELECTED), saved);
    }

    @Test
    public void execute_otherClientHasDifferentPet_rejectsOverlapWithHiddenBooking() throws Exception {
        logic.execute("appt c/1 d/" + FUTURE_DATE + " t/14:00 et/15:00 pn/Luna");
        logic.execute("find Benson");
        ReadOnlyPawPals original = new PawPals(model.getPawPals());
        String saved = Files.readString(dataFile());

        CommandException exception = assertThrows(CommandException.class, () -> logic.execute(
                "appt c/1 d/" + FUTURE_DATE + " t/14:30 et/15:30 pn/Milo"));

        assertEquals("Appointment overlaps with Alice Pauline's appointment: d/" + FUTURE_DATE
                + " t/14:00 et/15:00.", exception.getMessage());
        assertUnchanged(original, List.of(SELECTED), saved);
    }

    @Test
    public void execute_sameClientDifferentPets_rejectsOverlapButAllowsAdjacentBookings() throws Exception {
        logic.execute("find Benson");
        logic.execute("appt c/1 d/" + FUTURE_DATE + " t/14:00 et/15:00 pn/Milo");
        ReadOnlyPawPals original = new PawPals(model.getPawPals());
        List<Client> displayed = List.copyOf(model.getFilteredClientList());
        String saved = Files.readString(dataFile());
        CommandException exception = assertThrows(CommandException.class, () -> logic.execute(
                "appt c/1 d/" + FUTURE_DATE + " t/14:30 et/15:30 pn/Sir Fluffy"));
        assertEquals("Appointment overlaps with Benson Meier's appointment: d/" + FUTURE_DATE
                + " t/14:00 et/15:00.", exception.getMessage());
        assertUnchanged(original, displayed, saved);

        logic.execute("appt c/1 d/" + FUTURE_DATE + " t/15:00 et/16:00 pn/Sir Fluffy");
        Client expected = SELECTED.copyWithAppointment(slot("14:00", "15:00").withPetNames(Set.of(MILO.getName())))
                .copyWithAppointment(slot("15:00", "16:00").withPetNames(Set.of(FLUFFY.getName())));
        assertEquals(List.of(OTHER_CLIENT, expected), createStorage().readPawPals().orElseThrow().getClientList());
    }

    private void assertUnchanged(ReadOnlyPawPals original, List<Client> displayed, String saved) throws Exception {
        assertEquals(original, model.getPawPals());
        assertEquals(displayed, model.getFilteredClientList());
        assertEquals(saved, Files.readString(dataFile()));
        assertEquals(original, createStorage().readPawPals().orElseThrow());
    }

    private static Appointment slot(String start, String end) {
        return new Appointment(new AppointmentDate(FUTURE_DATE), new AppointmentTime(start))
                .withEndTime(new AppointmentTime(end));
    }

    private Path dataFile() {
        return testFolder.resolve("pawPals.json");
    }

    private StorageManager createStorage() {
        return new StorageManager(new JsonPawPalsStorage(dataFile()),
                new JsonUserPrefsStorage(testFolder.resolve("userPrefs.json")));
    }
}
