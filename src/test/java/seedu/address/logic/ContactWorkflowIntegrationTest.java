package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Person;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.PersonBuilder;

/**
 * Exercises user workflows through the real parser, model and JSON storage in an isolated directory.
 */
public class ContactWorkflowIntegrationTest {
    private static final String ADD_ALEX = "add n/Alex Tan p/+65 9123-4567 e/ALEX@Example.COM "
            + "tele/@Alex_Tan pr/ Team   Alpha";
    private static final String ADD_NAMESAKE = "add n/Alex Tan p/+65 8765-4321 e/OTHER@Example.COM "
            + "tele/@Other_Alex pr/Orbital";
    private static final Person ALEX = new PersonBuilder().withName("Alex Tan").withPhone("+6591234567")
            .withEmail("alex@example.com").withTelegramUsername("alex_tan").withProjects("Team Alpha").build();
    private static final Person NAMESAKE = new PersonBuilder().withName("Alex Tan").withPhone("+6587654321")
            .withEmail("other@example.com").withTelegramUsername("other_alex").withProjects("Orbital").build();

    @TempDir
    public Path temporaryFolder;

    private Model model;
    private Logic logic;
    private JsonAddressBookStorage storage;

    @BeforeEach
    public void setUp() {
        model = new ModelManager();
        storage = new JsonAddressBookStorage(temporaryFolder.resolve("contacts.json"));
        logic = createLogic(model, storage);
    }

    @Test
    public void execute_sameNameWorkflow_preservesIdentityAndNormalisedDataAfterReload() throws Exception {
        assertEquals("Added contact: Alex Tan [Team Alpha]", logic.execute(ADD_ALEX).getFeedbackToUser());
        assertEquals("Added contact: Alex Tan [Orbital]", logic.execute(ADD_NAMESAKE).getFeedbackToUser());
        assertSavedContacts(ALEX, NAMESAKE);
        // Person equality ignores email case, so verify the stored spelling separately.
        assertEquals("alex@example.com", storage.readAddressBook().orElseThrow()
                .getPersonList().get(0).getEmail().value);

        assertEquals("2 person(s) listed!", logic.execute("find LEX").getFeedbackToUser());
        assertEquals(List.of(ALEX, NAMESAKE), logic.getFilteredPersonList());
        logic.execute("project 2 pr/ Team   Alpha");
        Person associated = new PersonBuilder(NAMESAKE).withProjects("Orbital", "Team Alpha").build();
        assertEquals(List.of(ALEX, associated), logic.getFilteredPersonList());
        assertSavedContacts(ALEX, associated);

        logic.execute("findproject ORB");
        assertEquals(List.of(associated), logic.getFilteredPersonList());
        assertEquals("Deleted contact: Alex Tan.", logic.execute("delete 1").getFeedbackToUser());
        assertEquals(List.of(), logic.getFilteredPersonList());
        assertSavedContacts(ALEX);

        logic.execute("undo");
        assertSavedContacts(ALEX, associated);
        assertEquals(List.of(ALEX, associated), logic.getFilteredPersonList());
        reloadSession();
        assertFalse(model.canUndo());
        logic.execute("findproject team alpha");
        assertEquals(List.of(ALEX, associated), logic.getFilteredPersonList());
        assertEquals("Listed all contacts.", logic.execute("list").getFeedbackToUser());
        assertSavedContacts(ALEX, associated);
    }

