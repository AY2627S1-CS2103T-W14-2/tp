package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

import seedu.address.model.person.ContactConflict.Field;
import seedu.address.testutil.PersonBuilder;

public class ContactConflictTest {

    private static final Person ALEX = new PersonBuilder().withName("Alex Yeoh").build();

    @Test
    public void constructor_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new ContactConflict(null, "91234567", ALEX));
        assertThrows(NullPointerException.class, () -> new ContactConflict(Field.PHONE, null, ALEX));
        assertThrows(NullPointerException.class, () -> new ContactConflict(Field.PHONE, "91234567", null));
    }

    @Test
    public void getMessage_eachField_namesFieldValueAndExistingContact() {
        assertMessage("Phone number 91234567 is already used by Alex Yeoh. Please enter a different phone number.",
                Field.PHONE, "91234567");
        assertMessage("Email alex@example.com is already used by Alex Yeoh. Please enter a different email.",
                Field.EMAIL, "alex@example.com");
        assertMessage("Telegram username @alex_yeoh is already used by Alex Yeoh. "
                + "Please enter a different Telegram username.", Field.TELEGRAM_USERNAME, "@alex_yeoh");
    }

    private static void assertMessage(String expectedMessage, Field field, String value) {
        assertEquals(expectedMessage, new ContactConflict(field, value, ALEX).getMessage());
    }
}
