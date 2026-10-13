package seedu.address.model.appointment.exceptions;

/**
 * Signals that an appointment's date and time already exist for the client.
 */
public class DuplicateAppointmentException extends RuntimeException {
    public DuplicateAppointmentException() {
        super("Operation would result in a duplicate appointment for the client");
    }
}
