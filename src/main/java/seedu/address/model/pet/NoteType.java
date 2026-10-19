package seedu.address.model.pet;

import static java.util.Objects.requireNonNull;

import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;

/**
 * Represents the kind of note that can be recorded for a {@link Pet}.
 * Sensitivity and temperament notes are treated as safety information.
 */
public enum NoteType {
    PREF("pref", "preference", false),
    SENS("sens", "sensitivity", true),
    TEMP("temp", "temperament", true);

    public static final String MESSAGE_CONSTRAINTS = "Note type must be pref, sens or temp.";

    private final String keyword;
    private final String displayName;
    private final boolean isSafety;

    NoteType(String keyword, String displayName, boolean isSafety) {
        this.keyword = keyword;
        this.displayName = displayName;
        this.isSafety = isSafety;
    }

    /**
     * Returns the keyword used in commands, e.g. {@code temp}.
     */
    public String getKeyword() {
        return keyword;
    }

    /**
     * Returns the user-facing name, e.g. {@code temperament}.
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Returns true if notes of this type are safety information.
     */
    public boolean isSafety() {
        return isSafety;
    }

    /**
     * Returns true if {@code test} is a valid note type keyword, ignoring case and surrounding spaces.
     */
    public static boolean isValidKeyword(String test) {
        return test != null && findByKeyword(test).isPresent();
    }

    /**
     * Returns the note type matching the given keyword, ignoring case and surrounding spaces.
     *
     * @throws IllegalArgumentException if the keyword is not valid.
     */
    public static NoteType fromKeyword(String keyword) {
        requireNonNull(keyword);
        return findByKeyword(keyword).orElseThrow(() -> new IllegalArgumentException(MESSAGE_CONSTRAINTS));
    }

    private static Optional<NoteType> findByKeyword(String keyword) {
        String normalized = keyword.trim().toLowerCase(Locale.ROOT);
        return Arrays.stream(values())
                .filter(type -> type.keyword.equals(normalized))
                .findFirst();
    }
}
