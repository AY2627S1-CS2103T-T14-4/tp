package seedu.address.model.pet;

import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.util.Objects;

import seedu.address.commons.util.ToStringBuilder;

/**
 * Represents a Pet in PawPals.
 * Guarantees: details are present and not null, field values are validated, immutable.
 */
public class Pet {

    // Identity field
    private final PetName name;

    // Data fields
    private final Species species;
    private final Breed breed;

    /**
     * Every field must be present and not null.
     */
    public Pet(PetName name, Species species, Breed breed) {
        requireAllNonNull(name, species, breed);

        this.name = name;
        this.species = species;
        this.breed = breed;
    }

    public PetName getName() {
        return name;
    }

    public Species getSpecies() {
        return species;
    }

    public Breed getBreed() {
        return breed;
    }

    /**
     * Returns true if both pets have the same name, ignoring case.
     *
     * This method assumes that both pets belong to the same client.
     * Pet names are already normalized by {@link PetName}.
     */
    public boolean isSamePet(Pet otherPet) {
        if (otherPet == this) {
            return true;
        }

        return otherPet != null
                && otherPet.getName().fullName.equalsIgnoreCase(getName().fullName);
    }

    /**
     * Returns true if both pets have the same identity and data fields.
     * This defines a stronger notion of equality between two pets.
     */
    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof Pet otherPet)) {
            return false;
        }

        return name.equals(otherPet.name)
                && species.equals(otherPet.species)
                && breed.equals(otherPet.breed);
    }

    /**
     * Returns a hash code consistent with {@link #equals(Object)}.
     */
    @Override
    public int hashCode() {
        return Objects.hash(name, species, breed);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("name", name)
                .add("species", species)
                .add("breed", breed)
                .toString();
    }
}
