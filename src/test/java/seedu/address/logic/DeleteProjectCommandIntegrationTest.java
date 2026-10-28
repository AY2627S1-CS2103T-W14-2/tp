package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.person.Person;
import seedu.address.model.project.Project;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.PersonBuilder;

public class DeleteProjectCommandIntegrationTest {
    @TempDir
    public Path temporaryFolder;

    @Test
    public void execute_renamedProjectContainingPrefix_removesPersistsAndUndoes() throws Exception {
        Model model = new ModelManager();
        JsonAddressBookStorage storage = new JsonAddressBookStorage(temporaryFolder.resolve("contacts.json"));
        Logic logic = new LogicManager(model, new StorageManager(storage,
                new JsonUserPrefsStorage(temporaryFolder.resolve("preferences.json"))));

        logic.execute("add n/Alice Tan p/+6591111111 e/alice@example.com tele/alice_tan pr/Alpha");
        logic.execute("renameproject old/Alpha new/Team pr/X");
        Person renamed = model.getAddressBook().getPersonList().getFirst();
        assertEquals(List.of(new Project("Team pr/X")), renamed.getProjects());
        logic.execute("projects");
        logic.execute("deleteproject 1 pr/Team pr/X");
        assertEquals(new PersonBuilder(renamed).withProjects().build(),
                model.getAddressBook().getPersonList().getFirst());
        assertEquals(model.getAddressBook(), storage.readAddressBook().orElseThrow());

        logic.execute("undo");
        assertEquals(renamed, model.getAddressBook().getPersonList().getFirst());
        assertEquals(model.getAddressBook(), storage.readAddressBook().orElseThrow());
    }

    @Test
    public void execute_removalPersistsAndRetainsFilter_failedCommandsPreserveUndo() throws Exception {
        Model model = new ModelManager();
        Person alice = PersonBuilder.named("Alice").withProjects("Other").build();
        Person bob = PersonBuilder.named("Bob").withProjects("Team Alpha").build();
        model.addPerson(alice);
        model.addPerson(bob);
        JsonAddressBookStorage storage = new JsonAddressBookStorage(temporaryFolder.resolve("contacts.json"));
        Logic logic = new LogicManager(model, new StorageManager(storage,
                new JsonUserPrefsStorage(temporaryFolder.resolve("preferences.json"))));

        logic.execute("findproject Alpha");
        logic.execute("deleteproject 1 pr/ team   ALPHA");
        Person expected = new PersonBuilder(bob).withProjects().build();
        assertEquals(0, model.getFilteredPersonList().size());
        assertEquals(expected, model.getAddressBook().getPersonList().get(1));
        assertEquals(alice, model.getAddressBook().getPersonList().get(0));
        assertEquals(model.getAddressBook(), storage.readAddressBook().orElseThrow());

        assertThrows(CommandException.class, () -> logic.execute("deleteproject 1 pr/Team Alpha"));
        logic.execute("find Bob");
        assertThrows(CommandException.class, () -> logic.execute("deleteproject 1 pr/Missing"));
        assertThrows(ParseException.class, () -> logic.execute("deleteproject 1 pr/"));
        assertThrows(CommandException.class, () -> logic.execute("deleteproject 1 pr/A pr/B"));
        assertEquals(expected, model.getFilteredPersonList().get(0));
        assertEquals(model.getAddressBook(), storage.readAddressBook().orElseThrow());

        logic.execute("undo");
        assertEquals(bob, model.getAddressBook().getPersonList().get(1));
        assertEquals(2, model.getFilteredPersonList().size());
        assertEquals(model.getAddressBook(), storage.readAddressBook().orElseThrow());
    }
    @Test
    public void execute_batchRemoval_persistsAndUndoRestoresAllAfterFailures() throws Exception {
        Model model = new ModelManager();
        Person alice = PersonBuilder.named("Alice").withProjects("Team Alpha", "Other").build();
        Person bob = PersonBuilder.named("Bob").withProjects("Team Alpha").build();
        model.addPerson(alice);
        model.addPerson(bob);
        JsonAddressBookStorage storage = new JsonAddressBookStorage(temporaryFolder.resolve("batch.json"));
        Logic logic = new LogicManager(model, new StorageManager(storage,
                new JsonUserPrefsStorage(temporaryFolder.resolve("batch-prefs.json"))));
        logic.execute("findproject Alpha");
        logic.execute("deleteproject 2 1 pr/team alpha");
        assertEquals(0, model.getFilteredPersonList().size());
        assertEquals(java.util.List.of(new PersonBuilder(alice).withProjects("Other").build(),
                new PersonBuilder(bob).withProjects().build()), model.getAddressBook().getPersonList());
        assertEquals(model.getAddressBook(), storage.readAddressBook().orElseThrow());
        logic.execute("list");
        assertThrows(CommandException.class, () -> logic.execute("deleteproject 1 2 pr/Other"));
        assertThrows(CommandException.class, () -> logic.execute("deleteproject 1 3 pr/Other"));
        assertThrows(ParseException.class, () -> logic.execute("deleteproject 1 1 pr/Other"));
        assertEquals(model.getAddressBook(), storage.readAddressBook().orElseThrow());
        logic.execute("undo");
        assertEquals(java.util.List.of(alice, bob), model.getAddressBook().getPersonList());
        assertEquals(model.getAddressBook(), storage.readAddressBook().orElseThrow());
    }

}
