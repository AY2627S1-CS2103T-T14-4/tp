package seedu.address.logic;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import seedu.address.logic.parser.Prefix;
import seedu.address.model.appointment.Appointment;
import seedu.address.model.appointment.AppointmentConflictChecker.Conflict;
import seedu.address.model.client.Client;

/**
 * Container for user visible messages.
 */
public class Messages {

    public static final String MESSAGE_UNKNOWN_COMMAND = "Unknown command.";
    public static final String MESSAGE_INVALID_COMMAND_FORMAT = "Invalid command format!\n%1$s";
    public static final String MESSAGE_INVALID_CLIENT_DISPLAYED_INDEX = "The client index provided is invalid.";
    public static final String MESSAGE_UNKNOWN_PET = "This client has no pet named %1$s.";
    public static final String MESSAGE_CLIENTS_LISTED_OVERVIEW = "%1$d client(s) listed!";
    public static final String MESSAGE_DUPLICATE_FIELDS =
                "Multiple values specified for the following single-valued field(s): ";
    public static final String MESSAGE_APPOINTMENT_CONFLICT =
            "Appointment overlaps with %1$s's appointment: d/%2$s t/%3$s%4$s.";
    public static final String MESSAGE_APPOINTMENT_NOT_FOUND = "%1$s has no appointment at %2$s %3$s.";
    public static final String MESSAGE_APPOINTMENT_NOT_FUTURE =
            "Appointment date and time must be strictly in the future.";

    /**
     * Identifies a conflicting booking, including its end time when one is recorded.
     */
    public static String formatAppointmentConflict(Conflict conflict) {
        Appointment appointment = conflict.appointment();
        return String.format(MESSAGE_APPOINTMENT_CONFLICT, conflict.client().getName(), appointment.getDate(),
                appointment.getTime(), appointment.getEndTime().map(end -> " et/" + end).orElse(""));
    }

    /**
     * Returns an error message indicating the duplicate prefixes.
     */
    public static String getErrorMessageForDuplicatePrefixes(Prefix... duplicatePrefixes) {
        assert duplicatePrefixes.length > 0;

        Set<String> duplicateFields =
                Stream.of(duplicatePrefixes).map(Prefix::toString).collect(Collectors.toSet());

        return MESSAGE_DUPLICATE_FIELDS + String.join(" ", duplicateFields);
    }

    /**
     * Formats the {@code client} for display to the user.
     */
    public static String format(Client client) {
        final StringBuilder builder = new StringBuilder();
        builder.append(client.getName())
                .append("; Phone: ")
                .append(client.getPhone())
                .append("; Email: ")
                .append(client.getEmail())
                .append("; Address: ")
                .append(client.getAddress())
                .append("; Tags: ");
        client.getTags().forEach(builder::append);
        return builder.toString();
    }

}
