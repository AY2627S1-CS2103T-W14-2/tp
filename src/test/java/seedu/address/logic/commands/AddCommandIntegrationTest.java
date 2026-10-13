package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.testutil.ConflictTestUtil.ALICE_NAMESAKE;
import static seedu.address.testutil.ConflictTestUtil.clashesWith;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.BOB;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import seedu.address.logic.Messages;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Person;
import seedu.address.testutil.ConflictTestUtil.Clash;
import seedu.address.testutil.PersonBuilder;

/**
 * Contains integration tests (interaction with the Model) for {@code AddCommand}.
 */
public class AddCommandIntegrationTest {

    private Model model;

    @BeforeEach
    public void setUp() {
        model = new ModelManager(getTypicalAddressBook(), new UserPrefs());
    }

    @Test
    public void execute_newPerson_success() {
        Person validPerson = new PersonBuilder().withProjects("CS2103T").build();

        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        expectedModel.addPerson(validPerson);

        assertCommandSuccess(new AddCommand(validPerson), model,
                String.format(AddCommand.MESSAGE_SUCCESS, Messages.format(validPerson)),
                expectedModel);
    }

    @Test
    public void execute_sameNameDistinctDetails_success() {
        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        expectedModel.addPerson(ALICE_NAMESAKE);

        assertCommandSuccess(new AddCommand(ALICE_NAMESAKE), model,
                String.format(AddCommand.MESSAGE_SUCCESS, Messages.format(ALICE_NAMESAKE)), expectedModel);
    }

    @Test
    public void execute_samePerson_throwsCommandException() {
        assertCommandFailure(new AddCommand(ALICE), model, "Phone number 94351253 is already used by Alice Pauline. "
                + "Please enter a different phone number.");
    }

    @Test
    public void execute_clashingPerson_throwsCommandExceptionAndKeepsUndoState() {
        // In the order of ConflictTestUtil#clashesWith: phone, email, Telegram username
        List<String> expectedMessages = List.of(
                "Phone number 94351253 is already used by Alice Pauline. Please enter a different phone number.",
                "Email ALICE@EXAMPLE.COM is already used by Alice Pauline. Please enter a different email.",
                "Telegram username @alice_pauline is already used by Alice Pauline. "
                        + "Please enter a different Telegram username.");
        List<Clash> clashes = clashesWith(ALICE, BOB);

        for (int i = 0; i < clashes.size(); i++) {
            assertCommandFailure(new AddCommand(clashes.get(i).person()), model, expectedMessages.get(i));
        }
        assertFalse(model.canUndo());
    }

}
