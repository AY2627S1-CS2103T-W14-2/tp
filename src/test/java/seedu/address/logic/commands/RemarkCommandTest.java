package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.Messages;
import seedu.address.logic.parser.AddressBookParser;
import seedu.address.model.ModelManager;
import seedu.address.model.person.Person;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.testutil.PersonBuilder;

public class RemarkCommandTest {

    @TempDir
    public Path testFolder;

    private final AddressBookParser parser = new AddressBookParser();

    @Test
    public void execute_addReplaceRemove_preservesOtherDetails() throws Exception {
        ModelManager model = new ModelManager();
        Person original = new PersonBuilder().build();
        model.addPerson(original);
        CommandResult result = parser.parseCommand("remark 1 r/Likes swimming").execute(model);
        Person expected = new PersonBuilder(original).withRemark("Likes swimming").build();
        assertEquals(expected, model.getFilteredPersonList().getFirst());
        assertEquals(String.format(RemarkCommand.MESSAGE_ADD_REMARK_SUCCESS, Messages.format(expected)),
                result.getFeedbackToUser());
        assertTrue(original.isSamePerson(expected));
        assertNotEquals(original, expected);

        parser.parseCommand("remark 1 r/Likes hiking").execute(model);
        assertEquals(new PersonBuilder(original).withRemark("Likes hiking").build(),
                model.getFilteredPersonList().getFirst());

        result = parser.parseCommand("remark 1 r/").execute(model);
        assertEquals(original, model.getFilteredPersonList().getFirst());
        assertEquals(String.format(RemarkCommand.MESSAGE_DELETE_REMARK_SUCCESS, Messages.format(original)),
                result.getFeedbackToUser());
    }

    @Test
    public void execute_filteredList_updatesDisplayedPerson() throws Exception {
        ModelManager model = new ModelManager();
        Person amy = new PersonBuilder().withName("Amy").build();
        Person bob = new PersonBuilder().withName("Bob").build();
        model.addPerson(amy);
        model.addPerson(bob);
        parser.parseCommand("find Bob").execute(model);
        parser.parseCommand("remark 1 r/A note for Bob").execute(model);
        assertEquals(amy, model.getFilteredPersonList().get(0));
        assertEquals(new PersonBuilder(bob).withRemark("A note for Bob").build(),
                model.getFilteredPersonList().get(1));
    }

    @Test
    public void execute_outOfRangeIndex_doesNotChangeModel() throws Exception {
        ModelManager model = new ModelManager();
        Person original = new PersonBuilder().build();
        model.addPerson(original);
        Command command = parser.parseCommand("remark 2 r/Invalid target");
        var exception = assertThrows(seedu.address.logic.commands.exceptions.CommandException.class, () ->
                command.execute(model));
        assertEquals(Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX, exception.getMessage());
        assertEquals(original, model.getFilteredPersonList().getFirst());
    }

    @Test
    public void execute_editOtherFields_preservesRemark() throws Exception {
        ModelManager model = new ModelManager();
        model.addPerson(new PersonBuilder().withRemark("Keep this note").build());
        parser.parseCommand("edit 1 n/New Name").execute(model);
        assertEquals("Keep this note", model.getFilteredPersonList().getFirst().getRemark().value);
        assertEquals("New Name", model.getFilteredPersonList().getFirst().getName().fullName);
    }

    @Test
    public void saveAndReload_preservesRemark() throws Exception {
        ModelManager model = new ModelManager();
        model.addPerson(new PersonBuilder().build());
        parser.parseCommand("remark 1 r/Likes swimming").execute(model);
        JsonAddressBookStorage storage = new JsonAddressBookStorage(testFolder.resolve("addressbook.json"));
        storage.saveAddressBook(model.getAddressBook());
        assertEquals(model.getAddressBook(), storage.readAddressBook().orElseThrow());
    }
}
