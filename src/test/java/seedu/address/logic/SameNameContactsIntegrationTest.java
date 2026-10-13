package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.testutil.ConflictTestUtil.ALICE_NAMESAKE;
import static seedu.address.testutil.TypicalPersons.ALICE;

import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.person.Person;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.PersonBuilder;

/**
 * Tests that commands act on the right contact when two contacts share a name.
 * {@code ALICE} has no projects and {@code ALICE_NAMESAKE} is in the Orbital project.
 */
public class SameNameContactsIntegrationTest {

    @TempDir
    public Path temporaryFolder;

    private final Model model = new ModelManager();
    private Logic logic;

    @BeforeEach
    public void setUp() {
        model.addPerson(ALICE);
        model.addPerson(ALICE_NAMESAKE);
        logic = new LogicManager(model, new StorageManager(
                new JsonAddressBookStorage(temporaryFolder.resolve("addressBook.json")),
                new JsonUserPrefsStorage(temporaryFolder.resolve("userPrefs.json"))));
    }

    @Test
    public void execute_find_listsBothContacts() throws Exception {
        logic.execute("find Alice");
        assertEquals(List.of(ALICE, ALICE_NAMESAKE), model.getFilteredPersonList());
    }

    @Test
    public void execute_deleteSecond_deletesOnlySecondContact() throws Exception {
        logic.execute("delete 2");
        assertEquals(List.of(ALICE), model.getAddressBook().getPersonList());
    }

    @Test
    public void execute_projectFirst_updatesOnlyFirstContact() throws Exception {
        logic.execute("project 1 pr/CS2101");
        Person expectedAlice = new PersonBuilder(ALICE).withProjects("CS2101").build();
        assertEquals(List.of(expectedAlice, ALICE_NAMESAKE), model.getAddressBook().getPersonList());
    }

    @Test
    public void execute_findProjectThenDelete_deletesOnlyDisplayedContact() throws Exception {
        logic.execute("findproject Orbital");
        assertEquals(List.of(ALICE_NAMESAKE), model.getFilteredPersonList());

        logic.execute("delete 1");

        assertEquals(List.of(ALICE), model.getAddressBook().getPersonList());
    }
}
