package seedu.address.model.pet;

import static java.util.Objects.requireNonNull;

import seedu.address.commons.util.AppUtil;

/**
 * Represents a note about a pet, such as a grooming preference.
 * Guarantees: immutable; is valid as declared in {@link #isValidNote(String)}.
 */
public class PetNote {

    public static final int MAX_LENGTH = 200;

    public static final String MESSAGE_CONSTRAINTS =
            "Notes should be between 1 and " + MAX_LENGTH + " characters long.";

    public final String value;

    /**
     * Constructs a {@code PetNote}. Leading and trailing spaces are removed.
     *
     * @param note A valid note.
     */
    public PetNote(String note) {
        requireNonNull(note);

        String normalizedNote = note.trim();
        AppUtil.checkArgument(isValidNote(normalizedNote), MESSAGE_CONSTRAINTS);

        value = normalizedNote;
    }

    /**
     * Returns true if a given string is a valid note after trimming.
     */
    public static boolean isValidNote(String test) {
        if (test == null) {
            return false;
        }
        String trimmed = test.trim();
        return !trimmed.isEmpty() && trimmed.length() <= MAX_LENGTH;
    }

    @Override
    public String toString() {
        return value;
    }

    @Override
    public boolean equals(Object other) {
        return other == this
                || (other instanceof PetNote otherNote
                && value.equals(otherNote.value));
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }
}
