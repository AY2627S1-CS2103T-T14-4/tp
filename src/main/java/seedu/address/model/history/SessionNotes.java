package seedu.address.model.history;

import static java.util.Objects.requireNonNull;

import seedu.address.commons.util.AppUtil;

/**
 * Immutable notes describing one completed grooming session, separate from standing pet notes.
 */
public final class SessionNotes {
    public static final int MAX_LENGTH = 200;
    public static final String MESSAGE_CONSTRAINTS =
            "Session notes should be between 1 and " + MAX_LENGTH + " characters long.";

    private final String value;

    /**
     * Creates notes containing 1 to 200 characters after removing leading and trailing spaces.
     */
    public SessionNotes(String notes) {
        requireNonNull(notes);
        String trimmed = notes.trim();
        AppUtil.checkArgument(isValidNotes(trimmed), MESSAGE_CONSTRAINTS);
        value = trimmed;
    }

    /**
     * Returns whether the notes satisfy the length requirement after trimming.
     */
    public static boolean isValidNotes(String notes) {
        return notes != null && !notes.trim().isEmpty() && notes.trim().length() <= MAX_LENGTH;
    }

    @Override
    public boolean equals(Object other) {
        return other == this || (other instanceof SessionNotes otherNotes && value.equals(otherNotes.value));
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public String toString() {
        return value;
    }
}
