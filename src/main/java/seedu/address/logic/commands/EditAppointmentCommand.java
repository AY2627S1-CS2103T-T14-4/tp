package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import seedu.address.commons.core.index.Index;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.appointment.Appointment;
import seedu.address.model.appointment.AppointmentConflictChecker.Conflict;
import seedu.address.model.appointment.AppointmentDate;
import seedu.address.model.appointment.AppointmentTime;
import seedu.address.model.client.Client;

/**
 * Reschedules an active appointment identified by its owner's displayed index, date and start time.
 */
public class EditAppointmentCommand extends Command {
    public static final String COMMAND_WORD = "editappt";
    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Reschedules an appointment for the client identified by their displayed index.\n"
            + "Parameters: c/CLIENT_INDEX d/DD-MM-YYYY t/HH:mm [nd/DD-MM-YYYY] [ns/HH:mm] [ne/HH:mm]\n"
            + "Provide at least one new value. The resulting start must be strictly in the future.\n"
            + "Without ne/, an existing duration is preserved; appointments must end on the same day.\n"
            + "Linked pets and booking details are kept. Overlaps with other active bookings are rejected.\n"
            + "Example: " + COMMAND_WORD + " c/1 d/12-12-2026 t/14:00 ns/16:00";
    public static final String MESSAGE_NOT_EDITED = "At least one of nd/, ns/ or ne/ must be provided.";
    public static final String MESSAGE_END_AFTER_DAY =
            "The appointment would end after 23:59. Give a new end time with ne/.";
    public static final String MESSAGE_SUCCESS = "Rescheduled appointment: %1$s %2$s %3$s → %4$s %5$s";

    private static final long MINUTES_PER_DAY = ChronoUnit.DAYS.getDuration().toMinutes();
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    private final Index targetIndex;
    private final AppointmentDate date;
    private final AppointmentTime time;
    private final EditAppointmentDescriptor edits;
    private final Clock clock;

    /**
     * Creates a rescheduling command using the system clock and default time zone.
     *
     * @param targetIndex The client's index in the displayed list.
     * @param date The original appointment's date, which may be in the past.
     * @param time The original appointment's start time.
     * @param edits The replacement values; omitted fields are preserved.
     * @throws NullPointerException if any argument is null.
     */
    public EditAppointmentCommand(Index targetIndex, AppointmentDate date, AppointmentTime time,
            EditAppointmentDescriptor edits) {
        this(targetIndex, date, time, edits, Clock.systemDefaultZone());
    }

    /**
     * Creates a rescheduling command using the supplied clock for execution-time validation.
     * All arguments must be non-null.
     */
    EditAppointmentCommand(Index targetIndex, AppointmentDate date, AppointmentTime time,
            EditAppointmentDescriptor edits, Clock clock) {
        requireAllNonNull(targetIndex, date, time, edits, clock);
        this.targetIndex = targetIndex;
        this.date = date;
        this.time = time;
        this.edits = edits;
        this.clock = clock;
    }

