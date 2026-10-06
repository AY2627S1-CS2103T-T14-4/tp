package seedu.address.model.pet;

import static java.util.Objects.requireNonNull;

import seedu.address.commons.util.AppUtil;

/**
 * Represents a pet's name.
 * Guarantees: immutable; is valid as declared in {@link #isValidName(String)}.
 */
public class PetName {

    public static final String MESSAGE_CONSTRAINTS =
            "Pet names should be 1 to 40 characters long and contain only "
                    + "letters, numbers, spaces, apostrophes and hyphens.";

    private static final int MAX_LENGTH = 40;

    private static final String VALIDATION_REGEX = "[\\p{L}\\p{N}' -]+";

    public final String fullName;

    /**
     * Constructs a {@code PetName}.
     *
     * @param name A valid pet name.
     */
    public PetName(String name) {
        requireNonNull(name);

        String normalizedName = normalize(name);
        AppUtil.checkArgument(isValidName(normalizedName), MESSAGE_CONSTRAINTS);

        fullName = normalizedName;
    }

    private static String normalize(String name) {
        return name.trim().replaceAll(" {2,}", " ");
    }

    /**
     * Returns true if a given string is a valid pet name.
     */
    public static boolean isValidName(String test) {
        return test != null
                && !test.isEmpty()
                && test.length() <= MAX_LENGTH
                && test.matches(VALIDATION_REGEX);
    }

    @Override
    public String toString() {
        return fullName;
    }

    @Override
    public boolean equals(Object other) {
        return other == this
                || (other instanceof PetName otherName
                && fullName.equals(otherName.fullName));
    }

    /**
     * Returns a hash code based on the pet name, consistent with {@link #equals(Object)}.
     */
    @Override
    public int hashCode() {
        return fullName.hashCode();
    }
}
