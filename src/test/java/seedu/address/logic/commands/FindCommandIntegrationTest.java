package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.Logic;
import seedu.address.logic.LogicManager;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.person.Person;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.PersonBuilder;

/**
 * Tests partial-name search through parsing, filtering, displayed indices, and JSON storage.
 */
public class FindCommandIntegrationTest {
    @TempDir
    public Path temporaryFolder;

    private final Person bob = new PersonBuilder().named("Bob Lee").withProjects("Orbital").build();
    private final Person alex = new PersonBuilder().named("Alex Tan").withProjects("CS2103T").build();
    private final Person alexander = new PersonBuilder().named("Alexander Lee")
            .withProjects("Orbital", "CS2103T").build();

    private Model model;
    private Logic logic;
    private JsonAddressBookStorage addressBookStorage;

    @BeforeEach
    public void setUp() throws Exception {
        model = new ModelManager();
        model.addPerson(bob);
        model.addPerson(alex);
        model.addPerson(alexander);
        addressBookStorage = new JsonAddressBookStorage(temporaryFolder.resolve("addressBook.json"));
        addressBookStorage.saveAddressBook(model.getAddressBook());
        logic = new LogicManager(model, new StorageManager(addressBookStorage,
                new JsonUserPrefsStorage(temporaryFolder.resolve("userPrefs.json"))));
    }

    @Test
    public void execute_partialSearch_preservesContactDetailsProjectsAndSavedData() throws Exception {
        AddressBook original = new AddressBook(model.getAddressBook());
        String savedData = Files.readString(addressBookStorage.getAddressBookFilePath());

        CommandResult result = logic.execute("find LEX");

        assertEquals("2 contact(s) listed!", result.getFeedbackToUser());
        assertEquals(List.of(alex, alexander), logic.getFilteredPersonList());
        assertEquals(original, model.getAddressBook());
        assertEquals(original, addressBookStorage.readAddressBook().orElseThrow());
        assertEquals(savedData, Files.readString(addressBookStorage.getAddressBookFilePath()));
    }

    @Test
    public void execute_multipleAndRepeatedKeywords_preservesOrderWithoutDuplicates() throws Exception {
        logic.execute("find LEX bo lex");
        assertEquals(List.of(bob, alex, alexander), logic.getFilteredPersonList());
    }

    @Test
    public void execute_repeatedSearches_replacesFilterAndListRestoresAll() throws Exception {
        logic.execute("findproject orbital");
        assertEquals(List.of(bob, alexander), logic.getFilteredPersonList());

        logic.execute("find ale");
        assertEquals(List.of(alex, alexander), logic.getFilteredPersonList());

        CommandResult result = logic.execute("find nobody");
        assertEquals("0 contact(s) listed!", result.getFeedbackToUser());
        assertTrue(logic.getFilteredPersonList().isEmpty());

        logic.execute("find bo");
        assertEquals(List.of(bob), logic.getFilteredPersonList());

        logic.execute("list");
        assertEquals(List.of(bob, alex, alexander), logic.getFilteredPersonList());
    }

    @Test
    public void execute_invalidInput_preservesResultsAndSavedData() throws Exception {
        logic.execute("find ale");
        AddressBook original = new AddressBook(model.getAddressBook());
        String savedData = Files.readString(addressBookStorage.getAddressBookFilePath());

        for (String input : List.of("find", "find \t\u00a0\u2003", "find " + "a".repeat(61),
                "find " + "a".repeat(29) + "  " + "b".repeat(30))) {
            assertThrows(ParseException.class, () -> logic.execute(input));
            assertEquals(List.of(alex, alexander), logic.getFilteredPersonList());
            assertEquals(original, model.getAddressBook());
            assertEquals(savedData, Files.readString(addressBookStorage.getAddressBookFilePath()));
        }
    }

    @Test
    public void execute_deleteAfterPartialSearch_deletesDisplayedContactOnly() throws Exception {
        logic.execute("find ale");
        logic.execute("delete 1");

        assertEquals(List.of(alexander), logic.getFilteredPersonList());
        assertEquals(List.of(bob, alexander), model.getAddressBook().getPersonList());
        assertEquals(model.getAddressBook(), addressBookStorage.readAddressBook().orElseThrow());
    }

    @Test
    public void execute_editAfterPartialSearch_editsDisplayedContactAndPreservesProjects() throws Exception {
        logic.execute("find ale");
        logic.execute("edit 1 p/+6591234567");
        Person editedAlex = new PersonBuilder(alex).withPhone("+6591234567").build();

        logic.execute("find ale");
        assertEquals(List.of(editedAlex, alexander), logic.getFilteredPersonList());
        assertEquals(List.of(bob, editedAlex, alexander), model.getAddressBook().getPersonList());
        assertEquals(model.getAddressBook(), addressBookStorage.readAddressBook().orElseThrow());
    }
}
