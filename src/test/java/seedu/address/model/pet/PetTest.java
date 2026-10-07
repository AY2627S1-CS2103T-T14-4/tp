package seedu.address.model.pet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

public class PetTest {

    private final Pet milo = new Pet(
            new PetName("Milo"),
            new Species("Dog"),
            new Breed("Poodle"));

    @Test
    public void constructor_nullName_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () ->
                new Pet(null, new Species("Dog"), new Breed("Poodle")));
    }

    @Test
    public void constructor_nullSpecies_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () ->
                new Pet(new PetName("Milo"), null, new Breed("Poodle")));
    }

    @Test
    public void constructor_nullBreed_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () ->
                new Pet(new PetName("Milo"), new Species("Dog"), null));
    }

    @Test
    public void getters_returnCorrectValues() {
        assertEquals(new PetName("Milo"), milo.getName());
        assertEquals(new Species("Dog"), milo.getSpecies());
        assertEquals(new Breed("Poodle"), milo.getBreed());
    }

    @Test
    public void isSamePet() {
        // same object -> returns true
        assertTrue(milo.isSamePet(milo));

        // null -> returns false
        assertFalse(milo.isSamePet(null));

        // same name, different species and breed -> returns true
        Pet sameName = new Pet(
                new PetName("Milo"),
                new Species("Cat"),
                new Breed("Persian"));
        assertTrue(milo.isSamePet(sameName));

        // same name with different capitalisation -> returns true
        Pet sameNameDifferentCase = new Pet(
                new PetName("mILO"),
                new Species("Dog"),
                new Breed("Poodle"));
        assertTrue(milo.isSamePet(sameNameDifferentCase));

        // same name after whitespace normalization -> returns true
        Pet sameNameExtraSpaces = new Pet(
                new PetName("  Milo  "),
                new Species("Dog"),
                new Breed("Poodle"));
        assertTrue(milo.isSamePet(sameNameExtraSpaces));

        // different name -> returns false
        Pet differentName = new Pet(
                new PetName("Coco"),
                new Species("Dog"),
                new Breed("Poodle"));
        assertFalse(milo.isSamePet(differentName));
    }

    @Test
    public void equals() {
        // same values -> returns true
        Pet miloCopy = new Pet(
                new PetName("Milo"),
                new Species("Dog"),
                new Breed("Poodle"));
        assertTrue(milo.equals(miloCopy));

        // same object -> returns true
        assertTrue(milo.equals(milo));

        // null -> returns false
        assertFalse(milo.equals(null));

        // different type -> returns false
        assertFalse(milo.equals("Milo"));

        // different name -> returns false
        Pet differentName = new Pet(
                new PetName("Coco"),
                new Species("Dog"),
                new Breed("Poodle"));
        assertFalse(milo.equals(differentName));

        // different species -> returns false
        Pet differentSpecies = new Pet(
                new PetName("Milo"),
                new Species("Cat"),
                new Breed("Poodle"));
        assertFalse(milo.equals(differentSpecies));

        // different breed -> returns false
        Pet differentBreed = new Pet(
                new PetName("Milo"),
                new Species("Dog"),
                new Breed("Labrador"));
        assertFalse(milo.equals(differentBreed));
    }

    @Test
    public void hashCode_samePet_sameHashCode() {
        Pet miloCopy = new Pet(
                new PetName("Milo"),
                new Species("Dog"),
                new Breed("Poodle"));

        assertEquals(milo.hashCode(), miloCopy.hashCode());
    }

    @Test
    public void toStringMethod() {
        String expected = Pet.class.getCanonicalName()
                + "{name=" + milo.getName()
                + ", species=" + milo.getSpecies()
                + ", breed=" + milo.getBreed()
                + "}";

        assertEquals(expected, milo.toString());
    }
}
