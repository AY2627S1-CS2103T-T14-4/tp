package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.logic.commands.CommandTestUtil.showClientAtIndex;
import static seedu.address.testutil.TypicalClients.getTypicalPawPals;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_CLIENT;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_CLIENT;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.appointment.Appointment;
import seedu.address.model.appointment.AppointmentDate;
import seedu.address.model.appointment.AppointmentTime;
import seedu.address.model.client.Client;

public class AppointmentCommandTest {
    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-12-12T04:00:00Z"),
            ZoneId.of("Asia/Singapore"));
    private final Model model = new ModelManager(getTypicalPawPals(), new UserPrefs());

    @Test
    public void constructor_nullArguments_throwsNullPointerException() {
        Appointment appointment = createAppointment("13-12-2026", "00:00");
        assertThrows(NullPointerException.class, () -> new AppointmentCommand(null, appointment));
        assertThrows(NullPointerException.class, () -> new AppointmentCommand(INDEX_FIRST_CLIENT, null));
        assertThrows(NullPointerException.class, () -> new AppointmentCommand(INDEX_FIRST_CLIENT, appointment, null));
    }

    @Test
    public void execute_nullModel_throwsNullPointerException() {
        AppointmentCommand command = new AppointmentCommand(INDEX_FIRST_CLIENT,
                createAppointment("13-12-2026", "00:00"), CLOCK);
        assertThrows(NullPointerException.class, () -> command.execute(null));
    }

    @Test
    public void execute_emptyClientList_doesNotChangeData() {
        Model emptyModel = new ModelManager();
        assertCommandFailure(new AppointmentCommand(INDEX_FIRST_CLIENT,
                createAppointment("13-12-2026", "00:00"), CLOCK), emptyModel,
                Messages.MESSAGE_INVALID_CLIENT_DISPLAYED_INDEX);
    }

    private static Appointment createAppointment(String date, String time) {
        return new Appointment(new AppointmentDate(date), new AppointmentTime(time));
    }

    @Test
    public void execute_futureToday_preservesOtherData() {
        Appointment appointment = createAppointment("12-12-2026", "12:01");
        Client withoutAppointments = model.getFilteredClientList().get(0);
        Client original = withoutAppointments.copyWithAppointment(createAppointment("14-12-2026", "14:00"));
        model.setClient(withoutAppointments, original);
        Model expected = new ModelManager(model.getPawPals(), new UserPrefs());
        expected.setClient(original, original.copyWithAppointment(appointment));
        assertCommandSuccess(new AppointmentCommand(INDEX_FIRST_CLIENT, appointment, CLOCK), model,
                String.format(AppointmentCommand.MESSAGE_SUCCESS, original.getName(),
                        appointment.getDate(), appointment.getTime()), expected);
    }

    @Test
    public void execute_filteredList_updatesDisplayedClientAndPreservesFilter() throws Exception {
        Client selected = model.getFilteredClientList().get(1);
        Client untouched = model.getFilteredClientList().get(0);
        showClientAtIndex(model, INDEX_SECOND_CLIENT);
        Appointment appointment = createAppointment("13-12-2026", "00:00");
        new AppointmentCommand(INDEX_FIRST_CLIENT, appointment, CLOCK).execute(model);
        assertEquals(selected.copyWithAppointment(appointment), model.getFilteredClientList().get(0));
        assertEquals(1, model.getFilteredClientList().size());
        assertEquals(untouched, model.getPawPals().getClientList().get(0));
    }

    @Test
    public void execute_withEndTime_preservesOtherDataAndReportsEndTime() {
        Client original = model.getFilteredClientList().getFirst();
        Client withAppointment = original.copyWithAppointment(createAppointment("14-12-2026", "14:00"));
        model.setClient(original, withAppointment);
        Appointment appointment = createAppointment("12-12-2026", "12:01")
                .withEndTime(new AppointmentTime("13:00"));
        Model expected = new ModelManager(model.getPawPals(), new UserPrefs());
        expected.setClient(withAppointment, withAppointment.copyWithAppointment(appointment));

        assertCommandSuccess(new AppointmentCommand(INDEX_FIRST_CLIENT, appointment, CLOCK), model,
                "Added appointment: " + original.getName() + " d/12-12-2026 t/12:01 et/13:00", expected);
    }

    @Test
    public void execute_withEndTimeFilteredList_updatesOnlyDisplayedClient() throws Exception {
        Client selected = model.getFilteredClientList().get(1);
        Client untouched = model.getFilteredClientList().getFirst();
        showClientAtIndex(model, INDEX_SECOND_CLIENT);
        Appointment appointment = createAppointment("13-12-2026", "00:00")
                .withEndTime(new AppointmentTime("00:01"));

        new AppointmentCommand(INDEX_FIRST_CLIENT, appointment, CLOCK).execute(model);

        assertEquals(List.of(selected.copyWithAppointment(appointment)), model.getFilteredClientList());
        assertEquals(untouched, model.getPawPals().getClientList().getFirst());
        assertCommandFailure(new AppointmentCommand(INDEX_SECOND_CLIENT, appointment, CLOCK), model,
                Messages.MESSAGE_INVALID_CLIENT_DISPLAYED_INDEX);
    }

    @Test
    public void execute_pastStartWithEndTime_addsAppointmentWithNotice() {
        for (Appointment appointment : new Appointment[] {
            createAppointment("11-12-2026", "12:00").withEndTime(new AppointmentTime("13:00")),
            createAppointment("12-12-2026", "11:59").withEndTime(new AppointmentTime("13:00"))}) {
            assertAppointmentAdded(appointment, " et/13:00\n" + AppointmentCommand.MESSAGE_PAST_APPOINTMENT);
        }
    }

    @Test
    public void execute_presentStartWithEndTime_addsWithoutPastNotice() {
        assertAppointmentAdded(createAppointment("12-12-2026", "12:00")
                .withEndTime(new AppointmentTime("13:00")), " et/13:00");
    }

    @Test
    public void execute_sameStartWithDifferentEndTime_doesNotChangeData() {
        Appointment appointment = createAppointment("13-12-2026", "12:00");
        Client original = model.getFilteredClientList().getFirst();
        model.setClient(original, original.copyWithAppointment(
                appointment.withEndTime(new AppointmentTime("13:00"))));

        assertCommandFailure(new AppointmentCommand(INDEX_FIRST_CLIENT,
                appointment.withEndTime(new AppointmentTime("14:00")), CLOCK), model,
                "Appointment overlaps with " + original.getName()
                        + "'s appointment: d/13-12-2026 t/12:00 et/13:00.");
    }

    @Test
    public void execute_invalidDisplayedIndex_doesNotChangeData() {
        Appointment appointment = createAppointment("13-12-2026", "00:00");
        assertCommandFailure(new AppointmentCommand(Index.fromOneBased(100), appointment, CLOCK), model,
                Messages.MESSAGE_INVALID_CLIENT_DISPLAYED_INDEX);
        showClientAtIndex(model, INDEX_SECOND_CLIENT);
        assertCommandFailure(new AppointmentCommand(INDEX_SECOND_CLIENT, appointment, CLOCK), model,
                Messages.MESSAGE_INVALID_CLIENT_DISPLAYED_INDEX);
    }

    @Test
    public void execute_pastStart_addsAppointmentWithNotice() {
        for (Appointment appointment : new Appointment[] {createAppointment("11-12-2026", "23:59"),
            createAppointment("12-12-2026", "11:59"), createAppointment("01-01-0001", "00:00")}) {
            assertAppointmentAdded(appointment, "\n" + AppointmentCommand.MESSAGE_PAST_APPOINTMENT);
        }
    }

    @Test
    public void execute_presentStart_addsWithoutPastNotice() {
        assertAppointmentAdded(createAppointment("12-12-2026", "12:00"), "");
    }

    private void assertAppointmentAdded(Appointment appointment, String feedbackSuffix) {
        Model actual = new ModelManager(getTypicalPawPals(), new UserPrefs());
        Client original = actual.getFilteredClientList().getFirst();
        Model expected = new ModelManager(actual.getPawPals(), new UserPrefs());
        expected.setClient(original, original.copyWithAppointment(appointment));
        String feedback = String.format(AppointmentCommand.MESSAGE_SUCCESS, original.getName(),
                appointment.getDate(), appointment.getTime()) + feedbackSuffix;
        assertCommandSuccess(new AppointmentCommand(INDEX_FIRST_CLIENT, appointment, CLOCK), actual,
                feedback, expected);
    }

    @Test
    public void execute_duplicateWithDifferentDetails_doesNotChangeData() {
        Appointment appointment = createAppointment("13-12-2026", "00:00");
        Client original = model.getFilteredClientList().get(0);
        model.setClient(original, original.copyWithAppointment(
                new Appointment(appointment.getDate(), appointment.getTime(), "Existing details")));
        assertCommandFailure(new AppointmentCommand(INDEX_FIRST_CLIENT, appointment, CLOCK), model,
                "Appointment overlaps with " + original.getName() + "'s appointment: d/13-12-2026 t/00:00.");
        assertCommandFailure(new AppointmentCommand(INDEX_FIRST_CLIENT,
                new Appointment(appointment.getDate(), appointment.getTime(), "Existing details"), CLOCK), model,
                "Appointment overlaps with " + original.getName() + "'s appointment: d/13-12-2026 t/00:00.");
    }

    @Test
    public void execute_sameSlotForDifferentClients_rejectsWithoutChangingData() throws Exception {
        Appointment appointment = createAppointment("13-12-2026", "00:00");
        new AppointmentCommand(INDEX_FIRST_CLIENT, appointment, CLOCK).execute(model);
        assertCommandFailure(new AppointmentCommand(INDEX_SECOND_CLIENT, appointment, CLOCK), model,
                "Appointment overlaps with " + model.getPawPals().getClientList().getFirst().getName()
                        + "'s appointment: d/13-12-2026 t/00:00.");
    }

    @Test
    public void equals_comparesIndexAndAppointment() {
        Appointment appointment = createAppointment("13-12-2026", "00:00");
        AppointmentCommand command = new AppointmentCommand(INDEX_FIRST_CLIENT, appointment);
        assertEquals(command, new AppointmentCommand(INDEX_FIRST_CLIENT, appointment));
        assertEquals(command, command);
        assertEquals(command, new AppointmentCommand(INDEX_FIRST_CLIENT, appointment, CLOCK));
        assertEquals(command.hashCode(), new AppointmentCommand(INDEX_FIRST_CLIENT, appointment, CLOCK).hashCode());
        AppointmentCommand sameValueWithSeparateIndex = new AppointmentCommand(Index.fromOneBased(1), appointment);
        assertEquals(command, sameValueWithSeparateIndex);
        assertEquals(command.hashCode(), sameValueWithSeparateIndex.hashCode());
        assertNotEquals(command, new AppointmentCommand(INDEX_SECOND_CLIENT, appointment));
        assertNotEquals(command, new AppointmentCommand(INDEX_FIRST_CLIENT, createAppointment("14-12-2026", "00:00")));
        assertNotEquals(command, new AppointmentCommand(INDEX_FIRST_CLIENT,
                new Appointment(appointment.getDate(), appointment.getTime(), "Different details")));
        assertNotEquals(command, null);
        assertNotEquals(command, "appt");
    }
}
