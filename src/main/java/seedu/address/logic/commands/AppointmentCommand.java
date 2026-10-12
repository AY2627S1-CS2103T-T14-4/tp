package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import seedu.address.commons.core.index.Index;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.appointment.Appointment;
import seedu.address.model.client.Client;

/**
 * Adds an appointment to a client identified by their displayed index, including after filtering.
 * Duplicate slots are checked within the selected client, regardless of appointment details.
 */
public class AppointmentCommand extends Command {
    public static final String COMMAND_WORD = "appt";
    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Adds an appointment to the client identified by their displayed index.\n"
            + "Parameters: c/CLIENT_INDEX d/DD-MM-YYYY t/HH:mm\n"
            + "The index must be positive and the combined date and time must be strictly in the future.\n"
            + "Example: " + COMMAND_WORD + " c/1 d/12-12-2026 t/12:34";
    public static final String MESSAGE_SUCCESS = "Added appointment: %1$s d/%2$s t/%3$s";
    public static final String MESSAGE_NOT_FUTURE = "Appointment date and time must be strictly in the future.";
    public static final String MESSAGE_DUPLICATE_APPOINTMENT =
            "This client already has an appointment at that date and time.";

    private final Index targetIndex;
    private final Appointment appointment;
    private final Clock clock;

    /**
     * Creates a command using the system clock and default time zone.
     * Whether the appointment is in the future is checked at execution.
     *
     * @throws NullPointerException if either argument is null; validation is delegated to the full constructor.
     */
    public AppointmentCommand(Index targetIndex, Appointment appointment) {
        this(targetIndex, appointment, Clock.systemDefaultZone());
    }

    /**
     * Creates a command using the supplied clock for execution-time validation.
     *
     * @throws NullPointerException if any argument is null.
     */
    AppointmentCommand(Index targetIndex, Appointment appointment, Clock clock) {
        requireAllNonNull(targetIndex, appointment, clock);
        this.targetIndex = targetIndex;
        this.appointment = appointment;
        this.clock = clock;
    }

    /**
     * Adds the appointment while preserving the client's other data and the current list filter.
     *
     * @throws NullPointerException if {@code model} is null.
     * @throws CommandException if the displayed index is invalid, the appointment is not strictly
     *     in the future, or the client already has the same date/time slot.
     */
    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        List<Client> displayedClients = model.getFilteredClientList();
        if (targetIndex.getZeroBased() >= displayedClients.size()) {
            throw new CommandException(Messages.MESSAGE_INVALID_CLIENT_DISPLAYED_INDEX);
        }
        LocalDateTime dateTime = LocalDateTime.of(appointment.getDate().getValue(), appointment.getTime().getValue());
        if (!dateTime.isAfter(LocalDateTime.now(clock))) {
            throw new CommandException(MESSAGE_NOT_FUTURE);
        }
        Client client = displayedClients.get(targetIndex.getZeroBased());
        if (client.hasAppointment(appointment)) {
            throw new CommandException(MESSAGE_DUPLICATE_APPOINTMENT);
        }
        model.setClient(client, client.copyWithAppointment(appointment));
        return new CommandResult(String.format(MESSAGE_SUCCESS,
                client.getName(), appointment.getDate(), appointment.getTime()));
    }

    /**
     * Compares the target index and full appointment value, including details.
     * The clock is an execution dependency and is excluded from value equality.
     */
    @Override
    public boolean equals(Object other) {
        return other == this || (other instanceof AppointmentCommand otherCommand
                && targetIndex.equals(otherCommand.targetIndex)
                && appointment.equals(otherCommand.appointment));
    }

    @Override
    public int hashCode() {
        return Objects.hash(targetIndex, appointment);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).add("targetIndex", targetIndex).add("appointment", appointment).toString();
    }
}
