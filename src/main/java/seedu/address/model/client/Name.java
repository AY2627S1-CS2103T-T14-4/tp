package seedu.address.model.client;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

import java.util.Locale;

/**
 * Represents a Client's name in the address book.
 * Guarantees: immutable; is valid as declared in {@link #isValidName(String)}
 */
public class Name {

    public static final String MESSAGE_CONSTRAINTS =
            "Names should contain only letters, spaces, apostrophes and hyphens, and should be between 1 and 60 "
            + "characters long.";

    /*
     * The first character of the name must be a letter,
     * otherwise punctuation-only or blank values become valid inputs.
     */
    public static final String VALIDATION_REGEX = "[\\p{L}](?:[\\p{L} '\\-]*[\\p{L}])?";

    public final String fullName;

    /**
     * Constructs a {@code Name}.
     *
     * @param name A valid name.
     */
    public Name(String name) {
        requireNonNull(name);
        checkArgument(isValidName(name), MESSAGE_CONSTRAINTS);
        fullName = name;
    }

    /**
     * Returns true if a given string is a valid name.
     */
    public static boolean isValidName(String test) {
        return test.length() <= 60 && test.matches(VALIDATION_REGEX);
    }

    /**
     * Returns a canonical form of this name for identity comparisons.
     * Capitalisation and runs of whitespace are ignored.
     */
    public String normalized() {
        return fullName.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }


    @Override
    public String toString() {
        return fullName;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof Name otherName)) {
            return false;
        }

        return fullName.equals(otherName.fullName);
    }

    @Override
    public int hashCode() {
        return fullName.hashCode();
    }

}
