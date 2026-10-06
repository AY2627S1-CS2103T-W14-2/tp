package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

public class TelegramUsernameTest {

    @Test
    public void constructor_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new TelegramUsername(null));
    }

    @Test
    public void constructor_invalidTelegramUsername_throwsIllegalArgumentException() {
        String invalidTelegramUsername = "tele!";
        assertThrows(IllegalArgumentException.class, () -> new TelegramUsername(invalidTelegramUsername));
    }

    @Test
    public void constructor_validTelegramUsername_normalizesUsername() {
        TelegramUsername telegramUsername = new TelegramUsername("  @Alice_NUS  ");

        assertEquals("alice_nus", telegramUsername.value);
        assertEquals("@alice_nus", telegramUsername.toString());
    }

    @Test
    public void isValidTelegramUsername() {
        assertFalse(TelegramUsername.isValidTelegramUsername(""));
        assertFalse(TelegramUsername.isValidTelegramUsername("abcd"));
        assertFalse(TelegramUsername.isValidTelegramUsername("abcdefghijklmnopqrstuvwxyz1234567"));
        assertFalse(TelegramUsername.isValidTelegramUsername("alice-nus"));
        assertFalse(TelegramUsername.isValidTelegramUsername("@alice nus"));

        assertTrue(TelegramUsername.isValidTelegramUsername("alice"));
        assertTrue(TelegramUsername.isValidTelegramUsername("@Alice_NUS"));
        assertTrue(TelegramUsername.isValidTelegramUsername("abcde_12345"));
        assertTrue(TelegramUsername.isValidTelegramUsername("abcdefghijklmnopqrstuvwxyz123456"));
    }

    @Test
    public void equals_caseAndAtDifferences_returnsTrue() {
        TelegramUsername username = new TelegramUsername("alice_nus");
        TelegramUsername usernameWithAtAndUppercase = new TelegramUsername("@Alice_NUS");

        assertEquals(username, usernameWithAtAndUppercase);
    }
}
