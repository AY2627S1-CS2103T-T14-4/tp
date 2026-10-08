package seedu.address.model.appointment;

import static java.util.Objects.requireNonNull;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;

/**
 * Represents an appointment's calendar date.
 * Guarantees: immutable; contains a valid date in DD-MM-YYYY format with a year from 0001 to 9999.
 */
public final class AppointmentDate {

    public static final String MESSAGE_CONSTRAINTS =
            "Appointment dates must be valid calendar dates in DD-MM-YYYY format, "
                    + "with a year from 0001 to 9999.";

    private static final String VALIDATION_REGEX = "[0-9]{2}-[0-9]{2}-[0-9]{4}";
    // uuuu represents the year directly, allowing strict resolution without an era.
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd-MM-uuuu")
            .withResolverStyle(ResolverStyle.STRICT);

    private final LocalDate value;

    /**
     * Constructs an {@code AppointmentDate} from a date in DD-MM-YYYY format.
     *
     * @throws NullPointerException if {@code date} is null.
     * @throws IllegalArgumentException if {@code date} is not a valid appointment date.
     */
    public AppointmentDate(String date) {
        value = parseDate(requireNonNull(date));
    }

    /**
     * Returns true if the string is a valid appointment date; returns false for null.
     */
    public static boolean isValidDate(String test) {
        if (test == null) {
            return false;
        }
        try {
            parseDate(test);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private static LocalDate parseDate(String date) {
        if (!date.matches(VALIDATION_REGEX)) {
            throw new IllegalArgumentException(MESSAGE_CONSTRAINTS);
        }
        LocalDate parsedDate;
        try {
            parsedDate = LocalDate.parse(date, FORMATTER);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException(MESSAGE_CONSTRAINTS, e);
        }
        if (parsedDate.getYear() == 0) {
            throw new IllegalArgumentException(MESSAGE_CONSTRAINTS);
        }
        return parsedDate;
    }

    /**
     * Returns the immutable calendar date for use in scheduling logic.
     */
    public LocalDate getValue() {
        return value;
    }

    @Override
    public String toString() {
        return value.format(FORMATTER);
    }

    @Override
    public boolean equals(Object other) {
        return other == this
                || (other instanceof AppointmentDate otherDate && value.equals(otherDate.value));
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }
}
