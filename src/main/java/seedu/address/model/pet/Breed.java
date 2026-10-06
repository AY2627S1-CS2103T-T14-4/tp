package seedu.address.model.pet;

import static java.util.Objects.requireNonNull;

import seedu.address.commons.util.AppUtil;

/**
 * Represents a pet's breed.
 * Guarantees: immutable; is valid as declared in {@link #isValidBreed(String)}.
 */
public class Breed {

    public static final String MESSAGE_CONSTRAINTS =
            "Breed should be 1 to 60 characters long and contain only "
                    + "letters, numbers, spaces, apostrophes and hyphens.";

    private static final int MAX_LENGTH = 60;

    private static final String VALIDATION_REGEX = "[\\p{L}\\p{N}' -]+";

    public final String value;

    /**
     * Constructs a {@code Breed}.
     *
     * @param breed A valid breed.
     */
    public Breed(String breed) {
        requireNonNull(breed);

        String normalizedBreed = normalize(breed);
        AppUtil.checkArgument(isValidBreed(normalizedBreed), MESSAGE_CONSTRAINTS);

        value = normalizedBreed;
    }

    /**
     * Normalizes the breed by trimming leading and trailing spaces.
     */
    private static String normalize(String breed) {
        return breed.trim();
    }

    /**
     * Returns true if a given string is a valid breed.
     */
    public static boolean isValidBreed(String test) {
        return test != null
                && !test.isEmpty()
                && test.length() <= MAX_LENGTH
                && test.matches(VALIDATION_REGEX);
    }

    @Override
    public String toString() {
        return value;
    }

    @Override
    public boolean equals(Object other) {
        return other == this
                || (other instanceof Breed otherBreed
                && value.equals(otherBreed.value));
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }
}
