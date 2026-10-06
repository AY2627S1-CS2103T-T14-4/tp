package seedu.address.model.pet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

public class SpeciesTest {

    @Test
    public void constructor_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new Species(null));
    }

    @Test
    public void constructor_invalidSpecies_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new Species(""));
    }

    @Test
    public void isValidSpecies() {
        // null
        assertFalse(Species.isValidSpecies(null));

        // invalid
        assertFalse(Species.isValidSpecies(""));
        assertFalse(Species.isValidSpecies("Dog!"));
        assertFalse(Species.isValidSpecies("Cat@Home"));

        // valid
        assertTrue(Species.isValidSpecies("Dog"));
        assertTrue(Species.isValidSpecies("Cat"));
        assertTrue(Species.isValidSpecies("Guinea Pig"));
        assertTrue(Species.isValidSpecies("Species-2"));
        assertTrue(Species.isValidSpecies("Bird's Species"));
    }

    @Test
    public void isValidSpecies_lengthBoundary() {
        assertTrue(Species.isValidSpecies("A".repeat(40)));
        assertFalse(Species.isValidSpecies("A".repeat(41)));
    }

    @Test
    public void constructor_extraSpaces_normalizesSpaces() {
        Species species = new Species("  Guinea   Pig  ");

        assertEquals("Guinea Pig", species.value);
    }

    @Test
    public void toString_returnsSpecies() {
        Species species = new Species("Dog");

        assertEquals("Dog", species.toString());
    }

    @Test
    public void equals() {
        Species species = new Species("Dog");

        // same object
        assertTrue(species.equals(species));

        // same value
        assertTrue(species.equals(new Species("Dog")));

        // different value
        assertFalse(species.equals(new Species("Cat")));

        // null
        assertFalse(species.equals(null));

        // different type
        assertFalse(species.equals("Dog"));
    }

    @Test
    public void hashCode_sameSpecies_sameHashCode() {
        Species first = new Species("Dog");
        Species second = new Species("Dog");

        assertEquals(first.hashCode(), second.hashCode());
    }
}
