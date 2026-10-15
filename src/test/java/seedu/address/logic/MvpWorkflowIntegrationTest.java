package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

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
public class MvpWorkflowIntegrationTest {
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
