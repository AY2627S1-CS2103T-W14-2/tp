package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.person.Person;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.PersonBuilder;

/**
 * Tests project association through parsing, execution and JSON persistence.
 */
public class ProjectCommandIntegrationTest {
    @TempDir
    public Path temporaryFolder;

    @Test
    public void execute_filteredContact_persistsAssociationAndRejectsInvalidInput() throws Exception {
        Model model = new ModelManager();
        Person alice = new PersonBuilder().withName("Alice").withProjects().build();
        Person bob = new PersonBuilder().withName("Bob").withProjects("CS2103T").build();
        model.addPerson(alice);
        model.addPerson(bob);
        JsonAddressBookStorage addressStorage = new JsonAddressBookStorage(temporaryFolder.resolve("contacts.json"));
        Logic logic = new LogicManager(model, new StorageManager(addressStorage,
                new JsonUserPrefsStorage(temporaryFolder.resolve("preferences.json"))));

        logic.execute("find Bob");
        logic.execute("project 1 pr/ Team   Alpha");

        Person expected = new PersonBuilder(bob).withProjects("CS2103T", "Team Alpha").build();
        assertEquals(expected, model.getFilteredPersonList().get(0));
        assertEquals(1, model.getFilteredPersonList().size());
        assertEquals(model.getAddressBook(), addressStorage.readAddressBook().orElseThrow());
        assertEquals(alice, model.getAddressBook().getPersonList().get(0));

        assertThrows(CommandException.class, () -> logic.execute("project 1 pr/team alpha"));
        assertThrows(CommandException.class, () -> logic.execute("project 2 pr/Orbital"));
        assertThrows(ParseException.class, () -> logic.execute("project 1 pr/"));
        assertEquals(expected, model.getFilteredPersonList().get(0));
        assertEquals(model.getAddressBook(), addressStorage.readAddressBook().orElseThrow());
    }
}
