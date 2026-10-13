package seedu.address.model.person;

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
        assertFalse(Phone.isValidPhone("9312 1534")); // no leading +
        assertFalse(Phone.isValidPhone("+0123456")); // international numbers cannot start with zero
        assertFalse(Phone.isValidPhone("-+6591234567")); // + must come first
        assertFalse(Phone.isValidPhone("+65 9123x4567")); // unsupported character
        assertFalse(Phone.isValidPhone("+65\t91234567")); // internal tabs are not separators
        assertFalse(Phone.isValidPhone("+" + "6".repeat(16))); // too many digits

        // valid phone numbers
        assertTrue(Phone.isValidPhone("+123")); // exactly 3 digits
        assertTrue(Phone.isValidPhone("+6591234567"));
        assertTrue(Phone.isValidPhone("+65 9123-4567")); // permitted separators
        assertTrue(Phone.isValidPhone("+60123457878"));
        assertTrue(Phone.isValidPhone("+9991234567")); // calling codes are not checked
        assertTrue(Phone.isValidPhone("+1" + "2".repeat(14))); // upper length boundary
    }

    @Test
    public void constructor_formattedInternationalNumber_storesCanonicalValue() {
        assertEquals("+6591234567", new Phone("+65 9123-4567").value);
    }

    @Test
    public void fromStoredValue_legacyLocalNumber_preservesOriginalValue() {
        assertEquals("91234567", Phone.fromStoredValue("91234567").value);
        assertEquals("+6591234567", Phone.fromStoredValue("+6591234567").value);
        assertEquals("+9991234567", Phone.fromStoredValue("+9991234567").value);
        assertThrows(IllegalArgumentException.class, () -> Phone.fromStoredValue("+0123456"));
    }

    @Test
    public void equals() {
        Phone phone = new Phone("+6591234567");

        // same values -> returns true
        assertTrue(phone.equals(new Phone("+65 9123-4567")));

        // same object -> returns true
        assertTrue(phone.equals(phone));

        // null -> returns false
        assertFalse(phone.equals(null));

        // different types -> returns false
        assertFalse(phone.equals(5.0f));

        // different values -> returns false
        assertFalse(phone.equals(new Phone("+6591234568")));
    }
}
