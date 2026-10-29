package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.ClientDisplayMode;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.PawPals;
import seedu.address.model.UserPrefs;
import seedu.address.model.appointment.Appointment;
import seedu.address.model.appointment.AppointmentDate;
import seedu.address.model.appointment.AppointmentTime;
import seedu.address.model.client.Client;
import seedu.address.testutil.ClientBuilder;

public class ScheduleCommandTest {
    private static final AppointmentDate TARGET_DATE = new AppointmentDate("29-10-2026");
    private Client first;
    private Client second;
    private Model model;

    @BeforeEach
    public void setUp() {
        first = new ClientBuilder().withName("First Client").withAppointments(
                new Appointment(TARGET_DATE, new AppointmentTime("09:00")),
                new Appointment(new AppointmentDate("30-10-2026"), new AppointmentTime("10:00"))).build();
        second = new ClientBuilder().withName("Second Client").withEmail("second@example.com")
                .withPhone("91234567").build();
        PawPals pawPals = new PawPals();
        pawPals.setClients(List.of(first, second));
        model = new ModelManager(pawPals, new UserPrefs());
    }

    @Test
    public void execute_date_filtersAllStoredClientsAndSetsDateMode() throws Exception {
        model.updateFilteredClientList(client -> client.equals(second));

        CommandResult result = new ScheduleCommand(TARGET_DATE).execute(model);

        assertEquals(List.of(first), model.getFilteredClientList());
        assertEquals(ClientDisplayMode.scheduleDate(TARGET_DATE), model.clientDisplayModeProperty().getValue());
        assertEquals(String.format(ScheduleCommand.MESSAGE_DATE_SUCCESS, TARGET_DATE), result.getFeedbackToUser());
    }

    @Test
    public void execute_clientUsesDisplayedIndexAndSetsClientMode() throws Exception {
        model.updateFilteredClientList(client -> client.equals(second));

        CommandResult result = new ScheduleCommand(Index.fromOneBased(1)).execute(model);

        assertEquals(List.of(second), model.getFilteredClientList());
        assertEquals(ClientDisplayMode.SCHEDULE_CLIENT, model.clientDisplayModeProperty().getValue());
        assertEquals(String.format(ScheduleCommand.MESSAGE_CLIENT_SUCCESS, second.getName()),
                result.getFeedbackToUser());

        Client updatedClient = second.copyWithAppointment(
                new Appointment(TARGET_DATE, new AppointmentTime("11:00")));
        model.setClient(second, updatedClient);
        assertEquals(List.of(updatedClient), model.getFilteredClientList());
    }

    @Test
    public void execute_invalidDisplayedIndex_preservesView() {
        CommandException exception = assertThrows(CommandException.class, () ->
                new ScheduleCommand(Index.fromOneBased(3)).execute(model));

        assertEquals(Messages.MESSAGE_INVALID_CLIENT_DISPLAYED_INDEX, exception.getMessage());
        assertEquals(List.of(first, second), model.getFilteredClientList());
        assertEquals(ClientDisplayMode.NORMAL, model.clientDisplayModeProperty().getValue());
    }
}
