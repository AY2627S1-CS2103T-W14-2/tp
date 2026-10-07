package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.TypicalPersons.ALICE;

import org.junit.jupiter.api.Test;

import seedu.address.model.person.Person;
import seedu.address.model.person.TelegramUsername;

public class MessagesTest {

    @Test
    public void format_personWithTelegramUsername_includesTelegramUsername() {
        assertTrue(Messages.format(ALICE).contains("; Telegram: " + ALICE.getTelegramUsername()));
    }

    @Test
    public void format_legacyPersonWithoutTelegramUsername_omitsTelegramField() {
        Person legacyPerson = new Person(ALICE.getName(), ALICE.getPhone(), ALICE.getEmail(), TelegramUsername.EMPTY,
                ALICE.getAddress(), ALICE.getTags());

        assertFalse(Messages.format(legacyPerson).contains("Telegram:"));
    }
}
