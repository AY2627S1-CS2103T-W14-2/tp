package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.testutil.ConflictTestUtil.ALICE_NAMESAKE;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.BENSON;
import static seedu.address.testutil.TypicalPersons.CARL;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.NameContainsKeywordsPredicate;
import seedu.address.model.person.Person;
import seedu.address.testutil.AddressBookBuilder;
import seedu.address.testutil.PersonBuilder;

public class SortCommandTest {

    private final SortCommand sortCommand = new SortCommand();

    @Test
    public void execute_unsortedContacts_sortsByNameThenPhone() {
        Model model = modelWith(CARL, ALICE, BENSON, ALICE_NAMESAKE);
        Model expectedModel = modelWith(ALICE_NAMESAKE, ALICE, BENSON, CARL);

        assertCommandSuccess(sortCommand, model, String.format(SortCommand.MESSAGE_SUCCESS, 4), expectedModel);
    }

    @Test
    public void execute_namesWithNumbers_ordersNumbersByValue() {
        Person alexTwo = PersonBuilder.named("Alex 2").build();
        Person alexTen = PersonBuilder.named("Alex 10").build();
        Person alexYeoh = PersonBuilder.named("Alex Yeoh").build();
        Model model = modelWith(alexTen, alexYeoh, alexTwo);
        Model expectedModel = modelWith(alexTwo, alexTen, alexYeoh);

        assertCommandSuccess(sortCommand, model, String.format(SortCommand.MESSAGE_SUCCESS, 3), expectedModel);
    }

    @Test
    public void execute_activeSearch_keepsSearchResultsInNewOrder() {
        NameContainsKeywordsPredicate alicePredicate = new NameContainsKeywordsPredicate(List.of("Alice"));
        Model model = modelWith(CARL, ALICE, BENSON, ALICE_NAMESAKE);
        model.updateFilteredPersonList(alicePredicate);
        Model expectedModel = modelWith(ALICE_NAMESAKE, ALICE, BENSON, CARL);
        expectedModel.updateFilteredPersonList(alicePredicate);

        assertCommandSuccess(sortCommand, model, String.format(SortCommand.MESSAGE_SUCCESS, 4), expectedModel);
        assertEquals(List.of(ALICE_NAMESAKE, ALICE), model.getFilteredPersonList());
    }

    @Test
    public void execute_alreadySorted_throwsCommandExceptionAndKeepsOrder() {
        assertCommandFailure(sortCommand, modelWith(ALICE_NAMESAKE, ALICE, BENSON), SortCommand.MESSAGE_ALREADY_SORTED);
        // a single contact is always in order
        assertCommandFailure(sortCommand, modelWith(CARL), SortCommand.MESSAGE_ALREADY_SORTED);
    }

    @Test
    public void execute_noContacts_throwsCommandException() {
        assertCommandFailure(sortCommand, new ModelManager(), ListCommand.MESSAGE_NO_CONTACTS);
    }

    @Test
    public void isUndoable_returnsTrue() {
        assertTrue(sortCommand.isUndoable());
    }

    @Test
    public void equals() {
        assertEquals(sortCommand, new SortCommand());
        assertEquals(sortCommand.hashCode(), new SortCommand().hashCode());
        assertNotEquals(sortCommand, null);
        assertNotEquals(sortCommand, new ListCommand());
    }

    private static Model modelWith(Person... persons) {
        AddressBookBuilder addressBook = new AddressBookBuilder();
        for (Person person : persons) {
            addressBook.withPerson(person);
        }
        return new ModelManager(addressBook.build(), new UserPrefs());
    }
}
