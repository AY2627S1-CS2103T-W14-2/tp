package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

public class EmailTest {

    @Test
    public void constructor_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new Email(null));
    }

    @Test
    public void constructor_invalidEmail_throwsIllegalArgumentException() {
        String invalidEmail = "";
        assertThrows(IllegalArgumentException.class, () -> new Email(invalidEmail));
    }

    @Test
    public void isValidEmail() {
        // null email
        assertThrows(NullPointerException.class, () -> Email.isValidEmail(null));

        // blank email
        assertFalse(Email.isValidEmail("")); // empty string
        assertFalse(Email.isValidEmail(" ")); // spaces only

        // missing parts
        assertFalse(Email.isValidEmail("@example.com")); // missing local part
        assertFalse(Email.isValidEmail("peterjackexample.com")); // missing '@' symbol
        assertFalse(Email.isValidEmail("peterjack@")); // missing domain name

        // invalid parts
        assertFalse(Email.isValidEmail("peterjack@example")); // domain needs a dot
        assertFalse(Email.isValidEmail("peter jack@example.com")); // spaces in local part
        assertFalse(Email.isValidEmail("peterjack@exam ple.com")); // spaces in domain name
        assertFalse(Email.isValidEmail("peterjack@exam\tple.com")); // tabs in domain name
        assertFalse(Email.isValidEmail("peterjack@exam\u0000ple.com")); // control characters
        assertFalse(Email.isValidEmail(" peterjack@example.com")); // leading space
        assertFalse(Email.isValidEmail("peterjack@example.com ")); // trailing space
        assertFalse(Email.isValidEmail("peterjack@@example.com")); // double '@' symbol
        assertFalse(Email.isValidEmail("peter@jack@example.com")); // '@' symbol in local part
        assertFalse(Email.isValidEmail("peterjack@example@com")); // '@' symbol in domain name
        assertFalse(Email.isValidEmail("peterjack@.example.com")); // domain name starts with a period
        assertFalse(Email.isValidEmail("peterjack@example.com.")); // domain name ends with a period
        assertFalse(Email.isValidEmail("peterjack@example..com")); // empty internal domain label
        assertFalse(Email.isValidEmail("mailto:alex@example.com")); // URI prefix is not an email local part
        assertFalse(Email.isValidEmail("alex@example.com,")); // punctuation copied after an address
        assertFalse(Email.isValidEmail("alex@example.com;")); // punctuation copied after an address
        assertFalse(Email.isValidEmail("<alex@example.com>")); // angle-bracket display format
        assertFalse(Email.isValidEmail("alex(comment)@example.com")); // comments are unsupported
        assertFalse(Email.isValidEmail("alex@-example.com")); // domain label starts with hyphen
        assertFalse(Email.isValidEmail("alex@example-.com")); // domain label ends with hyphen
        assertFalse(Email.isValidEmail("alex@exa_mple.com")); // underscore is not a domain character
        assertFalse(Email.isValidEmail(".alex@example.com")); // leading local-part dot
        assertFalse(Email.isValidEmail("alex.@example.com")); // trailing local-part dot
        assertFalse(Email.isValidEmail("al..ex@example.com")); // repeated local-part dot

        // valid email
        assertTrue(Email.isValidEmail("PeterJack_1190@example.com")); // underscore in local part
        assertTrue(Email.isValidEmail("PeterJack.1190@example.com")); // period in local part
        assertTrue(Email.isValidEmail("PeterJack+1190@example.com")); // '+' symbol in local part
        assertTrue(Email.isValidEmail("PeterJack-1190@example.com")); // hyphen in local part
        assertTrue(Email.isValidEmail("a@b.c")); // minimal structure
        assertTrue(Email.isValidEmail("a!b@example.com")); // broader local part
        assertTrue(Email.isValidEmail("alex+project@sub.example.com")); // common project alias
        assertTrue(Email.isValidEmail("123@145.com")); // numeric local part
        assertTrue(Email.isValidEmail("a1+be.d@example1.com")); // mixture of alphanumeric and special characters
        assertTrue(Email.isValidEmail("peter_jack@very-very-very-long-example.com")); // long domain name
        assertTrue(Email.isValidEmail("if.you.dream.it_you.can.do.it@example.com")); // long local part
        assertTrue(Email.isValidEmail("e1234567@u.nus.edu")); // more than one period in domain
    }

    @Test
    public void constructor_newEmail_storesLowercase() {
        assertEquals("alex.tan@example.com", new Email("Alex.Tan@Example.COM").value);
    }

    @Test
    public void fromStoredValue_oldValidEmail_preservesOriginalValue() {
        assertEquals("Alex@Example", Email.fromStoredValue("Alex@Example").value);
        assertEquals("Alex@Example.COM", Email.fromStoredValue("Alex@Example.COM").value);
        assertThrows(IllegalArgumentException.class, () -> Email.fromStoredValue("not-an-email"));
    }

    @Test
    public void equals() {
        Email email = new Email("valid@email.com");

        // same values -> returns true
        assertTrue(email.equals(new Email("valid@email.com")));

        // same object -> returns true
        assertTrue(email.equals(email));

        // null -> returns false
        assertFalse(email.equals(null));

        // different types -> returns false
        assertFalse(email.equals(5.0f));

        // different values -> returns false
        assertFalse(email.equals(new Email("other.valid@email.com")));
    }
}
