package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import seedu.address.commons.core.index.Index;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.appointment.Appointment;
import seedu.address.model.appointment.AppointmentConflictChecker.Conflict;
import seedu.address.model.client.Client;
import seedu.address.model.pet.PetName;

/**
 * Adds an appointment to a client identified by their displayed index, including after filtering.
 * Overlaps are rejected across all clients' active appointments, including clients hidden by filtering.
 */
public class AppointmentCommand extends Command {
    public static final String COMMAND_WORD = "appt";
    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Adds an appointment to the client identified by their displayed index.\n"
            + "Parameters: c/CLIENT_INDEX d/DD-MM-YYYY t/HH:mm [et/HH:mm] [pn/PET_NAME]...\n"
            + "The index must be positive and the combined date and time must be strictly in the future.\n"
            + "The optional end time must be after the start time on the same date.\n"
            + "Each pet must belong to the selected client. Repeated references to the same pet are ignored.\n"
            + "Appointments must not overlap any client's active bookings. Adjacent appointments are allowed.\n"
            + "Example: " + COMMAND_WORD + " c/1 d/12-12-2026 t/12:34 et/13:34 pn/Milo pn/Luna";
    public static final String MESSAGE_SUCCESS = "Added appointment: %1$s d/%2$s t/%3$s";

    private final Index targetIndex;
    private final Appointment appointment;
    private final Clock clock;

    /**
     * Creates a command using the system clock and default time zone.
     * Whether the appointment is in the future is checked at execution.
     *
     * @param targetIndex The client's index in the currently displayed list.
     * @param appointment The appointment to add.
     * @throws NullPointerException if either argument is null; validation is delegated to the full constructor.
     */
    public AppointmentCommand(Index targetIndex, Appointment appointment) {
        this(targetIndex, appointment, Clock.systemDefaultZone());
    }

    /**
     * Creates a command using the supplied clock for execution-time validation.
     *
     * @param targetIndex The client's index in the currently displayed list.
     * @param appointment The appointment to add.
     * @param clock The clock providing the current time and local time zone.
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
     * @param model The model containing the displayed clients.
     * @return Feedback identifying the client, times and linked pets if supplied.
     * @throws NullPointerException if {@code model} is null.
     * @throws CommandException if the displayed index is invalid, the appointment is not strictly
     *     in the future, a linked pet does not belong to the client, or the appointment overlaps an active booking.
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
            throw new CommandException(Messages.MESSAGE_APPOINTMENT_NOT_FUTURE);
        }
        Client client = displayedClients.get(targetIndex.getZeroBased());
        Appointment resolvedAppointment = resolvePetNames(client);
        Optional<Conflict> conflict = model.findAppointmentConflict(resolvedAppointment);
        if (conflict.isPresent()) {
            throw new CommandException(Messages.formatAppointmentConflict(conflict.get()));
        }
        model.setClient(client, client.copyWithAppointment(resolvedAppointment));
        return new CommandResult(String.format(MESSAGE_SUCCESS,
                client.getName(), appointment.getDate(), appointment.getTime())
                + appointment.getEndTime().map(endTime -> " et/" + endTime).orElse("")
                + resolvedAppointment.getPetNames().stream().map(name -> " pn/" + name).collect(Collectors.joining()));
    }

    /**
     * Resolves requested names within the selected client and retains each recorded pet name once, in input order.
     * All references are validated before any model update; omitting pet names does not infer the client's pets.
     */
    private Appointment resolvePetNames(Client client) throws CommandException {
        Set<PetName> resolvedNames = new LinkedHashSet<>();
        for (PetName requestedName : appointment.getPetNames()) {
            PetName recordedName = client.getPet(requestedName).orElseThrow(() -> new CommandException(
                    String.format(Messages.MESSAGE_UNKNOWN_PET, requestedName))).getName();
            resolvedNames.add(recordedName);
        }
        return appointment.withPetNames(resolvedNames);
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
        return Objects.hash(targetIndex.getZeroBased(), appointment);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).add("targetIndex", targetIndex).add("appointment", appointment).toString();
    }
}
