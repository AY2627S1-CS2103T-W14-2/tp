package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.ConflictTestUtil.ALICE_NAMESAKE;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.BENSON;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.commands.ListCommand;
import seedu.address.logic.commands.SortCommand;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.person.Person;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;

/**
 * Tests {@code sort} through parsing, execution, JSON storage and undo.
 */
public class SortCommandIntegrationTest {

    private static final List<Person> UNSORTED = List.of(BENSON, ALICE, ALICE_NAMESAKE);
    private static final List<Person> SORTED = List.of(ALICE_NAMESAKE, ALICE, BENSON);

    @TempDir
    public Path temporaryFolder;

    private final Model model = new ModelManager();
    private JsonAddressBookStorage addressBookStorage;
    private Logic logic;

    @BeforeEach
    public void setUp() {
        addressBookStorage = new JsonAddressBookStorage(temporaryFolder.resolve("addressBook.json"));
        logic = new LogicManager(model, new StorageManager(addressBookStorage,
                new JsonUserPrefsStorage(temporaryFolder.resolve("userPrefs.json"))));
    }

    @Test
    public void execute_sort_savesNewOrderAndUndoRestoresPreviousOrder() throws Exception {
        UNSORTED.forEach(model::addPerson);

        assertEquals(String.format(SortCommand.MESSAGE_SUCCESS, 3), logic.execute("sort").getFeedbackToUser());
        assertEquals(SORTED, savedContacts());

        logic.execute("undo");
        assertEquals(UNSORTED, model.getAddressBook().getPersonList());
        assertEquals(UNSORTED, savedContacts());
        assertFalse(model.canUndo()); // sorting created exactly one undo step
    }

    @Test
    public void execute_alreadySorted_keepsSavedFileAndUndoHistory() throws Exception {
        SORTED.forEach(model::addPerson);
        logic.execute("list"); // saves the current contacts
        String savedData = Files.readString(addressBookStorage.getAddressBookFilePath());

        assertThrows(CommandException.class, SortCommand.MESSAGE_ALREADY_SORTED, () -> logic.execute("sort"));

        assertEquals(savedData, Files.readString(addressBookStorage.getAddressBookFilePath()));
        assertFalse(model.canUndo());
    }

    @Test
    public void execute_alreadySortedWithUndoHistory_keepsExistingHistory() throws Exception {
        SORTED.forEach(model::addPerson);
        logic.execute("delete 3"); // creates one undo entry
        List<Person> afterDelete = List.copyOf(model.getAddressBook().getPersonList());

        assertThrows(CommandException.class, SortCommand.MESSAGE_ALREADY_SORTED, () -> logic.execute("sort"));

        assertEquals(afterDelete, model.getAddressBook().getPersonList());
        logic.execute("undo"); // undoes the delete, not the rejected sort
        assertEquals(SORTED, model.getAddressBook().getPersonList());
        assertFalse(model.canUndo());
    }

    @Test
    public void execute_noContacts_keepsSavedFileAndUndoHistory() throws Exception {
        logic.execute("list"); // saves the empty address book
        String savedData = Files.readString(addressBookStorage.getAddressBookFilePath());

        assertThrows(CommandException.class, ListCommand.MESSAGE_NO_CONTACTS, () -> logic.execute("sort"));

        assertEquals(savedData, Files.readString(addressBookStorage.getAddressBookFilePath()));
        assertFalse(model.canUndo());
    }

    @Test
    public void execute_deleteAfterSort_usesNewIndices() throws Exception {
        UNSORTED.forEach(model::addPerson);

        logic.execute("sort");
        logic.execute("delete 1");

        assertEquals(List.of(ALICE, BENSON), model.getAddressBook().getPersonList());
    }

    @Test
    public void execute_sortAfterFind_keepsSearchResults() throws Exception {
        UNSORTED.forEach(model::addPerson);

        logic.execute("find Alice");
        logic.execute("sort");

        assertEquals(List.of(ALICE_NAMESAKE, ALICE), logic.getFilteredPersonList());
    }

    @Test
    public void execute_extraText_ignoredLikeOtherParameterlessCommands() throws Exception {
        UNSORTED.forEach(model::addPerson);

        logic.execute("sort by phone please");

        assertEquals(SORTED, model.getAddressBook().getPersonList());
    }

    private List<Person> savedContacts() throws Exception {
        return addressBookStorage.readAddressBook().orElseThrow().getPersonList();
    }
}
