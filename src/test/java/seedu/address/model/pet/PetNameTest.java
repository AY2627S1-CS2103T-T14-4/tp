package seedu.address.model.pet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

public class PetNameTest {

    @Test
    public void constructor_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new PetName(null));
    }

    @Test
    public void constructor_invalidPetName_throwsIllegalArgumentException() {
        String invalidPetName = "";
        assertThrows(IllegalArgumentException.class, () -> new PetName(invalidPetName));
    }

    @Test
    public void isValidName() {
        // null
        assertFalse(PetName.isValidName(null));

        // invalid
        assertFalse(PetName.isValidName(""));
        assertFalse(PetName.isValidName("Milo!"));
        assertFalse(PetName.isValidName("Milo@Home"));

        // valid
        assertTrue(PetName.isValidName("Milo"));
        assertTrue(PetName.isValidName("Milo 2"));
        assertTrue(PetName.isValidName("Mr-Paws"));
        assertTrue(PetName.isValidName("O'Malley"));
    }

    @Test
    public void constructor_extraSpaces_normalizesSpaces() {
        PetName petName = new PetName("  Milo   Jr  ");

        assertEquals("Milo Jr", petName.fullName);
    }
}