    /**
     * Replaces only the selected appointment after validating the proposed slot against all active bookings.
     * Preserves the current filter and all unrelated data.
     *
     * @throws NullPointerException if {@code model} is null.
     * @throws CommandException if the target is missing or the proposed slot is invalid or conflicts with a booking.
     */
    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        Client client = resolveClient(model);
        Appointment original = client.getAppointment(date, time).orElseThrow(() -> new CommandException(
                String.format(CancelCommand.MESSAGE_APPOINTMENT_NOT_FOUND, client.getName(), date, time)));
        Appointment replacement = createReplacement(original);
        validateFutureStart(replacement);
        Optional<Conflict> conflict = model.findAppointmentConflict(replacement, client, original);
        if (conflict.isPresent()) {
            throw new CommandException(Messages.formatAppointmentConflict(conflict.get()));
        }
        model.setClient(client, client.withAppointmentReplaced(date, time, replacement));
        return new CommandResult(String.format(MESSAGE_SUCCESS, client.getName(), date, time,
                replacement.getDate(), replacement.getTime())
                + replacement.getEndTime().map(end -> " et/" + end).orElse(""));
    }

    /**
     * Resolves the selected client against the displayed list without changing its filter.
     */
    private Client resolveClient(Model model) throws CommandException {
        List<Client> displayedClients = model.getFilteredClientList();
        if (targetIndex.getZeroBased() >= displayedClients.size()) {
            throw new CommandException(Messages.MESSAGE_INVALID_CLIENT_DISPLAYED_INDEX);
        }
        return displayedClients.get(targetIndex.getZeroBased());
    }

    /**
     * Builds a validated appointment while retaining its booking details and pet links.
     */
    private Appointment createReplacement(Appointment original) throws CommandException {
        AppointmentDate newDate = edits.date().orElse(original.getDate());
        AppointmentTime newStart = edits.start().orElse(original.getTime());
        Optional<AppointmentTime> newEnd = resolveEndTime(original, newStart);
        try {
            return new Appointment(newDate, newStart, newEnd, original.getDetails(), original.getPetNames());
        } catch (IllegalArgumentException e) {
            throw new CommandException(e.getMessage(), e);
        }
    }

    /**
     * Uses an explicit end time or preserves the original duration without wrapping past midnight.
     */
    private Optional<AppointmentTime> resolveEndTime(Appointment original, AppointmentTime newStart)
            throws CommandException {
        if (edits.end().isPresent()) {
            return edits.end();
        }
        if (original.getEndTime().isEmpty()) {
            return Optional.empty();
        }
        long duration = Duration.between(original.getTime().getValue(),
                original.getEndTime().orElseThrow().getValue()).toMinutes();
        long endMinute = ChronoUnit.MINUTES.between(LocalTime.MIDNIGHT, newStart.getValue()) + duration;
        if (endMinute >= MINUTES_PER_DAY) {
            throw new CommandException(MESSAGE_END_AFTER_DAY);
        }
        String end = LocalTime.MIDNIGHT.plusMinutes(endMinute).format(TIME_FORMATTER);
        return Optional.of(new AppointmentTime(end));
    }

    /**
     * Requires the resulting start to be strictly after the current local date and time.
     */
    private void validateFutureStart(Appointment replacement) throws CommandException {
        LocalDateTime start = LocalDateTime.of(replacement.getDate().getValue(), replacement.getTime().getValue());
        if (!start.isAfter(LocalDateTime.now(clock))) {
            throw new CommandException(AppointmentCommand.MESSAGE_NOT_FUTURE);
        }
    }

    @Override
    public boolean equals(Object other) {
        return other == this || (other instanceof EditAppointmentCommand otherCommand
                && targetIndex.equals(otherCommand.targetIndex)
                && date.equals(otherCommand.date)
                && time.equals(otherCommand.time)
                && edits.equals(otherCommand.edits));
    }

    @Override
    public int hashCode() {
        return Objects.hash(targetIndex.getZeroBased(), date, time, edits);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).add("targetIndex", targetIndex).add("date", date)
                .add("time", time).add("edits", edits).toString();
    }

    /**
     * Immutable replacement values for an appointment; empty optionals preserve existing values.
     *
     * @param date The new date, if supplied.
     * @param start The new start time, if supplied.
     * @param end The new end time, if supplied.
     */
    public record EditAppointmentDescriptor(Optional<AppointmentDate> date, Optional<AppointmentTime> start,
            Optional<AppointmentTime> end) {
        /**
         * Creates a descriptor containing at least one edit.
         *
         * @throws NullPointerException if any optional is null.
         * @throws IllegalArgumentException if no edit is supplied.
         */
        public EditAppointmentDescriptor {
            requireAllNonNull(date, start, end);
            if (date.isEmpty() && start.isEmpty() && end.isEmpty()) {
                throw new IllegalArgumentException(MESSAGE_NOT_EDITED);
            }
        }
    }
}
