package seedu.address.storage;

import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.model.pet.Breed;
import seedu.address.model.pet.NoteType;
import seedu.address.model.pet.Pet;
import seedu.address.model.pet.PetNote;
import seedu.address.model.pet.PetName;
import seedu.address.model.pet.Species;

/**
 * Jackson-friendly representation of a {@link Pet}.
 */
class JsonAdaptedPet {
    public static final String MISSING_FIELD_MESSAGE_FORMAT = "Pet's %s field is missing!";

    private final String name;
    private final String species;
    private final String breed;
    private final Map<String, String> notes;

    /**
     * Creates an adapted pet from stored fields.
     */
    @JsonCreator
    public JsonAdaptedPet(@JsonProperty("name") String name, @JsonProperty("species") String species,
            @JsonProperty("breed") String breed, @JsonProperty("notes") Map<String, String> notes) {
        this.name = name;
        this.species = species;
        this.breed = breed;
        this.notes = notes == null ? new LinkedHashMap<>() : new LinkedHashMap<>(notes);
    }

    /**
     * Creates an adapted pet from stored fields without notes, for compatibility with older callers.
     */
    public JsonAdaptedPet(String name, String species, String breed) {
        this(name, species, breed, null);
    }

    /**
     * Converts a model pet to its stored representation.
     */
    public JsonAdaptedPet(Pet source) {
        name = source.getName().toString();
        species = source.getSpecies().toString();
        breed = source.getBreed().toString();
        notes = new LinkedHashMap<>();
        source.getNotes().forEach((type, note) -> notes.put(type.getKeyword(), note.value));
    }

    /**
     * Converts stored values to a model pet.
     *
     * @throws IllegalValueException if a required field is missing or invalid.
     */
    public Pet toModelType() throws IllegalValueException {
        if (name == null) {
            throw new IllegalValueException(String.format(MISSING_FIELD_MESSAGE_FORMAT,
                    PetName.class.getSimpleName()));
        }
        if (!PetName.isValidName(name)) {
            throw new IllegalValueException(PetName.MESSAGE_CONSTRAINTS);
        }

        if (species == null) {
            throw new IllegalValueException(String.format(MISSING_FIELD_MESSAGE_FORMAT,
                    Species.class.getSimpleName()));
        }
        if (!Species.isValidSpecies(species)) {
            throw new IllegalValueException(Species.MESSAGE_CONSTRAINTS);
        }

        if (breed == null) {
            throw new IllegalValueException(String.format(MISSING_FIELD_MESSAGE_FORMAT,
                    Breed.class.getSimpleName()));
        }
        if (!Breed.isValidBreed(breed)) {
            throw new IllegalValueException(Breed.MESSAGE_CONSTRAINTS);
        }

        Map<NoteType, PetNote> modelNotes = new EnumMap<>(NoteType.class);
        for (Map.Entry<String, String> noteEntry : notes.entrySet()) {
            if (!NoteType.isValidKeyword(noteEntry.getKey())) {
                throw new IllegalValueException(NoteType.MESSAGE_CONSTRAINTS);
            }

            NoteType noteType = NoteType.fromKeyword(noteEntry.getKey());
            if (modelNotes.containsKey(noteType)) {
                throw new IllegalValueException(NoteType.MESSAGE_CONSTRAINTS);
            }
            if (!PetNote.isValidNote(noteEntry.getValue())) {
                throw new IllegalValueException(PetNote.MESSAGE_CONSTRAINTS);
            }
            modelNotes.put(noteType, new PetNote(noteEntry.getValue()));
        }

        try {
            return new Pet(new PetName(name), new Species(species), new Breed(breed), modelNotes);
        } catch (IllegalArgumentException e) {
            throw new IllegalValueException(e.getMessage(), e);
        }
    }
}
