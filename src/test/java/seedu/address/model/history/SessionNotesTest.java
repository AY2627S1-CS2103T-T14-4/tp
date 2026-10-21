package seedu.address.model.history;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class SessionNotesTest {
    @Test
    public void constructor_trimsAndAcceptsLengthBoundaries() {
        assertEquals("x", new SessionNotes("  x  ").toString());
        assertEquals("x".repeat(200), new SessionNotes(" " + "x".repeat(200) + " ").toString());
        assertEquals("Short ears,  no perfume", new SessionNotes("Short ears,  no perfume").toString());
    }

    @Test
    public void constructor_invalidNotes_throwsException() {
        assertThrows(NullPointerException.class, () -> new SessionNotes(null));
        for (String notes : new String[] {"", " \t\n ", "x".repeat(201)}) {
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, ()
                    -> new SessionNotes(notes));
            assertEquals(SessionNotes.MESSAGE_CONSTRAINTS, exception.getMessage());
        }
    }

    @Test
    public void isValidNotes_checksTrimmedLength() {
        assertFalse(SessionNotes.isValidNotes(null));
        assertFalse(SessionNotes.isValidNotes(" \t\n "));
        assertFalse(SessionNotes.isValidNotes("x".repeat(201)));
        assertTrue(SessionNotes.isValidNotes("x"));
        assertTrue(SessionNotes.isValidNotes(" " + "x".repeat(200) + " "));
    }

    @Test
    public void equalsAndHashCode_compareTrimmedValue() {
        SessionNotes notes = new SessionNotes("Short ears");
        assertEquals(notes, notes);
        assertEquals(notes, new SessionNotes(" Short ears "));
        assertEquals(notes.hashCode(), new SessionNotes("Short ears").hashCode());
        assertNotEquals(notes, new SessionNotes("Long ears"));
        assertNotEquals(notes, null);
        assertNotEquals(notes, "Short ears");
    }
}
