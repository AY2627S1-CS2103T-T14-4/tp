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

    @Test
    public void isValidName_tooLong_returnsFalse() {
        assertFalse(PetName.isValidName("A".repeat(41)));
    }

    @Test
    public void toString_returnsFullName() {
        PetName petName = new PetName("Milo");

        assertEquals("Milo", petName.toString());
    }

    @Test
    public void equals() {
        PetName petName = new PetName("Milo");

        // same object
        assertTrue(petName.equals(petName));

        // same value
        assertTrue(petName.equals(new PetName("Milo")));

        // different value
        assertFalse(petName.equals(new PetName("Coco")));

        // null
        assertFalse(petName.equals(null));

        // different type
        assertFalse(petName.equals("Milo"));
    }

    @Test
    public void hashCode_samePetName_sameHashCode() {
        PetName first = new PetName("Milo");
        PetName second = new PetName("Milo");

        assertEquals(first.hashCode(), second.hashCode());
    }

    @Test
    public void isValidName_lengthBoundary() {
        assertTrue(PetName.isValidName("A".repeat(40)));
        assertFalse(PetName.isValidName("A".repeat(41)));
    }
}
