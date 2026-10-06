package seedu.address.model.pet;

import static java.util.Objects.requireNonNull;

import seedu.address.commons.util.AppUtil;

/**
 * Represents a pet's species.
 * Guarantees: immutable; is valid as declared in {@link #isValidSpecies(String)}.
 */
public class Species {

    public static final String MESSAGE_CONSTRAINTS =
            "Species should be 1 to 40 characters long and contain only "
                    + "letters, numbers, spaces, apostrophes and hyphens.";

    private static final int MAX_LENGTH = 40;

    private static final String VALIDATION_REGEX = "[\\p{L}\\p{N}' -]+";

    public final String value;

    /**
     * Constructs a {@code Species}.
     *
     * @param species A valid species.
     */
    public Species(String species) {
        requireNonNull(species);

        String normalizedSpecies = normalize(species);
        AppUtil.checkArgument(isValidSpecies(normalizedSpecies), MESSAGE_CONSTRAINTS);

        value = normalizedSpecies;
    }

    /**
     * Normalizes the species by trimming leading/trailing spaces
     * and replacing repeated spaces with a single space.
     */
    private static String normalize(String species) {
        return species.trim().replaceAll(" {2,}", " ");
    }

    /**
     * Returns true if a given string is a valid species.
     */
    public static boolean isValidSpecies(String test) {
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
                || (other instanceof Species otherSpecies
                && value.equals(otherSpecies.value));
    }

    /**
     * Returns a hash code based on the value, consistent with {@link #equals(Object)}.
     */
    @Override
    public int hashCode() {
        return value.hashCode();
    }
}
