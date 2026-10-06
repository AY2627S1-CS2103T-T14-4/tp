package seedu.address.model.pet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

public class BreedTest {

    @Test
    public void constructor_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new Breed(null));
    }

    @Test
    public void constructor_invalidBreed_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new Breed(""));
    }

    @Test
    public void isValidBreed() {
        // null
        assertFalse(Breed.isValidBreed(null));

        // invalid
        assertFalse(Breed.isValidBreed(""));
        assertFalse(Breed.isValidBreed("Poodle!"));
        assertFalse(Breed.isValidBreed("Golden@Retriever"));

        // valid
        assertTrue(Breed.isValidBreed("Poodle"));
        assertTrue(Breed.isValidBreed("Golden Retriever"));
        assertTrue(Breed.isValidBreed("Breed-2"));
        assertTrue(Breed.isValidBreed("King's Breed"));
    }

    @Test
    public void isValidBreed_lengthBoundary() {
        assertTrue(Breed.isValidBreed("A".repeat(60)));
        assertFalse(Breed.isValidBreed("A".repeat(61)));
    }

    @Test
    public void constructor_leadingAndTrailingSpaces_trimsSpaces() {
        Breed breed = new Breed("  Golden Retriever  ");

        assertEquals("Golden Retriever", breed.value);
    }

    @Test
    public void constructor_repeatedInternalSpaces_preservesSpaces() {
        Breed breed = new Breed("Golden   Retriever");

        assertEquals("Golden   Retriever", breed.value);
    }

    @Test
    public void toString_returnsBreed() {
        Breed breed = new Breed("Poodle");

        assertEquals("Poodle", breed.toString());
    }

    @Test
    public void equals() {
        Breed breed = new Breed("Poodle");

        // same object
        assertTrue(breed.equals(breed));

        // same value
        assertTrue(breed.equals(new Breed("Poodle")));

        // different value
        assertFalse(breed.equals(new Breed("Labrador")));

        // null
        assertFalse(breed.equals(null));

        // different type
        assertFalse(breed.equals("Poodle"));
    }

    @Test
    public void hashCode_sameBreed_sameHashCode() {
        Breed first = new Breed("Poodle");
        Breed second = new Breed("Poodle");

        assertEquals(first.hashCode(), second.hashCode());
    }
}
