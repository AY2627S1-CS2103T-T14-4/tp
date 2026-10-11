package seedu.address.model.appointment;

import static java.util.Objects.requireNonNull;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;

/**
 * Represents an appointment's time of day.
 * Guarantees: immutable; contains a valid time in 24-hour HH:mm format.
 */
public final class AppointmentTime {

    public static final String MESSAGE_CONSTRAINTS =
            "Appointment times must be valid times in 24-hour HH:mm format, from 00:00 to 23:59.";

    private static final String VALIDATION_REGEX = "[0-9]{2}:[0-9]{2}";
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("HH:mm")
            .withResolverStyle(ResolverStyle.STRICT);

    private final LocalTime value;

    /**
     * Constructs an {@code AppointmentTime} from a time in 24-hour HH:mm format.
     *
     * @throws NullPointerException if {@code time} is null.
     * @throws IllegalArgumentException if {@code time} is not a valid appointment time.
     */
    public AppointmentTime(String time) {
        value = parseTime(requireNonNull(time));
    }

    /**
     * Returns true if the string is a valid appointment time; returns false for null.
     */
    public static boolean isValidTime(String test) {
        if (test == null) {
            return false;
        }
        try {
            parseTime(test);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private static LocalTime parseTime(String time) {
        if (!time.matches(VALIDATION_REGEX)) {
            throw new IllegalArgumentException(MESSAGE_CONSTRAINTS);
        }
        try {
            return LocalTime.parse(time, FORMATTER);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException(MESSAGE_CONSTRAINTS, e);
        }
    }

    /**
     * Returns the immutable time of day for use in scheduling logic.
     */
    public LocalTime getValue() {
        return value;
    }

    @Override
    public String toString() {
        return value.format(FORMATTER);
    }

    @Override
    public boolean equals(Object other) {
        return other == this
                || (other instanceof AppointmentTime otherTime && value.equals(otherTime.value));
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }
}
