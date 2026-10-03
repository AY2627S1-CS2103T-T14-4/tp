package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class RemarkTest {

    @Test
    public void constructor_null_throwsNullPointerException() {
        assertThrows(
                NullPointerException.class, () -> new Remark(null));
    }

    @Test
    public void toString_returnsRemarkValue() {
        Remark remark = new Remark("Hello");

        assertEquals("Hello", remark.toString());
    }

    @Test
    public void equals() {
        Remark remark = new Remark("Hello");

        assertTrue(remark.equals(remark));

        Remark remarkCopy = new Remark("Hello");
        assertTrue(remark.equals(remarkCopy));

        Remark differentRemark = new Remark("Bye");
        assertFalse(remark.equals(differentRemark));

        assertFalse(remark.equals(null));

        assertFalse(remark.equals("Hello"));
    }

    @Test
    public void hashCode_sameRemark_sameHashCode() {
        Remark first = new Remark("Hello");
        Remark second = new Remark("Hello");

        assertEquals(first.hashCode(), second.hashCode());
    }
}
