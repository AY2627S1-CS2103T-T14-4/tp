package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.CommandTestUtil.showClientAtIndex;
import static seedu.address.testutil.TypicalClients.ALICE;
import static seedu.address.testutil.TypicalClients.BENSON;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_CLIENT;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_CLIENT;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.LinkedHashSet;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
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

public class DoneCommandTest {
    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-12-12T06:00:00Z"),
            ZoneId.of("Asia/Singapore"));
    private static final SessionNotes NOTES = new SessionNotes("  Teddy cut, short ears  ");
    private static final Pet MILO = new Pet(new PetName("Milo"), new Species("Dog"), new Breed("Poodle"))
            .withNote(NoteType.SENS, new PetNote("Sensitive paws"));
    private static final Pet LUNA = new Pet(new PetName("Luna"), new Species("Cat"), new Breed("Persian"));
    private static final AppointmentDate DATE = new AppointmentDate("12-12-2026");
    private static final AppointmentTime TIME = new AppointmentTime("14:00");
    private static final Appointment TARGET = new Appointment(DATE, TIME, "Call on arrival",
            new LinkedHashSet<>(List.of(MILO.getName(), LUNA.getName())))
            .withEndTime(new AppointmentTime("15:30"));
    private static final Appointment LATER = new Appointment(DATE, new AppointmentTime("16:00"));
    private static final Appointment PAST = new Appointment(new AppointmentDate("11-12-2026"), TIME);
    private static final CompletedSession OLD_SESSION = new CompletedSession(
            new Appointment(new AppointmentDate("10-12-2026"), TIME), NOTES, CLOCK.instant().minusSeconds(60));

    private CountingModel model;
    private Client selected;
    private Client other;

    @BeforeEach
    public void setUp() {
        selected = new ClientBuilder(BENSON).withPets(MILO, LUNA).withAppointments(PAST, TARGET, LATER)
                .withHistory(OLD_SESSION).build();
        other = new ClientBuilder(ALICE).withAppointments(new Appointment(DATE, new AppointmentTime("09:00"))).build();
        PawPals data = new PawPals();
        data.addClient(other);
        data.addClient(selected);
        model = new CountingModel(data);
    }

    @Test
    public void constructorAndExecute_nullArguments_throwNullPointerException() {
        assertThrows(NullPointerException.class, () -> new DoneCommand(null, DATE, TIME, NOTES));
        assertThrows(NullPointerException.class, () -> new DoneCommand(INDEX_FIRST_CLIENT, null, TIME, NOTES));
        assertThrows(NullPointerException.class, () -> new DoneCommand(INDEX_FIRST_CLIENT, DATE, null, NOTES));
        assertThrows(NullPointerException.class, () -> new DoneCommand(INDEX_FIRST_CLIENT, DATE, TIME, null));
        assertThrows(NullPointerException.class, () -> new DoneCommand(INDEX_FIRST_CLIENT, DATE, TIME, NOTES, null));
        assertThrows(NullPointerException.class, () -> command(INDEX_FIRST_CLIENT, TARGET, CLOCK).execute(null));
    }

    @Test
    public void execute_atStartBeforePlannedEnd_movesFullSnapshotInOneUpdate() throws Exception {
        CommandResult result = command(INDEX_SECOND_CLIENT, TARGET, CLOCK).execute(model);
        CompletedSession session = new CompletedSession(TARGET, NOTES, CLOCK.instant());
        Client expected = new ClientBuilder(selected).withAppointments(PAST, LATER)
                .withHistory(session, OLD_SESSION).build();
        assertEquals(List.of(other, expected), model.getPawPals().getClientList());
        assertEquals(1, model.updateCount);
        assertEquals(List.of(PAST, TARGET, LATER), selected.getAppointments());
        assertEquals("Completed session: Benson Meier d/12-12-2026 t/14:00 et/15:30"
                + " pn/Milo pn/Luna nt/Teddy cut, short ears", result.getFeedbackToUser());
    }

    @Test
    public void execute_pastClientOnlyVisit_doesNotInferCurrentPets() throws Exception {
        SessionNotes longNotes = new SessionNotes("x".repeat(200));
        CommandResult result = new DoneCommand(INDEX_SECOND_CLIENT, PAST.getDate(), PAST.getTime(), longNotes, CLOCK)
                .execute(model);
        Client expected = new ClientBuilder(selected).withAppointments(TARGET, LATER)
                .withHistory(new CompletedSession(PAST, longNotes, CLOCK.instant()), OLD_SESSION).build();
        assertEquals(List.of(other, expected), model.getPawPals().getClientList());
        assertTrue(expected.getHistory().getFirst().getAppointment().getPetNames().isEmpty());
        assertEquals("Completed session: Benson Meier d/11-12-2026 t/14:00 nt/" + longNotes,
                result.getFeedbackToUser());
    }

    @Test
    public void execute_filteredList_usesDisplayedIndexAndPreservesFilter() throws Exception {
        showClientAtIndex(model, INDEX_SECOND_CLIENT);
        command(INDEX_FIRST_CLIENT, TARGET, CLOCK).execute(model);
        assertEquals(1, model.getFilteredClientList().size());
        assertEquals(BENSON.getName(), model.getFilteredClientList().getFirst().getName());
        assertEquals(2, model.getFilteredClientList().getFirst().getAppointments().size());
        assertEquals(other, model.getPawPals().getClientList().getFirst());
    }

    @Test
    public void execute_lastActiveAppointment_keepsClientAndOtherData() throws Exception {
        Client onlyTarget = new ClientBuilder(selected).withAppointments(TARGET).build();
        model.setClient(selected, onlyTarget);
        command(INDEX_SECOND_CLIENT, TARGET, CLOCK).execute(model);
        assertEquals(new ClientBuilder(onlyTarget).withAppointments()
                .withHistory(new CompletedSession(TARGET, NOTES, CLOCK.instant()), OLD_SESSION).build(),
                model.getPawPals().getClientList().get(1));
    }

    @Test
    public void execute_futureStartOrWrongZone_doesNotChangeData() {
        assertCommandFailure(command(INDEX_SECOND_CLIENT, LATER, CLOCK), model, Client.MESSAGE_APPOINTMENT_NOT_STARTED);
        assertCommandFailure(command(INDEX_SECOND_CLIENT, TARGET,
                Clock.fixed(CLOCK.instant().minusSeconds(1), CLOCK.getZone())), model,
                Client.MESSAGE_APPOINTMENT_NOT_STARTED);
        assertCommandFailure(command(INDEX_SECOND_CLIENT, TARGET, CLOCK.withZone(ZoneOffset.UTC)), model,
                Client.MESSAGE_APPOINTMENT_NOT_STARTED);
        assertEquals(0, model.updateCount);
    }

    @Test
    public void execute_invalidOrEmptyDisplayedList_doesNotChangeData() {
        assertCommandFailure(command(Index.fromOneBased(3), TARGET, CLOCK), model,
                Messages.MESSAGE_INVALID_CLIENT_DISPLAYED_INDEX);
        showClientAtIndex(model, INDEX_SECOND_CLIENT);
        assertCommandFailure(command(INDEX_SECOND_CLIENT, TARGET, CLOCK), model,
                Messages.MESSAGE_INVALID_CLIENT_DISPLAYED_INDEX);
        model.updateFilteredClientList(client -> false);
        assertCommandFailure(command(INDEX_FIRST_CLIENT, TARGET, CLOCK), model,
                Messages.MESSAGE_INVALID_CLIENT_DISPLAYED_INDEX);
        assertCommandFailure(command(INDEX_FIRST_CLIENT, TARGET, CLOCK), new ModelManager(),
                Messages.MESSAGE_INVALID_CLIENT_DISPLAYED_INDEX);
        assertEquals(0, model.updateCount);
    }

    @Test
    public void execute_missingSlotOrEndTime_doesNotCompleteAnotherAppointment() {
        assertCommandFailure(command(INDEX_FIRST_CLIENT, TARGET, CLOCK), model,
                "Alice Pauline has no active appointment at 12-12-2026 14:00.");
        assertCommandFailure(new DoneCommand(INDEX_SECOND_CLIENT, DATE, new AppointmentTime("15:30"), NOTES, CLOCK),
                model, "Benson Meier has no active appointment at 12-12-2026 15:30.");
        assertEquals(0, model.updateCount);
    }

    @Test
    public void execute_repeatedCompletion_rejectsWithoutDuplicatingOrChangingNotes() throws Exception {
        command(INDEX_SECOND_CLIENT, TARGET, CLOCK).execute(model);
        DoneCommand repeated = new DoneCommand(INDEX_SECOND_CLIENT, DATE, TIME, new SessionNotes("Changed"), CLOCK);
        assertCommandFailure(repeated, model, "Benson Meier already has a completed session at 12-12-2026 14:00.");
        assertEquals(1, model.updateCount);
        assertEquals(NOTES, model.getPawPals().getClientList().get(1).getHistory().getFirst().getNotes());
    }

    @Test
    public void execute_activeSlotMatchingOldHistory_completesActiveBookingFirst() throws Exception {
        CompletedSession previous = new CompletedSession(TARGET, new SessionNotes("Previous record"),
                CLOCK.instant().minusSeconds(1));
        Client withHistory = new ClientBuilder(selected).withHistory(previous, OLD_SESSION).build();
        model.setClient(selected, withHistory);
        command(INDEX_SECOND_CLIENT, TARGET, CLOCK).execute(model);
        assertEquals(List.of(new CompletedSession(TARGET, NOTES, CLOCK.instant()), previous, OLD_SESSION),
                model.getPawPals().getClientList().get(1).getHistory());
    }

    @Test
    public void execute_completionReleasesRemainingInterval_butBookingStillRequiresFutureStart() throws Exception {
        Appointment next = new Appointment(DATE, new AppointmentTime("14:15"))
                .withEndTime(new AppointmentTime("15:00"));
        assertCommandFailure(new AppointmentCommand(INDEX_FIRST_CLIENT, next, CLOCK), model,
                "Appointment overlaps with Benson Meier's appointment: d/12-12-2026 t/14:00 et/15:30.");
        command(INDEX_SECOND_CLIENT, TARGET, CLOCK).execute(model);
        new AppointmentCommand(INDEX_FIRST_CLIENT, next, CLOCK).execute(model);
        assertEquals(next, model.getPawPals().getClientList().getFirst().getAppointments().getLast());
        assertEquals(TARGET, model.getPawPals().getClientList().get(1).getHistory().getFirst().getAppointment());
        assertCommandFailure(new AppointmentCommand(INDEX_FIRST_CLIENT, TARGET, CLOCK), model,
                Messages.MESSAGE_APPOINTMENT_NOT_FUTURE);
    }

    @Test
    public void equalsHashCodeAndToString_compareTargetAndNotesButNotClock() {
        DoneCommand command = command(INDEX_FIRST_CLIENT, TARGET, CLOCK);
        DoneCommand same = new DoneCommand(Index.fromOneBased(1), DATE, TIME, NOTES);
        assertEquals(command, command);
        assertEquals(command, same);
        assertEquals(command.hashCode(), same.hashCode());
        assertNotEquals(command, command(INDEX_SECOND_CLIENT, TARGET, CLOCK));
        assertNotEquals(command, command(INDEX_FIRST_CLIENT, PAST, CLOCK));
        assertNotEquals(command, command(INDEX_FIRST_CLIENT, LATER, CLOCK));
        assertNotEquals(command, new DoneCommand(INDEX_FIRST_CLIENT, DATE, TIME, new SessionNotes("Other")));
        assertNotEquals(command, null);
        assertNotEquals(command, "done");
        assertEquals(DoneCommand.class.getCanonicalName() + "{targetIndex=" + INDEX_FIRST_CLIENT + ", date=" + DATE
                + ", time=" + TIME + ", notes=" + NOTES + "}", command.toString());
    }

    private static DoneCommand command(Index index, Appointment appointment, Clock clock) {
        return new DoneCommand(index, appointment.getDate(), appointment.getTime(), NOTES, clock);
    }

    private static class CountingModel extends ModelManager {
        private int updateCount;

        CountingModel(PawPals data) {
            super(data, new UserPrefs());
        }

        @Override
        public void setClient(Client target, Client editedClient) {
            super.setClient(target, editedClient);
            updateCount++;
        }
    }
}
