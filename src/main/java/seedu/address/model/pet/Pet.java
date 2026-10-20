package seedu.address.model.pet;

import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

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
    private final Map<NoteType, PetNote> notes;

    /**
     * Every field must be present and not null.
     */
    public Pet(PetName name, Species species, Breed breed) {
        this(name, species, breed, Collections.emptyMap());
    }

    /**
     * Every field must be present and not null. The notes map is defensively copied.
     */
    public Pet(PetName name, Species species, Breed breed, Map<NoteType, PetNote> notes) {
        requireAllNonNull(name, species, breed);
        requireAllNonNull(notes);

        this.name = name;
        this.species = species;
        this.breed = breed;
        EnumMap<NoteType, PetNote> copiedNotes = new EnumMap<>(NoteType.class);
        notes.forEach((type, note) -> {
            requireAllNonNull(type, note);
            copiedNotes.put(type, note);
        });
        this.notes = Collections.unmodifiableMap(copiedNotes);
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
     * Returns this pet's notes keyed by note type.
     */
    public Map<NoteType, PetNote> getNotes() {
        return notes;
    }

    /**
     * Returns this pet's note of the given type, if one exists.
     */
    public Optional<PetNote> getNote(NoteType type) {
        return Optional.ofNullable(notes.get(Objects.requireNonNull(type)));
    }

    /**
     * Returns true if this pet has a note of the given type.
     */
    public boolean hasNote(NoteType type) {
        return notes.containsKey(Objects.requireNonNull(type));
    }

    /**
     * Returns true if this pet has a sensitivity or temperament note.
     */
    public boolean hasSafetyNote() {
        return hasNote(NoteType.SENS) || hasNote(NoteType.TEMP);
    }

    /**
     * Returns a copy of this pet with the note of the given type added or replaced.
     */
    public Pet withNote(NoteType type, PetNote note) {
        requireAllNonNull(type, note);
        EnumMap<NoteType, PetNote> updatedNotes = new EnumMap<>(NoteType.class);
        updatedNotes.putAll(notes);
        updatedNotes.put(type, note);
        return new Pet(name, species, breed, updatedNotes);
    }

    /**
     * Returns a copy of this pet without the note of the given type.
     */
    public Pet withoutNote(NoteType type) {
        requireAllNonNull(type);
        EnumMap<NoteType, PetNote> updatedNotes = new EnumMap<>(NoteType.class);
        updatedNotes.putAll(notes);
        updatedNotes.remove(type);
        return new Pet(name, species, breed, updatedNotes);
    }

    /**
     * Returns the normalized identity key for this pet.
     * Pet identity is determined by name, ignoring case.
     *
     * @return the case-normalized pet name
     */
    public String getIdentityKey() {
        return name.fullName.toLowerCase(Locale.ROOT);
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
                && getIdentityKey().equals(otherPet.getIdentityKey());
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
                && breed.equals(otherPet.breed)
                && notes.equals(otherPet.notes);
    }

    /**
     * Returns a hash code consistent with {@link #equals(Object)}.
     */
    @Override
    public int hashCode() {
        return Objects.hash(name, species, breed, notes);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("name", name)
                .add("species", species)
                .add("breed", breed)
                .add("notes", notes)
                .toString();
    }
}
