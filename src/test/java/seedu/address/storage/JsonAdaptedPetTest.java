package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.model.pet.Breed;
import seedu.address.model.pet.Pet;
import seedu.address.model.pet.PetName;
import seedu.address.model.pet.Species;

public class JsonAdaptedPetTest {
    private static final Pet MILO = new Pet(new PetName("Milo"), new Species("Dog"), new Breed("Poodle"));

    @Test
    public void toModelType_validPetDetails_returnsPet() throws Exception {
        assertEquals(MILO, new JsonAdaptedPet(MILO).toModelType());
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

        assertThrows(IllegalValueException.class, String.format(JsonAdaptedPet.MISSING_FIELD_MESSAGE_FORMAT,
                        Breed.class.getSimpleName()), missingBreed::toModelType);
        assertThrows(IllegalValueException.class, Breed.MESSAGE_CONSTRAINTS, invalidBreed::toModelType);
    }
}
