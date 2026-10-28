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
    public void isValidTelegramUsername_breaksTelegramRules_returnsFalse() {
        // Issue #189: Telegram itself rejects these
        assertFalse(TelegramUsername.isValidTelegramUsername("_____"));
        assertFalse(TelegramUsername.isValidTelegramUsername("12345"));
        assertFalse(TelegramUsername.isValidTelegramUsername("1alex"));
        assertFalse(TelegramUsername.isValidTelegramUsername("_alex"));
        assertFalse(TelegramUsername.isValidTelegramUsername("alex_"));
        assertFalse(TelegramUsername.isValidTelegramUsername("@1alex"));
        assertFalse(TelegramUsername.isValidTelegramUsername("@Alex_"));
        assertFalse(TelegramUsername.isValidTelegramUsername("ale_"));
        assertFalse(TelegramUsername.isValidTelegramUsername("a" + "_".repeat(31)));
        assertFalse(TelegramUsername.isValidTelegramUsername("a" + "b".repeat(32)));
    }

    @Test
    public void isValidTelegramUsername_followsTelegramRules_returnsTrue() {
        assertTrue(TelegramUsername.isValidTelegramUsername("alex1"));
        assertTrue(TelegramUsername.isValidTelegramUsername("a___1"));
        assertTrue(TelegramUsername.isValidTelegramUsername("al__ex"));
        assertTrue(TelegramUsername.isValidTelegramUsername("Alex_T"));
        assertTrue(TelegramUsername.isValidTelegramUsername("a" + "_".repeat(30) + "b"));
    }

    @Test
    public void constructor_breaksTelegramRules_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, TelegramUsername.MESSAGE_CONSTRAINTS, () ->
                new TelegramUsername("alex_"));
        assertThrows(IllegalArgumentException.class, TelegramUsername.MESSAGE_CONSTRAINTS, () ->
                new TelegramUsername("1alex"));
    }

    @Test
    public void isValidStoredTelegramUsername() {
        // saved before v1.5, under the older rule
        assertTrue(TelegramUsername.isValidStoredTelegramUsername("alex_"));
        assertTrue(TelegramUsername.isValidStoredTelegramUsername("1alex"));
        assertTrue(TelegramUsername.isValidStoredTelegramUsername("_____"));
        assertTrue(TelegramUsername.isValidStoredTelegramUsername("alex_tan"));

        // never valid
        assertFalse(TelegramUsername.isValidStoredTelegramUsername("abcd"));
        assertFalse(TelegramUsername.isValidStoredTelegramUsername("alex-tan"));
        assertFalse(TelegramUsername.isValidStoredTelegramUsername("a".repeat(33)));
    }

    @Test
    public void fromStoredValue_legacyUsername_keepsValue() {
        TelegramUsername username = TelegramUsername.fromStoredValue("alex_");

        assertEquals("alex_", username.value);
        assertEquals("@alex_", username.toString());
        assertEquals(username, TelegramUsername.fromStoredValue("@Alex_"));
    }

    @Test
    public void fromStoredValue_invalidUsername_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> TelegramUsername.fromStoredValue("alex-tan"));
    }

    @Test
    public void equals_caseAndAtDifferences_returnsTrue() {
        TelegramUsername username = new TelegramUsername("alice_nus");
        TelegramUsername usernameWithAtAndUppercase = new TelegramUsername("@Alice_NUS");

        assertEquals(username, usernameWithAtAndUppercase);
    }
}
