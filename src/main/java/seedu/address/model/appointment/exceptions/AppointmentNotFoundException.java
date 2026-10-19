package seedu.address.model.appointment.exceptions;

/**
 * Signals that the client has no appointment at the given date and time.
 */
public class AppointmentNotFoundException extends RuntimeException {
    public AppointmentNotFoundException() {
        super("The client has no appointment at that date and time");
    }
}
