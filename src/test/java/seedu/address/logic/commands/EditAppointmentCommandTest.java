package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
import java.util.function.Predicate;

import org.junit.jupiter.api.Test;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import seedu.address.commons.core.GuiSettings;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.EditAppointmentCommand.EditAppointmentDescriptor;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.ReadOnlyPawPals;
import seedu.address.model.ReadOnlyUserPrefs;
import seedu.address.model.appointment.Appointment;
import seedu.address.model.appointment.AppointmentConflictChecker.Conflict;
import seedu.address.model.appointment.AppointmentDate;
import seedu.address.model.appointment.AppointmentTime;
import seedu.address.model.client.Client;
import seedu.address.testutil.ClientBuilder;

/**
 * Tests command validation and orchestration using a model stub without real filtering or conflict checking.
 */
public class EditAppointmentCommandTest {
    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-12-12T04:00:00Z"),
            ZoneId.of("Asia/Singapore"));
    private static final Appointment ORIGINAL = slot("13-12-2026", "14:00", "15:30");
    private static final EditAppointmentDescriptor MOVE_START = edits(null, "16:00", null);

    @Test
    public void constructor_nullArguments_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new EditAppointmentCommand(null,
                ORIGINAL.getDate(), ORIGINAL.getTime(), MOVE_START));
        assertThrows(NullPointerException.class, () -> new EditAppointmentCommand(INDEX_FIRST_CLIENT,
                null, ORIGINAL.getTime(), MOVE_START));
        assertThrows(NullPointerException.class, () -> new EditAppointmentCommand(INDEX_FIRST_CLIENT,
                ORIGINAL.getDate(), null, MOVE_START));
        assertThrows(NullPointerException.class, () -> new EditAppointmentCommand(INDEX_FIRST_CLIENT,
                ORIGINAL.getDate(), ORIGINAL.getTime(), null));
        assertThrows(NullPointerException.class, () -> new EditAppointmentCommand(INDEX_FIRST_CLIENT,
                ORIGINAL.getDate(), ORIGINAL.getTime(), MOVE_START, null));
    }

    @Test
    public void descriptor_nullOrEmptyOptionals_rejectsInvalidState() {
        assertThrows(NullPointerException.class, () -> new EditAppointmentDescriptor(
                null, Optional.empty(), Optional.empty()));
        assertThrows(NullPointerException.class, () -> new EditAppointmentDescriptor(
                Optional.empty(), null, Optional.empty()));
        assertThrows(NullPointerException.class, () -> new EditAppointmentDescriptor(
                Optional.empty(), Optional.empty(), null));
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class, () -> edits(null, null, null));
        assertEquals(EditAppointmentCommand.MESSAGE_NOT_EDITED, error.getMessage());
    }

    @Test
    public void execute_nullModel_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> command(ORIGINAL, MOVE_START).execute(null));
    }

    @Test
    public void descriptor_clearEnd_rejectsExplicitEnd() {
        assertThrows(IllegalArgumentException.class, () -> new EditAppointmentDescriptor(
                Optional.empty(), Optional.empty(), Optional.of(new AppointmentTime("15:00")), true));
    }

    @Test
    public void descriptor_clearEnd_distinguishesPreservingAndClearing() {
        EditAppointmentDescriptor clear = clearEnd(null, "16:00");
        assertEquals(clear, clearEnd(null, "16:00"));
        assertEquals(clear.hashCode(), clearEnd(null, "16:00").hashCode());
        assertNotEquals(MOVE_START, clear);
        assertNotEquals(command(ORIGINAL, MOVE_START), command(ORIGINAL, clear));
        assertEquals(MOVE_START, new EditAppointmentDescriptor(Optional.empty(),
                Optional.of(new AppointmentTime("16:00")), Optional.empty(), false));
    }

    @Test
    public void execute_clearEnd_preservesDateAndStartAndOmitsEndInFeedback() throws Exception {
        ModelStub model = modelWith(owner(ORIGINAL));
        CommandResult result = command(ORIGINAL, clearEnd(null, null)).execute(model);
        Appointment expected = slot("13-12-2026", "14:00", null);
        assertEquals(List.of(expected), model.editedClient.getAppointments());
        assertEquals(expected, model.checkedAppointment);
        assertEquals(1, model.updateCount);
        assertEquals("Rescheduled appointment: " + ALICE.getName()
                + " 13-12-2026 14:00 → 13-12-2026 14:00", result.getFeedbackToUser());
    }

    @Test
    public void execute_clearEndWithReschedule_skipsDurationPreservationAndOverflow() throws Exception {
        assertReplacement(ORIGINAL, clearEnd(null, "16:00"), slot("13-12-2026", "16:00", null));
        assertReplacement(ORIGINAL, clearEnd("14-12-2026", null), slot("14-12-2026", "14:00", null));
        assertReplacement(ORIGINAL, clearEnd("14-12-2026", "23:59"), slot("14-12-2026", "23:59", null));
    }

    @Test
    public void execute_clearAbsentEnd_succeeds() throws Exception {
        Appointment original = slot("13-12-2026", "14:00", null);
        assertReplacement(original, clearEnd(null, null), original);
    }

    @Test
    public void execute_clearEndWithPastStart_leavesDataUnchanged() {
        Appointment past = slot("11-12-2026", "14:00", "15:30");
        assertCommandFailure(command(past, clearEnd(null, null)), modelWith(owner(past)),
                Messages.MESSAGE_APPOINTMENT_NOT_FUTURE);
    }

    @Test
    public void execute_emptyListOrInvalidDisplayedIndex_leavesDataUnchanged() {
        assertCommandFailure(command(ORIGINAL, MOVE_START), modelWith(),
                Messages.MESSAGE_INVALID_CLIENT_DISPLAYED_INDEX);
        ModelStub model = modelWith(owner(ORIGINAL));
        EditAppointmentCommand command = new EditAppointmentCommand(INDEX_SECOND_CLIENT,
                ORIGINAL.getDate(), ORIGINAL.getTime(), MOVE_START, CLOCK);
        assertCommandFailure(command, model, Messages.MESSAGE_INVALID_CLIENT_DISPLAYED_INDEX);
    }

    @Test
    public void execute_missingAppointment_leavesDataUnchanged() {
        assertCommandFailure(command(ORIGINAL, MOVE_START), modelWith(ALICE),
                ALICE.getName() + " has no appointment at 13-12-2026 14:00.");
    }

    @Test
    public void execute_dateOnlyMove_preservesStartAndEnd() throws Exception {
        assertReplacement(ORIGINAL, edits("14-12-2026", null, null), slot("14-12-2026", "14:00", "15:30"));
    }

    @Test
    public void execute_endOnlyEdit_preservesDateAndStartAndReportsNewEnd() throws Exception {
        ModelStub model = modelWith(owner(ORIGINAL));
        CommandResult result = command(ORIGINAL, edits(null, null, "16:00")).execute(model);
        assertEquals(List.of(slot("13-12-2026", "14:00", "16:00")),
                model.editedClient.getAppointments());
        assertEquals(1, model.updateCount);
        assertTrue(result.getFeedbackToUser().endsWith("et/16:00"));
    }

    @Test
    public void execute_explicitEnd_overridesDurationEvenWhenPreservedEndWouldOverflow() throws Exception {
        assertReplacement(ORIGINAL, edits("14-12-2026", "23:00", "23:30"),
                slot("14-12-2026", "23:00", "23:30"));
    }

    @Test
    public void execute_startOnlyBooking_staysWithoutEndUnlessEndIsSupplied() throws Exception {
        Appointment original = slot("13-12-2026", "14:00", null);
        assertReplacement(original, edits(null, "23:59", null), slot("13-12-2026", "23:59", null));
        assertReplacement(original, edits("14-12-2026", null, null), slot("14-12-2026", "14:00", null));
        assertReplacement(original, edits(null, null, "15:00"), slot("13-12-2026", "14:00", "15:00"));
    }

    @Test
    public void execute_durationNearMidnight_accepts2358And2359ButRejectsMidnight() throws Exception {
        assertReplacement(ORIGINAL, edits(null, "22:28", null), slot("13-12-2026", "22:28", "23:58"));
        assertReplacement(ORIGINAL, edits(null, "22:29", null), slot("13-12-2026", "22:29", "23:59"));
        for (String start : new String[] {"22:30", "23:59"}) {
            assertCommandFailure(command(ORIGINAL, edits(null, start, null)), modelWith(owner(ORIGINAL)),
                    EditAppointmentCommand.MESSAGE_END_AFTER_DAY);
        }
    }

    @Test
    public void execute_endBeforeOrAtStart_leavesDataUnchanged() {
        for (String end : new String[] {"13:59", "14:00"}) {
            assertCommandFailure(command(ORIGINAL, edits(null, null, end)), modelWith(owner(ORIGINAL)),
                    Appointment.MESSAGE_END_NOT_AFTER_START);
        }
    }

    @Test
    public void execute_resultingStartBeforeOrAtNow_leavesDataUnchanged() {
        for (String start : new String[] {"11:59", "12:00"}) {
            assertCommandFailure(command(ORIGINAL, edits("12-12-2026", start, null)), modelWith(owner(ORIGINAL)),
                    Messages.MESSAGE_APPOINTMENT_NOT_FUTURE);
        }
        assertCommandFailure(command(ORIGINAL, edits("11-12-2026", null, null)), modelWith(owner(ORIGINAL)),
                Messages.MESSAGE_APPOINTMENT_NOT_FUTURE);
    }

    @Test
    public void execute_pastOriginalMovedJustAfterNow_succeeds() throws Exception {
        Appointment past = slot("11-12-2026", "09:00", "10:30");
        assertReplacement(past, edits("12-12-2026", "12:01", null), slot("12-12-2026", "12:01", "13:31"));
    }

    @Test
    public void execute_unchangedPastStartWithNewEnd_rejectsPastResult() {
        Appointment past = slot("11-12-2026", "09:00", null);
        assertCommandFailure(command(past, edits(null, null, "10:00")), modelWith(owner(past)),
                Messages.MESSAGE_APPOINTMENT_NOT_FUTURE);
    }

    @Test
    public void execute_clockHasSeconds_requiresStartAfterFullInstant() {
        Clock clock = Clock.fixed(CLOCK.instant().plusSeconds(30), CLOCK.getZone());
        EditAppointmentCommand command = new EditAppointmentCommand(INDEX_FIRST_CLIENT, ORIGINAL.getDate(),
                ORIGINAL.getTime(), edits("12-12-2026", "12:00", null), clock);
        assertCommandFailure(command, modelWith(owner(ORIGINAL)), Messages.MESSAGE_APPOINTMENT_NOT_FUTURE);
    }

    @Test
    public void execute_noConflict_passesReplacementAndOriginalToModelBeforeOneUpdate() throws Exception {
        Client owner = owner(ORIGINAL);
        ModelStub model = modelWith(owner);

        CommandResult result = command(ORIGINAL, MOVE_START).execute(model);

        Appointment expected = slot("13-12-2026", "16:00", "17:30");
        assertEquals(expected, model.checkedAppointment);
        assertEquals(owner, model.excludedClient);
        assertEquals(ORIGINAL, model.excludedAppointment);
        assertEquals(1, model.conflictCheckCount);
        assertEquals(1, model.updateCount);
        assertEquals(owner, model.targetClient);
        assertEquals(new ClientBuilder(owner).withAppointments(expected).build(), model.editedClient);
        assertEquals(List.of(ORIGINAL), owner.getAppointments());
        assertEquals("Rescheduled appointment: " + ALICE.getName() + " 13-12-2026 14:00 → 13-12-2026 16:00 et/17:30",
                result.getFeedbackToUser());
    }

    @Test
    public void execute_conflictReturnedByModel_reportsConflictWithoutUpdating() {
        ModelStub model = modelWith(owner(ORIGINAL));
        model.conflict = Optional.of(new Conflict(BENSON, slot("13-12-2026", "16:00", "17:00")));

        assertCommandFailure(command(ORIGINAL, MOVE_START), model,
                "Appointment overlaps with " + BENSON.getName() + "'s appointment: d/13-12-2026 t/16:00 et/17:00.");

        assertEquals(1, model.conflictCheckCount);
    }

    @Test
    public void equalsAndHashCode_compareTargetAndEditsButIgnoreClock() {
        EditAppointmentCommand command = command(ORIGINAL, MOVE_START);
        EditAppointmentCommand same = new EditAppointmentCommand(INDEX_FIRST_CLIENT, ORIGINAL.getDate(),
                ORIGINAL.getTime(), MOVE_START, Clock.systemUTC());
        assertEquals(command, same);
        assertEquals(command.hashCode(), same.hashCode());
        assertNotEquals(null, command);
        assertNotEquals(command, command(ORIGINAL, edits(null, "17:00", null)));
        assertNotEquals(command, new EditAppointmentCommand(INDEX_SECOND_CLIENT, ORIGINAL.getDate(),
                ORIGINAL.getTime(), MOVE_START));
        assertNotEquals(command, command(slot("14-12-2026", "14:00", null), MOVE_START));
        assertNotEquals(command, command(slot("13-12-2026", "15:00", null), MOVE_START));
    }

    @Test
    public void toStringMethod_returnsExpectedString() {
        EditAppointmentCommand command = command(ORIGINAL, MOVE_START);
        String expected = EditAppointmentCommand.class.getCanonicalName() + "{targetIndex=" + INDEX_FIRST_CLIENT
                + ", date=" + ORIGINAL.getDate() + ", time=" + ORIGINAL.getTime() + ", edits=" + MOVE_START + "}";

        assertEquals(expected, command.toString());
    }

    private static void assertReplacement(Appointment original, EditAppointmentDescriptor edits, Appointment expected)
            throws Exception {
        ModelStub model = modelWith(owner(original));
        command(original, edits).execute(model);
        assertEquals(List.of(expected), model.editedClient.getAppointments());
        assertEquals(1, model.updateCount);
        assertEquals(1, model.conflictCheckCount);
    }

    private static void assertCommandFailure(EditAppointmentCommand command, ModelStub model, String expectedMessage) {
        List<Client> originalClients = List.copyOf(model.getFilteredClientList());
        CommandException error = assertThrows(CommandException.class, () -> command.execute(model));
        assertEquals(expectedMessage, error.getMessage());
        assertEquals(0, model.updateCount);
        assertEquals(originalClients, model.getFilteredClientList());
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

    private static EditAppointmentDescriptor clearEnd(String date, String start) {
        return new EditAppointmentDescriptor(Optional.ofNullable(date).map(AppointmentDate::new),
                Optional.ofNullable(start).map(AppointmentTime::new), Optional.empty(), true);
    }

    private static Client owner(Appointment... appointments) {
        return new ClientBuilder(ALICE).withAppointments(appointments).build();
    }

    private static ModelStub modelWith(Client... clients) {
        return new ModelStub(List.of(clients));
    }

    /**
     * Supplies displayed clients and a predetermined conflict, and records calls without implementing model behavior.
     * Unexpected model operations fail immediately.
     */
    private static class ModelStub implements Model {
        private static final String UNEXPECTED_CALL = "This model operation should not be called by the command.";

        private final ObservableList<Client> clients;
        private Optional<Conflict> conflict = Optional.empty();
        private Appointment checkedAppointment;
        private Client excludedClient;
        private Appointment excludedAppointment;
        private Client targetClient;
        private Client editedClient;
        private int conflictCheckCount;
        private int updateCount;

        ModelStub(List<Client> clients) {
            this.clients = FXCollections.unmodifiableObservableList(FXCollections.observableArrayList(clients));
        }

        @Override
        public ObservableList<Client> getFilteredClientList() {
            return clients;
        }

        @Override
        public Optional<Conflict> findAppointmentConflict(Appointment candidate, Client excludedClient,
                Appointment excludedAppointment) {
            assertEquals(0, updateCount, "Conflict validation must precede any update");
            checkedAppointment = candidate;
            this.excludedClient = excludedClient;
            this.excludedAppointment = excludedAppointment;
            conflictCheckCount++;
            return conflict;
        }

        @Override
        public Optional<Conflict> findAppointmentConflict(Appointment candidate) {
            throw new AssertionError(UNEXPECTED_CALL);
        }

        @Override
        public void setClient(Client target, Client editedClient) {
            assertEquals(1, conflictCheckCount, "The proposed slot must be checked before updating");
            targetClient = target;
            this.editedClient = editedClient;
            updateCount++;
        }

        @Override
        public ReadOnlyUserPrefs getUserPrefs() {
            throw new AssertionError(UNEXPECTED_CALL);
        }

        @Override
        public GuiSettings getGuiSettings() {
            throw new AssertionError(UNEXPECTED_CALL);
        }

        @Override
        public void setGuiSettings(GuiSettings guiSettings) {
            throw new AssertionError(UNEXPECTED_CALL);
        }

        @Override
        public void setPawPals(ReadOnlyPawPals pawPals) {
            throw new AssertionError(UNEXPECTED_CALL);
        }

        @Override
        public ReadOnlyPawPals getPawPals() {
            throw new AssertionError(UNEXPECTED_CALL);
        }

        @Override
        public boolean hasClient(Client client) {
            throw new AssertionError(UNEXPECTED_CALL);
        }

        @Override
        public void deleteClient(Client target) {
            throw new AssertionError(UNEXPECTED_CALL);
        }

        @Override
        public void addClient(Client client) {
            throw new AssertionError(UNEXPECTED_CALL);
        }

        @Override
        public void updateFilteredClientList(Predicate<Client> predicate) {
            throw new AssertionError(UNEXPECTED_CALL);
        }
    }
}
