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
import seedu.address.testutil.PersonUtil;

/**
 * Tests project search through command parsing, model filtering, and JSON storage.
 */
public class FindProjectCommandIntegrationTest {
    @TempDir
    public Path temporaryFolder;

    private final Person alice = new PersonBuilder().withName("Alice").withProjects("Orbital").build();
    private final Person bob = new PersonBuilder().withName("Bob")
            .withProjects("CS2103T Team Alpha", "CS2103T Team Beta").build();
    private final Person carl = new PersonBuilder().withName("Carl")
            .withProjects("Orbital", "CS2103T Team Alpha").build();
    private final Person noProject = new PersonBuilder().withName("Team Alpha").withProjects().build();

    private Model model;
    private Logic logic;
    private JsonAddressBookStorage addressBookStorage;

    @BeforeEach
    public void setUp() throws Exception {
        model = new ModelManager();
        model.addPerson(alice);
        model.addPerson(bob);
        model.addPerson(carl);
        model.addPerson(noProject);
        addressBookStorage = new JsonAddressBookStorage(temporaryFolder.resolve("addressBook.json"));
        addressBookStorage.saveAddressBook(model.getAddressBook());
        logic = new LogicManager(model, new StorageManager(addressBookStorage,
                new JsonUserPrefsStorage(temporaryFolder.resolve("userPrefs.json"))));
    }

    @Test
    public void execute_phraseSearch_displaysMatchingContactsAndPreservesStoredData() throws Exception {
        AddressBook original = new AddressBook(model.getAddressBook());
        CommandResult result = logic.execute("findproject \u00a0TEAM  \t ALPHA\u00a0");

        assertEquals("Found 2 contact(s) matching the project keyword.", result.getFeedbackToUser());
        assertEquals(List.of(bob, carl), logic.getFilteredPersonList());
        assertEquals(original, model.getAddressBook());
        assertEquals(original, addressBookStorage.readAddressBook().orElseThrow());
    }

    @Test
    public void execute_repeatedSearches_replacesPreviousFilterAndListRestoresAll() throws Exception {
        logic.execute("find Alice");
        assertEquals(List.of(alice), logic.getFilteredPersonList());

        logic.execute("findproject team alpha");
        assertEquals(List.of(bob, carl), logic.getFilteredPersonList());

        logic.execute("findproject missing project");
        assertTrue(logic.getFilteredPersonList().isEmpty());

        logic.execute("findproject ORBITAL");
        assertEquals(List.of(alice, carl), logic.getFilteredPersonList());

        logic.execute("list");
        assertEquals(List.of(alice, bob, carl, noProject), logic.getFilteredPersonList());
    }

    @Test
    public void execute_deleteAfterSearch_deletesDisplayedContactOnly() throws Exception {
        logic.execute("findproject team alpha");
        logic.execute("delete 1");

        assertEquals(List.of(carl), logic.getFilteredPersonList());
        assertEquals(List.of(alice, carl, noProject), model.getAddressBook().getPersonList());
        assertEquals(model.getAddressBook(), addressBookStorage.readAddressBook().orElseThrow());

        logic.execute("list");
        assertEquals(List.of(alice, carl, noProject), logic.getFilteredPersonList());
    }

    @Test
    public void execute_invalidKeyword_preservesResultsAndStoredData() throws Exception {
        logic.execute("findproject team alpha");
        AddressBook original = new AddressBook(model.getAddressBook());
        String savedData = Files.readString(addressBookStorage.getAddressBookFilePath());

        for (String input : List.of("findproject", "findproject \u00a0\u2003",
                "findproject \u00a0" + Character.toString(0x1c) + "\u00a0",
                "findproject " + "x".repeat(41))) {
            assertThrows(ParseException.class, () -> logic.execute(input));
            assertEquals(List.of(bob, carl), logic.getFilteredPersonList());
            assertEquals(original, model.getAddressBook());
            assertEquals(savedData, Files.readString(addressBookStorage.getAddressBookFilePath()));
        }
    }

    @Test
    public void execute_addWithProjectThenSearch_findsNewContact() throws Exception {
        Person newContact = new PersonBuilder().withName("Dina").withProjects("Orbital Team Delta").build();
        logic.execute(PersonUtil.getAddCommand(newContact));
        logic.execute("findproject TEAM DEL");

        assertEquals(List.of(newContact), logic.getFilteredPersonList());
        assertTrue(addressBookStorage.readAddressBook().orElseThrow().getPersonList().contains(newContact));
    }

    @Test
    public void execute_maxLengthKeyword_matchesProjectName() throws Exception {
        String keyword = "a".repeat(40);
        Person newContact = new PersonBuilder().withName("Dina").withProjects(keyword).build();
        logic.execute(PersonUtil.getAddCommand(newContact));

        logic.execute("findproject " + keyword);
        assertEquals(List.of(newContact), logic.getFilteredPersonList());
        assertThrows(ParseException.class, () -> logic.execute("findproject " + keyword + "b"));
        assertEquals(List.of(newContact), logic.getFilteredPersonList());
    }

    @Test
    public void execute_punctuationInPhrase_matchesLiterally() throws Exception {
        Person literal = new PersonBuilder().withName("Dina").withProjects("C++ [Team]/Alpha").build();
        Person different = new PersonBuilder().withName("Ella").withProjects("C Team Alpha").build();
        logic.execute(PersonUtil.getAddCommand(literal));
        logic.execute(PersonUtil.getAddCommand(different));

        logic.execute("findproject ++ [team]/");
        assertEquals(List.of(literal), logic.getFilteredPersonList());
    }

    @Test
    public void execute_editAfterSearch_preservesProjectsAndUsesDisplayedIndex() throws Exception {
        logic.execute("findproject team alpha");
        logic.execute("edit 1 p/91234567");
        Person editedBob = new PersonBuilder(bob).withPhone("91234567").build();

        logic.execute("findproject team alpha");
        assertEquals(List.of(editedBob, carl), logic.getFilteredPersonList());
        assertEquals(List.of(alice, editedBob, carl, noProject), model.getAddressBook().getPersonList());
        assertEquals(model.getAddressBook(), addressBookStorage.readAddressBook().orElseThrow());
    }
}
