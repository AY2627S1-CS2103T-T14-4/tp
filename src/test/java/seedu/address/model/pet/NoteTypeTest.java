package seedu.address.model.pet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

public class NoteTypeTest {

    @Test
    public void isValidKeyword() {
        // invalid
        assertFalse(NoteType.isValidKeyword(null));
        assertFalse(NoteType.isValidKeyword(""));
        assertFalse(NoteType.isValidKeyword("preference"));
        assertFalse(NoteType.isValidKeyword("temp2"));

        // valid, ignoring case and surrounding spaces
        assertTrue(NoteType.isValidKeyword("pref"));
        assertTrue(NoteType.isValidKeyword("SENS"));
        assertTrue(NoteType.isValidKeyword("  Temp  "));
    }

    @Test
    public void fromKeyword_validKeyword_returnsType() {
        assertEquals(NoteType.PREF, NoteType.fromKeyword("pref"));
        assertEquals(NoteType.SENS, NoteType.fromKeyword("Sens"));
        assertEquals(NoteType.TEMP, NoteType.fromKeyword(" TEMP "));
    }

    @Test
    public void fromKeyword_invalidKeyword_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, NoteType.MESSAGE_CONSTRAINTS, () -> NoteType.fromKeyword("x"));
    }

    @Test
    public void fromKeyword_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> NoteType.fromKeyword(null));
    }

    @Test
    public void isSafety() {
        assertFalse(NoteType.PREF.isSafety());
        assertTrue(NoteType.SENS.isSafety());
        assertTrue(NoteType.TEMP.isSafety());
    }
}
