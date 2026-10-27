package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

public class NameTest {

    @Test
    public void constructor_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new Name(null));
    }

    @Test
    public void constructor_invalidName_throwsIllegalArgumentException() {
        String invalidName = "";
        assertThrows(IllegalArgumentException.class, () -> new Name(invalidName));
    }

    @Test
    public void isValidName() {
        // null name
        assertThrows(NullPointerException.class, () -> Name.isValidName(null));

        // invalid name
        assertFalse(Name.isValidName("")); // empty string
        assertFalse(Name.isValidName(" ")); // spaces only
        assertFalse(Name.isValidName("^")); // only non-alphanumeric characters
        assertFalse(Name.isValidName("peter*")); // contains non-alphanumeric characters
        assertFalse(Name.isValidName("12345")); // must contain a letter
        assertFalse(Name.isValidName("Émile")); // only English letters are accepted for new input
        assertFalse(Name.isValidName("A".repeat(61))); // exceeds the 60-character limit
        assertFalse(Name.isValidName("A".repeat(31) + "  " + "B".repeat(30))); // 61 after collapsing spaces

        // valid name
        assertTrue(Name.isValidName("peter jack")); // alphabets only
        assertTrue(Name.isValidName("peter the 2nd")); // alphanumeric characters
        assertTrue(Name.isValidName("Capital Tan")); // with capital letters
        assertTrue(Name.isValidName("David Roger Jackson Ray Jr 2nd")); // long names
        assertTrue(Name.isValidName("O'Connor-Smith Jr. 2")); // permitted punctuation
        assertTrue(Name.isValidName("A".repeat(60))); // upper length boundary
        assertTrue(Name.isValidName("  Alex Tan  ")); // surrounding spaces are trimmed
        assertTrue(Name.isValidName("A\tTan")); // a tab becomes one space
        assertTrue(Name.isValidName("A".repeat(30) + "    " + "B".repeat(29))); // 60 after collapsing spaces
    }

    @Test
    public void constructor_trimsSurroundingSpaces_preservesCapitalization() {
        assertEquals("Alex Tan", new Name("  Alex Tan  ").fullName);
    }

    @Test
    public void constructor_repeatedWhitespace_collapsesToOneSpace() {
        assertEquals("Emma First", new Name("Emma   First").fullName);
        assertEquals("Emma First", new Name("Emma\tFirst").fullName);
        assertEquals("Emma First", new Name("Emma First").fullName); // non-breaking space from copy-paste
        assertEquals("Mary Jane O'Neil", new Name(" Mary \t Jane  O'Neil ").fullName);
        assertEquals(new Name("Emma First"), new Name("Emma  First"));
    }

    @Test
    public void fromStoredValue_oldValidName_preservesOriginalValue() {
        assertEquals("12345 ", Name.fromStoredValue("12345 ").fullName);
        assertEquals("A".repeat(61), Name.fromStoredValue("A".repeat(61)).fullName);
        assertEquals("Emma  First", Name.fromStoredValue("Emma  First").fullName); // saved spacing is kept
        assertThrows(IllegalArgumentException.class, () -> Name.fromStoredValue("R@chel"));
        assertThrows(IllegalArgumentException.class, () -> Name.fromStoredValue("A\tTan")); // loading is unchanged
    }

    @Test
    public void equals() {
        Name name = new Name("Valid Name");

        // same values -> returns true
        assertTrue(name.equals(new Name("Valid Name")));

        // same object -> returns true
        assertTrue(name.equals(name));

        // null -> returns false
        assertFalse(name.equals(null));

        // different types -> returns false
        assertFalse(name.equals(5.0f));

        // different values -> returns false
        assertFalse(name.equals(new Name("Other Valid Name")));
    }
}
