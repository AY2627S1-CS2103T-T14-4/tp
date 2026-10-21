package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.time.Clock;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import seedu.address.commons.core.index.Index;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.appointment.Appointment;
import seedu.address.model.appointment.AppointmentDate;
import seedu.address.model.appointment.AppointmentTime;
import seedu.address.model.client.Client;
import seedu.address.model.history.SessionNotes;

/**
 * Moves a displayed client's active appointment into completed-session history with visit notes.
 */
public class DoneCommand extends Command {
    public static final String COMMAND_WORD = "done";
    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Completes an appointment for the client identified by their displayed index.\n"
            + "Parameters: c/CLIENT_INDEX d/DD-MM-YYYY t/HH:mm nt/NOTES\n"
            + "Use the appointment's start time. It must have started; its planned end need not have passed.\n"
            + "Session notes must contain 1 to 200 characters after trimming.\n"
            + "Example: " + COMMAND_WORD + " c/1 d/12-12-2026 t/14:00 nt/Teddy cut, short ears";
    public static final String MESSAGE_SUCCESS = "Completed session: %1$s d/%2$s t/%3$s";
    public static final String MESSAGE_APPOINTMENT_NOT_FOUND = "%1$s has no active appointment at %2$s %3$s.";
    public static final String MESSAGE_ALREADY_COMPLETED = "%1$s already has a completed session at %2$s %3$s.";

    private final Index targetIndex;
    private final AppointmentDate date;
    private final AppointmentTime time;
    private final SessionNotes notes;
    private final Clock clock;

    /**
     * Creates a completion command using the system clock and local time zone.
     */
    public DoneCommand(Index targetIndex, AppointmentDate date, AppointmentTime time, SessionNotes notes) {
        this(targetIndex, date, time, notes, Clock.systemDefaultZone());
    }

    /**
     * Creates a completion command with the supplied clock for eligibility and logging time.
     */
    DoneCommand(Index targetIndex, AppointmentDate date, AppointmentTime time, SessionNotes notes, Clock clock) {
        requireAllNonNull(targetIndex, date, time, notes, clock);
        this.targetIndex = targetIndex;
        this.date = date;
        this.time = time;
        this.notes = notes;
        this.clock = clock;
    }

    /**
     * Records a complete immutable snapshot and removes the active booking in one model update.
     * Preserves the current filter, all other client data and previous history.
     *
     * @throws NullPointerException if {@code model} is null.
     * @throws CommandException if the displayed index is invalid, the appointment is missing or already completed,
     *     or the appointment has not started.
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
                String.format(client.hasCompletedAppointment(date, time)
                        ? MESSAGE_ALREADY_COMPLETED : MESSAGE_APPOINTMENT_NOT_FOUND, client.getName(), date, time)));
        Client completed;
        try {
            completed = client.completeAppointment(date, time, notes, clock);
        } catch (IllegalArgumentException e) {
            throw new CommandException(e.getMessage(), e);
        }
        model.setClient(client, completed);
        return new CommandResult(String.format(MESSAGE_SUCCESS, client.getName(), date, time)
                + appointment.getEndTime().map(end -> " et/" + end).orElse("")
                + appointment.getPetNames().stream().map(name -> " pn/" + name).collect(Collectors.joining())
                + " nt/" + notes);
    }

    @Override
    public boolean equals(Object other) {
        return other == this || (other instanceof DoneCommand otherCommand
                && targetIndex.equals(otherCommand.targetIndex)
                && date.equals(otherCommand.date)
                && time.equals(otherCommand.time)
                && notes.equals(otherCommand.notes));
    }

    @Override
    public int hashCode() {
        return Objects.hash(targetIndex.getZeroBased(), date, time, notes);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).add("targetIndex", targetIndex).add("date", date).add("time", time)
                .add("notes", notes).toString();
    }
}