    @Test
    public void execute_rejectedCommands_preserveFilterFileAndUndoHistory() throws Exception {
        logic.execute(ADD_ALEX);
        logic.execute(ADD_NAMESAKE);
        logic.execute("findproject Orbital");
        AddressBook before = new AddressBook(model.getAddressBook());
        String savedData = Files.readString(storage.getAddressBookFilePath());

        for (String command : List.of("add n/Third Person p/+6599999999 e/third@example.com tele/third_user",
                "project 1 pr/", "project 1 pr/New pr/Other", "find " + "x".repeat(61),
                "findproject " + "x".repeat(41))) {
            assertThrows(ParseException.class, () -> logic.execute(command), command);
            assertRejectedState(before, savedData);
        }

        for (String command : List.of(
                "add n/Third Person p/+65 9123-4567 e/third@example.com tele/third_user pr/New",
                "add n/Third Person p/+6599999999 e/ALEX@Example.COM tele/third_user pr/New",
                "add n/Third Person p/+6599999999 e/third@example.com tele/@ALEX_TAN pr/New",
                "edit 1 e/ALEX@Example.COM", "project 1 pr/ ORBITAL", "delete 2")) {
            assertThrows(CommandException.class, () -> logic.execute(command), command);
            assertRejectedState(before, savedData);
        }

        // Failed commands must neither replace nor consume the two successful add snapshots.
        logic.execute("undo");
        assertSavedContacts(ALEX);
        logic.execute("undo");
        assertSavedContacts();
        assertFalse(model.canUndo());
    }

    @Test
    public void execute_afterReload_undoOnlyRevertsChangesFromNewSession() throws Exception {
        logic.execute(ADD_ALEX);
        logic.execute(ADD_NAMESAKE);
        logic.execute("project 2 pr/CS2103T");
        Person savedNamesake = new PersonBuilder(NAMESAKE).withProjects("Orbital", "CS2103T").build();
        reloadSession();
        assertSavedContacts(ALEX, savedNamesake);

        logic.execute("findproject cs2103");
        String savedData = Files.readString(storage.getAddressBookFilePath());
        CommandException failure = assertThrows(CommandException.class, () -> logic.execute("undo"));
        assertEquals("No command to undo.", failure.getMessage());
        assertFalse(model.canUndo());
        assertEquals(List.of(savedNamesake), logic.getFilteredPersonList());
        assertEquals(savedData, Files.readString(storage.getAddressBookFilePath()));

        logic.execute("project 1 pr/ New   Team");
        Person updated = new PersonBuilder(NAMESAKE).withProjects("Orbital", "CS2103T", "New Team").build();
        assertSavedContacts(ALEX, updated);
        assertEquals(List.of(updated), logic.getFilteredPersonList());
        logic.execute("undo");
        assertSavedContacts(ALEX, savedNamesake);
        assertEquals(List.of(ALEX, savedNamesake), logic.getFilteredPersonList());
        assertFalse(model.canUndo());

        // Reopening the file must retain the undone state, not the most recent project addition.
        reloadSession();
        logic.execute("findproject new team");
        assertEquals(List.of(), logic.getFilteredPersonList());
        logic.execute("list");
        assertEquals(List.of(ALEX, savedNamesake), logic.getFilteredPersonList());
        assertSavedContacts(ALEX, savedNamesake);
    }

    private void assertRejectedState(AddressBook before, String savedData) throws Exception {
        assertEquals(before, model.getAddressBook());
        assertEquals(List.of(NAMESAKE), logic.getFilteredPersonList());
        assertEquals(savedData, Files.readString(storage.getAddressBookFilePath()));
    }

    private Logic createLogic(Model sessionModel, JsonAddressBookStorage sessionStorage) {
        return new LogicManager(sessionModel, new StorageManager(sessionStorage,
                new JsonUserPrefsStorage(temporaryFolder.resolve("preferences.json"))));
    }

    private void assertSavedContacts(Person... expected) throws Exception {
        assertEquals(List.of(expected), model.getAddressBook().getPersonList());
        assertEquals(List.of(expected), storage.readAddressBook().orElseThrow().getPersonList());
    }

    private void reloadSession() throws Exception {
        storage = new JsonAddressBookStorage(storage.getAddressBookFilePath());
        model = new ModelManager(storage.readAddressBook().orElseThrow(), new UserPrefs());
        logic = createLogic(model, storage);
    }
}
