package seedu.address.model.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

public class PhoneTest {

    @Test
    public void constructor_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new Phone(null));
    }

    @Test
    public void constructor_invalidPhone_throwsIllegalArgumentException() {
        String invalidPhone = "";
        assertThrows(IllegalArgumentException.class, () -> new Phone(invalidPhone));
    }

    @Test
    public void isValidPhone() {
        // null phone number
        assertThrows(NullPointerException.class, () -> Phone.isValidPhone(null));

        // invalid phone numbers
        assertFalse(Phone.isValidPhone("")); // empty string
        assertFalse(Phone.isValidPhone(" ")); // spaces only
        assertFalse(Phone.isValidPhone("91")); // less than 3 numbers
        assertFalse(Phone.isValidPhone("phone")); // non-numeric
        assertFalse(Phone.isValidPhone("9011p041")); // alphabets within digits
        assertFalse(Phone.isValidPhone("12-34")); // invalid formatting
        assertFalse(Phone.isValidPhone("1242938420331234")); // too long
        assertFalse(Phone.isValidPhone("+123 4567 8901 2345 6")); // 16 digits

        // valid phone numbers
        assertTrue(Phone.isValidPhone("911")); // minimum length
        assertTrue(Phone.isValidPhone("93121534"));
        assertTrue(Phone.isValidPhone("+65 9123 4567")); // optional country code and spaces
        assertTrue(Phone.isValidPhone("124293842033123")); // maximum length
        assertTrue(Phone.isValidPhone("+123 4567 8901 2345")); // 15 digits with formatting
    }

    @Test
    public void constructor_formattedPhone_normalizesValue() {
        Phone phone = new Phone("  +65  9123   4567  ");

        assertEquals("+65 9123 4567", phone.value);
    }

    @Test
    public void equals() {
        Phone phone = new Phone("99999999");

        // same values -> returns true
        assertTrue(phone.equals(new Phone("99999999")));
        assertTrue(new Phone("+65 9123 4567").equals(new Phone("6591234567")));

        // same object -> returns true
        assertTrue(phone.equals(phone));

        // null -> returns false
        assertFalse(phone.equals(null));

        // different types -> returns false
        assertFalse(phone.equals(5.0f));

        // different values -> returns false
        assertFalse(phone.equals(new Phone("99999995")));
    }
}
