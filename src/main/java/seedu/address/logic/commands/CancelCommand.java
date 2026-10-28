package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.util.List;
import java.util.Objects;

import seedu.address.commons.core.index.Index;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.appointment.Appointment;
import seedu.address.model.appointment.AppointmentDate;
import seedu.address.model.appointment.AppointmentTime;
import seedu.address.model.client.Client;

/**
 * Cancels an appointment identified by its client's displayed index, date and start time.
 */
public class CancelCommand extends Command {
    public static final String COMMAND_WORD = "cancel";
    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Cancels an appointment for the client identified by their displayed index.\n"
            + "Parameters: c/CLIENT_INDEX d/DD-MM-YYYY t/HH:mm\n"
            + "Use the appointment's date and start time. Past and future appointments can be cancelled.\n"
            + "Example: " + COMMAND_WORD + " c/1 d/12-12-2026 t/14:00";
    public static final String MESSAGE_SUCCESS = "Cancelled appointment: %1$s d/%2$s t/%3$s";
    public static final String MESSAGE_END_TIME_NOT_SUPPORTED =
            "cancel does not accept et/. Remove et/ and use the appointment's date and start time.";

    private final Index targetIndex;
    private final AppointmentDate date;
    private final AppointmentTime time;

    /**
     * Creates a command targeting the appointment at {@code date} and {@code time}.
     *
     * @param targetIndex The client's index in the currently displayed list.
     * @param date The appointment's date, which may be in the past.
     * @param time The appointment's start time.
     * @throws NullPointerException if any argument is null.
     */
    public CancelCommand(Index targetIndex, AppointmentDate date, AppointmentTime time) {
        requireAllNonNull(targetIndex, date, time);
        this.targetIndex = targetIndex;
        this.date = date;
        this.time = time;
    }

    /**
     * Removes only the selected appointment, preserving the client's other data and current filter.
     *
     * @throws NullPointerException if {@code model} is null.
     * @throws CommandException if the displayed client index is invalid or the appointment does not exist.
     */
    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        List<Client> displayedClients = model.getFilteredClientList();
        if (targetIndex.getZeroBased() >= displayedClients.size()) {
            throw new CommandException(Messages.MESSAGE_INVALID_CLIENT_DISPLAYED_INDEX);
        }
        Client client = displayedClients.get(targetIndex.getZeroBased());
        Appointment appointment = client.getAppointment(date, time).orElseThrow(() -> new CommandException(
                String.format(Messages.MESSAGE_APPOINTMENT_NOT_FOUND, client.getName(), date, time)));
        model.setClient(client, client.withoutAppointment(date, time));
        return new CommandResult(String.format(MESSAGE_SUCCESS,
                client.getName(), appointment.getDate(), appointment.getTime()));
    }

    @Override
    public boolean equals(Object other) {
        return other == this || (other instanceof CancelCommand otherCommand
                && targetIndex.equals(otherCommand.targetIndex)
                && date.equals(otherCommand.date)
                && time.equals(otherCommand.time));
    }

    @Override
    public int hashCode() {
        return Objects.hash(targetIndex.getZeroBased(), date, time);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).add("targetIndex", targetIndex).add("date", date).add("time", time).toString();
    }
}
