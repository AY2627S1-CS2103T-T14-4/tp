package seedu.address.model.pet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

public class PetNoteTest {

    @Test
    public void constructor_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new PetNote(null));
    }

    @Test
    public void constructor_invalidNote_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new PetNote(""));
        assertThrows(IllegalArgumentException.class, () -> new PetNote("   "));
    }

    @Test
    public void constructor_trimsSurroundingSpaces() {
        assertEquals("Anxious around dryers", new PetNote("  Anxious around dryers  ").value);
    }

    @Test
    public void isValidNote() {
        // invalid
        assertFalse(PetNote.isValidNote(null));
        assertFalse(PetNote.isValidNote(""));
        assertFalse(PetNote.isValidNote("   "));
        assertFalse(PetNote.isValidNote("a".repeat(PetNote.MAX_LENGTH + 1)));

        // valid
        assertTrue(PetNote.isValidNote("a"));
        assertTrue(PetNote.isValidNote("No perfume, sensitive skin!"));
        assertTrue(PetNote.isValidNote("a".repeat(PetNote.MAX_LENGTH)));
        assertTrue(PetNote.isValidNote("  " + "a".repeat(PetNote.MAX_LENGTH) + "  "));
    }

    @Test
    public void equals() {
        PetNote note = new PetNote("Bites when nervous");

        assertTrue(note.equals(new PetNote("Bites when nervous")));
        assertTrue(note.equals(note));
        assertFalse(note.equals(null));
        assertFalse(note.equals("Bites when nervous"));
        assertFalse(note.equals(new PetNote("Calm")));
        assertEquals(note.hashCode(), new PetNote("Bites when nervous").hashCode());
    }
}
