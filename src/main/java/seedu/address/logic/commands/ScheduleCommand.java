package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.util.List;
import java.util.Objects;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.ClientDisplayMode;
import seedu.address.model.Model;
import seedu.address.model.appointment.AppointmentDate;
import seedu.address.model.client.Client;

/** Shows appointments for a date or the complete appointment history of one displayed client. */
public class ScheduleCommand extends Command {
    public static final String COMMAND_WORD = "schedule";
    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Shows active appointments on a date, or all appointments and completed sessions for one client.\n"
            + "Parameters: d/DATE or c/CLIENT_INDEX\n"
            + "Examples: " + COMMAND_WORD + " d/29-10-2026, " + COMMAND_WORD + " c/1";
    public static final String MESSAGE_DATE_SUCCESS = "Listed appointments for %1$s.";
    public static final String MESSAGE_CLIENT_SUCCESS = "Listed all appointments and completed sessions for %1$s.";

    private final AppointmentDate date;
    private final Index clientIndex;

    /** Creates a command that displays active appointments on {@code date}. */
    public ScheduleCommand(AppointmentDate date) {
        this.date = requireNonNull(date);
        clientIndex = null;
    }

    /** Creates a command that displays the complete appointment history of a displayed client. */
    public ScheduleCommand(Index clientIndex) {
        this.clientIndex = requireNonNull(clientIndex);
        date = null;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        if (date != null) {
            model.setClientDisplayMode(ClientDisplayMode.scheduleDate(date));
            model.updateFilteredClientList(client -> client.getAppointments().stream()
                    .anyMatch(appointment -> appointment.getDate().equals(date)));
            return new CommandResult(String.format(MESSAGE_DATE_SUCCESS, date), FeedbackType.NEUTRAL);
        }

        List<Client> displayedClients = model.getFilteredClientList();
        if (clientIndex.getZeroBased() >= displayedClients.size()) {
            throw new CommandException(Messages.MESSAGE_INVALID_CLIENT_DISPLAYED_INDEX);
        }
        Client selectedClient = displayedClients.get(clientIndex.getZeroBased());
        model.setClientDisplayMode(ClientDisplayMode.SCHEDULE_CLIENT);
        model.updateFilteredClientList(client -> client.isSameClient(selectedClient));
        return new CommandResult(String.format(MESSAGE_CLIENT_SUCCESS, selectedClient.getName()), FeedbackType.NEUTRAL);
    }

    @Override
    public boolean equals(Object other) {
        return other == this || (other instanceof ScheduleCommand otherCommand
                && Objects.equals(date, otherCommand.date) && Objects.equals(clientIndex, otherCommand.clientIndex));
    }

    @Override
    public int hashCode() {
        return Objects.hash(date, clientIndex);
    }
}
