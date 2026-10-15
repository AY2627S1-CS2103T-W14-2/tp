package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.logic.commands.CommandTestUtil.showPersonAtIndex;
import static seedu.address.testutil.ConflictTestUtil.ALICE_NAMESAKE;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_PERSON;
import static seedu.address.testutil.TypicalPersons.ALICE;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.testutil.AddressBookBuilder;

public class CommandTestUtilTest {
    @Test
    public void showPersonAtIndex_sameNameContacts_selectsOnlyRequestedContact() {
        Model model = new ModelManager(new AddressBookBuilder().withPerson(ALICE)
                .withPerson(ALICE_NAMESAKE).build(), new UserPrefs());

        showPersonAtIndex(model, INDEX_SECOND_PERSON);

        assertEquals(List.of(ALICE_NAMESAKE), model.getFilteredPersonList());
        assertEquals(List.of(ALICE, ALICE_NAMESAKE), model.getAddressBook().getPersonList());
    }
}
