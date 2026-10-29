package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.testutil.TypicalClients.ALICE;
import static seedu.address.testutil.TypicalClients.BENSON;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_CLIENT;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_CLIENT;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;

import seedu.address.logic.Messages;
import seedu.address.logic.commands.EditAppointmentCommand.EditAppointmentDescriptor;
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

/**
 * Tests rescheduling with the real model, filtering and global conflict checking.
 */
public class EditAppointmentCommandIntegrationTest {
    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-12-12T04:00:00Z"),
            ZoneId.of("Asia/Singapore"));
    private static final Appointment ORIGINAL = slot("13-12-2026", "14:00", "15:30");
    private static final EditAppointmentDescriptor MOVE_START = edits(null, "16:00", null);

    @Test
    public void execute_startMove_preservesDurationAndAllOtherData() throws Exception {
        Pet milo = new Pet(new PetName("Milo"), new Species("Dog"), new Breed("Poodle"))
                .withNote(NoteType.SENS, new PetNote("Sensitive paws"));
        Appointment original = new Appointment(ORIGINAL.getDate(), ORIGINAL.getTime(), ORIGINAL.getEndTime(),
                "Call on arrival", Set.of(milo.getName()));
        Appointment other = slot("14-12-2026", "10:00", null);
        CompletedSession history = new CompletedSession(slot("11-12-2026", "09:00", "10:00"),
                new SessionNotes("Teddy cut"), CLOCK.instant());
        Client owner = new ClientBuilder(ALICE).withTags("regular").withPets(milo)
                .withAppointments(other, original).withHistory(history).build();
        Model model = modelWith(owner, BENSON);

        CommandResult result = command(original, MOVE_START).execute(model);

        Appointment expected = new Appointment(original.getDate(), new AppointmentTime("16:00"),
                Optional.of(new AppointmentTime("17:30")), original.getDetails(), original.getPetNames());
        assertEquals(List.of(new ClientBuilder(owner).withAppointments(other, expected).build(), BENSON),
                model.getPawPals().getClientList());
        assertEquals("Rescheduled appointment: " + ALICE.getName() + " 13-12-2026 14:00 → 13-12-2026 16:00 et/17:30",
                result.getFeedbackToUser());
        assertEquals(List.of(other, original), owner.getAppointments());
    }

    @Test
    public void execute_slotOverlapsOriginalOrIsUnchanged_excludesOriginal() throws Exception {
        assertReplacement(ORIGINAL, edits(null, "14:30", null), slot("13-12-2026", "14:30", "16:00"));
        assertReplacement(ORIGINAL, edits(null, "14:00", null), ORIGINAL);
        assertReplacement(ORIGINAL, edits(null, "13:30", null), slot("13-12-2026", "13:30", "15:00"));
    }

    @Test
    public void execute_conflictWithSameClient_rejectsOtherBooking() {
        Appointment other = slot("13-12-2026", "16:00", "17:00");
        assertCommandFailure(command(ORIGINAL, MOVE_START), modelWith(owner(ORIGINAL, other)),
                "Appointment overlaps with " + ALICE.getName() + "'s appointment: d/13-12-2026 t/16:00 et/17:00.");
    }

    @Test
    public void execute_conflictWithVisibleOrHiddenClient_leavesDataAndFilterUnchanged() {
        Appointment other = slot("13-12-2026", "16:00", "17:00");
        Client otherOwner = new ClientBuilder(BENSON).withAppointments(other).build();
        for (boolean isFiltered : new boolean[] {false, true}) {
            Model model = modelWith(owner(ORIGINAL), otherOwner);
            if (isFiltered) {
                model.updateFilteredClientList(client -> client.isSameClient(ALICE));
            }
            assertCommandFailure(command(ORIGINAL, MOVE_START), model,
                    "Appointment overlaps with " + BENSON.getName() + "'s appointment: d/13-12-2026 t/16:00 et/17:00.");
        }
    }

    @Test
    public void execute_otherOwnerAtOriginalSlot_isNotExcluded() {
        Model model = modelWith(owner(ORIGINAL), new ClientBuilder(BENSON).withAppointments(ORIGINAL).build());
        assertCommandFailure(command(ORIGINAL, edits(null, "14:30", null)), model,
                "Appointment overlaps with " + BENSON.getName() + "'s appointment: d/13-12-2026 t/14:00 et/15:30.");
    }

    @Test
    public void execute_adjacentBookingOrDifferentDate_succeeds() throws Exception {
        Appointment other = slot("13-12-2026", "16:00", "17:00");
        Model model = modelWith(owner(ORIGINAL, other));
        command(ORIGINAL, edits(null, "14:30", null)).execute(model);
        assertEquals(List.of(slot("13-12-2026", "14:30", "16:00"), other),
                model.getFilteredClientList().getFirst().getAppointments());
        command(other, edits("14-12-2026", null, null)).execute(model);
        assertEquals(slot("14-12-2026", "16:00", "17:00"),
                model.getFilteredClientList().getFirst().getAppointments().get(1));
    }

    @Test
    public void execute_startOnlyBookings_rejectsSameMinuteAndAllowsNextMinute() throws Exception {
        Appointment original = slot("13-12-2026", "14:00", null);
        Appointment other = slot("13-12-2026", "16:00", null);
        Model model = modelWith(owner(original, other));
        assertCommandFailure(command(original, MOVE_START), model,
                "Appointment overlaps with " + ALICE.getName() + "'s appointment: d/13-12-2026 t/16:00.");
        command(original, edits(null, "16:01", null)).execute(model);
        assertEquals(List.of(slot("13-12-2026", "16:01", null), other),
                model.getFilteredClientList().getFirst().getAppointments());
    }

    @Test
    public void execute_filteredTarget_updatesDisplayedClientAndRetainsPredicate() throws Exception {
        Client selected = new ClientBuilder(BENSON).withAppointments(ORIGINAL).build();
        Model model = modelWith(ALICE, selected);
        model.updateFilteredClientList(client -> client.isSameClient(BENSON));

        command(ORIGINAL, MOVE_START).execute(model);

        Client expected = new ClientBuilder(selected).withAppointments(slot("13-12-2026", "16:00", "17:30")).build();
        assertEquals(List.of(expected), model.getFilteredClientList());
        assertEquals(List.of(ALICE, expected), model.getPawPals().getClientList());
        assertCommandFailure(new EditAppointmentCommand(INDEX_SECOND_CLIENT, ORIGINAL.getDate(),
                ORIGINAL.getTime(), MOVE_START, CLOCK), model, Messages.MESSAGE_INVALID_CLIENT_DISPLAYED_INDEX);
    }

    @Test
    public void execute_completedSessionAtDestination_doesNotBlockMoveOrChangeHistory() throws Exception {
        CompletedSession history = new CompletedSession(slot("13-12-2026", "16:00", "17:30"),
                new SessionNotes("Previous visit"), CLOCK.instant());
        Client client = new ClientBuilder(ALICE).withAppointments(ORIGINAL).withHistory(history).build();
        Model model = modelWith(client);
        command(ORIGINAL, MOVE_START).execute(model);
        assertEquals(new ClientBuilder(client).withAppointments(history.getAppointment()).build(),
                model.getFilteredClientList().getFirst());
    }

    @Test
    public void execute_clearEnd_checksOnlyResultingMinuteAgainstHiddenBooking() throws Exception {
        Client hidden = new ClientBuilder(BENSON).withAppointments(slot("13-12-2026", "16:00", "17:00")).build();
        Model model = modelWith(owner(ORIGINAL), hidden);
        model.updateFilteredClientList(client -> client.isSameClient(ALICE));

        EditAppointmentDescriptor clear = new EditAppointmentDescriptor(Optional.empty(),
                Optional.of(new AppointmentTime("15:59")), Optional.empty(), true);
        command(ORIGINAL, clear).execute(model);

        Appointment expected = slot("13-12-2026", "15:59", null);
        Client expectedOwner = owner(expected);
        assertEquals(List.of(expectedOwner), model.getFilteredClientList());
        assertEquals(List.of(expectedOwner, hidden), model.getPawPals().getClientList());
    }

    @Test
    public void execute_pastMove_checksHiddenConflictsAndExcludesOriginalWhilePreservingHistory() throws Exception {
        Appointment past = slot("11-12-2026", "09:00", "10:30");
        Appointment destination = slot("11-12-2026", "10:30", "12:00");
        CompletedSession history = new CompletedSession(destination, new SessionNotes("Previous visit"),
                CLOCK.instant());
        Client selected = new ClientBuilder(ALICE).withAppointments(past).withHistory(history).build();
        Client hidden = new ClientBuilder(BENSON)
                .withAppointments(slot("11-12-2026", "12:00", "13:00")).build();
        Model model = modelWith(selected, hidden);
        model.updateFilteredClientList(client -> client.isSameClient(ALICE));

        assertCommandFailure(command(past, edits(null, "11:00", null)), model,
                "Appointment overlaps with " + BENSON.getName() + "'s appointment: d/11-12-2026 t/12:00 et/13:00.");

        CommandResult result = command(past, edits(null, "10:30", null)).execute(model);
        Client expected = new ClientBuilder(selected).withAppointments(destination).build();
        assertEquals(List.of(expected), model.getFilteredClientList());
        assertEquals(List.of(expected, hidden), model.getPawPals().getClientList());
        assertEquals("Rescheduled appointment: " + ALICE.getName()
                + " 11-12-2026 09:00 → 11-12-2026 10:30 et/12:00\n"
                + AppointmentCommand.MESSAGE_PAST_APPOINTMENT, result.getFeedbackToUser());

        command(destination, edits(null, "10:30", null)).execute(model);
        assertEquals(List.of(expected), model.getFilteredClientList());
        assertEquals(List.of(expected, hidden), model.getPawPals().getClientList());
    }

    @Test
    public void execute_clearEndAtHiddenBooking_rejectsWithoutChangingDataOrFilter() {
        Client hidden = new ClientBuilder(BENSON).withAppointments(slot("13-12-2026", "16:00", "17:00")).build();
        Model model = modelWith(owner(ORIGINAL), hidden);
        model.updateFilteredClientList(client -> client.isSameClient(ALICE));
        EditAppointmentDescriptor clear = new EditAppointmentDescriptor(Optional.empty(),
                Optional.of(new AppointmentTime("16:00")), Optional.empty(), true);

        assertCommandFailure(command(ORIGINAL, clear), model,
                "Appointment overlaps with " + BENSON.getName() + "'s appointment: d/13-12-2026 t/16:00 et/17:00.");
    }

    private static void assertReplacement(Appointment original, EditAppointmentDescriptor edits, Appointment expected)
            throws Exception {
        Model model = modelWith(owner(original));
        command(original, edits).execute(model);
        assertEquals(List.of(expected), model.getFilteredClientList().getFirst().getAppointments());
    }

    private static EditAppointmentCommand command(Appointment original, EditAppointmentDescriptor edits) {
        return new EditAppointmentCommand(INDEX_FIRST_CLIENT, original.getDate(), original.getTime(), edits, CLOCK);
    }

    private static EditAppointmentDescriptor edits(String date, String start, String end) {
        return new EditAppointmentDescriptor(Optional.ofNullable(date).map(AppointmentDate::new),
                Optional.ofNullable(start).map(AppointmentTime::new),
                Optional.ofNullable(end).map(AppointmentTime::new));
    }

    private static Appointment slot(String date, String start, String end) {
        return new Appointment(new AppointmentDate(date), new AppointmentTime(start),
                Optional.ofNullable(end).map(AppointmentTime::new), "", Set.of());
    }

    private static Client owner(Appointment... appointments) {
        return new ClientBuilder(ALICE).withAppointments(appointments).build();
    }

    private static Model modelWith(Client... clients) {
        PawPals pawPals = new PawPals();
        for (Client client : clients) {
            pawPals.addClient(client);
        }
        return new ModelManager(pawPals, new UserPrefs());
    }
}
