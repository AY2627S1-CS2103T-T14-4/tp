package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.testutil.Assert.assertThrows;

import java.util.Map;

import org.junit.jupiter.api.Test;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.model.pet.Breed;
import seedu.address.model.pet.NoteType;
import seedu.address.model.pet.Pet;
import seedu.address.model.pet.PetName;
import seedu.address.model.pet.PetNote;
import seedu.address.model.pet.Species;

public class JsonAdaptedPetTest {
    private static final Pet MILO = new Pet(new PetName("Milo"), new Species("Dog"), new Breed("Poodle"));
    private static final Pet MILO_WITH_NOTES = MILO
            .withNote(NoteType.PREF, new PetNote("Quiet handling"))
            .withNote(NoteType.SENS, new PetNote("Sensitive paws"))
            .withNote(NoteType.TEMP, new PetNote("Anxious around dryers"));

    @Test
    public void toModelType_validPetDetails_returnsPet() throws Exception {
        assertEquals(MILO, new JsonAdaptedPet(MILO).toModelType());
    }

    @Test
    public void toModelType_petWithNotes_returnsPetWithNotes() throws Exception {
        assertEquals(MILO_WITH_NOTES, new JsonAdaptedPet(MILO_WITH_NOTES).toModelType());
    }

    @Test
    public void toModelType_missingNotes_returnsPetWithoutNotes() throws Exception {
        assertEquals(MILO, new JsonAdaptedPet("Milo", "Dog", "Poodle").toModelType());
    }

    @Test
    public void toModelType_invalidNotes_throwsIllegalValueException() {
        JsonAdaptedPet invalidType = new JsonAdaptedPet("Milo", "Dog", "Poodle",
                Map.of("unknown", "Some note"));
        JsonAdaptedPet invalidValue = new JsonAdaptedPet("Milo", "Dog", "Poodle",
                Map.of("pref", " "));
        JsonAdaptedPet duplicateType = new JsonAdaptedPet("Milo", "Dog", "Poodle",
                Map.of("pref", "Quiet handling", "PREF", "Short sessions"));

        assertThrows(IllegalValueException.class, NoteType.MESSAGE_CONSTRAINTS, invalidType::toModelType);
        assertThrows(IllegalValueException.class, PetNote.MESSAGE_CONSTRAINTS, invalidValue::toModelType);
        assertThrows(IllegalValueException.class, NoteType.MESSAGE_CONSTRAINTS, duplicateType::toModelType);
    }

    @Test
    public void toModelType_missingOrInvalidName_throwsIllegalValueException() {
        JsonAdaptedPet missingName = new JsonAdaptedPet(null, "Dog", "Poodle");
        JsonAdaptedPet invalidName = new JsonAdaptedPet(" ", "Dog", "Poodle");

        assertThrows(IllegalValueException.class, String.format(JsonAdaptedPet.MISSING_FIELD_MESSAGE_FORMAT,
                        PetName.class.getSimpleName()), missingName::toModelType);
        assertThrows(IllegalValueException.class, PetName.MESSAGE_CONSTRAINTS, invalidName::toModelType);
    }

    @Test
    public void toModelType_missingOrInvalidSpecies_throwsIllegalValueException() {
        JsonAdaptedPet missingSpecies = new JsonAdaptedPet("Milo", null, "Poodle");
        JsonAdaptedPet invalidSpecies = new JsonAdaptedPet("Milo", " ", "Poodle");

        assertThrows(IllegalValueException.class, String.format(JsonAdaptedPet.MISSING_FIELD_MESSAGE_FORMAT,
                        Species.class.getSimpleName()), missingSpecies::toModelType);
        assertThrows(IllegalValueException.class, Species.MESSAGE_CONSTRAINTS, invalidSpecies::toModelType);
    }

    @Test
    public void toModelType_missingOrInvalidBreed_throwsIllegalValueException() {
        JsonAdaptedPet missingBreed = new JsonAdaptedPet("Milo", "Dog", null);
        JsonAdaptedPet invalidBreed = new JsonAdaptedPet("Milo", "Dog", " ");
        JsonAdaptedPet invalidBreedCharacters = new JsonAdaptedPet("Milo", "Dog", "Poodle!");

        assertThrows(IllegalValueException.class, String.format(JsonAdaptedPet.MISSING_FIELD_MESSAGE_FORMAT,
                        Breed.class.getSimpleName()), missingBreed::toModelType);
        assertThrows(IllegalValueException.class, Breed.MESSAGE_CONSTRAINTS, invalidBreed::toModelType);
        assertThrows(IllegalValueException.class, Breed.MESSAGE_CONSTRAINTS, invalidBreedCharacters::toModelType);
    }
}
