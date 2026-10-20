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

public class ReplaceProjectCommandIntegrationTest {
    @TempDir
    public Path temporaryFolder;

    @Test
    public void execute_replacementPersistsAndRetainsFilter_failedCommandsPreserveUndo() throws Exception {
        Model model = new ModelManager();
        Person alice = PersonBuilder.named("Alice").withProjects("Other").build();
        Person bob = PersonBuilder.named("Bob").withProjects("Team Alpha").build();
        model.addPerson(alice);
        model.addPerson(bob);
        JsonAddressBookStorage storage = new JsonAddressBookStorage(temporaryFolder.resolve("contacts.json"));
        Logic logic = new LogicManager(model, new StorageManager(storage,
                new JsonUserPrefsStorage(temporaryFolder.resolve("preferences.json"))));
        logic.execute("findproject Alpha");
        logic.execute("replaceproject 1 old/team alpha new/Orbital");
        assertEquals(0, model.getFilteredPersonList().size());
        assertEquals(new PersonBuilder(bob).withProjects("Orbital").build(),
                model.getAddressBook().getPersonList().get(1));
        assertEquals(alice, model.getAddressBook().getPersonList().get(0));
        assertEquals(model.getAddressBook(), storage.readAddressBook().orElseThrow());
        logic.execute("find Bob");
        assertThrows(CommandException.class, () -> logic.execute("replaceproject 1 old/Orbital new/ORBITAL"));
        assertThrows(CommandException.class, () -> logic.execute("replaceproject 1 old/Missing new/New"));
        assertThrows(ParseException.class, () -> logic.execute("replaceproject 1 old/Orbital new/"));
        assertEquals(model.getAddressBook(), storage.readAddressBook().orElseThrow());
        logic.execute("undo");
        assertEquals(bob, model.getAddressBook().getPersonList().get(1));
        assertEquals(2, model.getFilteredPersonList().size());
        assertEquals(model.getAddressBook(), storage.readAddressBook().orElseThrow());
    }
}
