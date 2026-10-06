package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.BENSON;

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

    @Test
    public void format_personWithProjects_includesProjectsInSavedOrder() {
        String formattedPerson = Messages.format(BENSON);

        assertTrue(formattedPerson.contains("; Projects: CS2103T, Orbital"));
    }

    @Test
    public void format_personWithoutProjects_omitsProjectsField() {
        assertFalse(Messages.format(ALICE).contains("Projects:"));
    }
}
