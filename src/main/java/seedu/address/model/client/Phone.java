package seedu.address.model.client;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

/**
 * Represents a Client's phone number in the PawPals data.
 * Guarantees: immutable; is valid as declared in {@link #isValidPhone(String)}
 */
public class Phone {


    public static final String MESSAGE_CONSTRAINTS =
            "Phone numbers should contain 3 to 15 digits, with an optional leading '+' and spaces allowed.";
    public static final String VALIDATION_REGEX = "\\d{3,15}";
    public final String value;

    /**
     * Constructs a {@code Phone}.
     *
     * @param phone A valid phone number.
     */
    public Phone(String phone) {
        requireNonNull(phone);
        String normalizedPhone = normalize(phone);
        checkArgument(isValidPhone(normalizedPhone), MESSAGE_CONSTRAINTS);
        value = normalizedPhone;
    }

    /**
     * Trims a phone number and collapses repeated whitespace to one space.
     */
    private static String normalize(String phone) {
        return phone.trim().replaceAll("\\s+", " ");
    }

    /**
     * Returns true if a given string is a valid phone number.
     */
    public static boolean isValidPhone(String test) {
        String normalizedPhone = normalize(test);
        String digitsOnly = digitsOnly(normalizedPhone);
        return digitsOnly.matches(VALIDATION_REGEX)
                && normalizedPhone.matches("\\+?[0-9]+(?: [0-9]+)*");
    }

    /**
     * Returns the digits of a normalized phone number without its optional plus sign or spaces.
     */
    private static String digitsOnly(String normalizedPhone) {
        return normalizedPhone.startsWith("+")
                ? normalizedPhone.substring(1).replace(" ", "")
                : normalizedPhone.replace(" ", "");
    }

    @Override
    public String toString() {
        return value;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof Phone otherPhone)) {
            return false;
        }

        return digitsOnly(value).equals(digitsOnly(otherPhone.value));
    }

    @Override
    public int hashCode() {
        return digitsOnly(value).hashCode();
    }

}
