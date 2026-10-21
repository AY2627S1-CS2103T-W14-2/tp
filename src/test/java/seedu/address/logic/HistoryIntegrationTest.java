package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.commands.HistoryCommand;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.AddressBookParser;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.PersonBuilder;

public class HistoryIntegrationTest {
    @TempDir
    public Path temporaryFolder;

    private LogicManager logic(Model model) {
        return new LogicManager(model, new StorageManager(
                new JsonAddressBookStorage(temporaryFolder.resolve("contacts.json")),
                new JsonUserPrefsStorage(temporaryFolder.resolve("prefs.json"))));
    }

    @Test
    public void execute_historyRecordsFailuresAndDuplicates_excludesCurrentSubmission() throws Exception {
        LogicManager logic = logic(new ModelManager());
        assertEquals(HistoryCommand.MESSAGE_EMPTY, logic.execute("history").getFeedbackToUser());
        logic.execute("list");
        logic.execute("list");
        assertThrows(ParseException.class, () -> logic.execute("unknown"));
        assertThrows(CommandException.class, () -> logic.execute("delete 99"));
        assertThrows(ParseException.class, () -> logic.execute("history extra"));
        assertThrows(ParseException.class, () -> logic.execute("   "));
        assertEquals(HistoryCommand.MESSAGE_HEADER + "history extra\ndelete 99\nunknown\nlist\nlist\nhistory",
                logic.execute("history").getFeedbackToUser());
        assertEquals(List.of("history", "list", "list", "unknown", "delete 99", "history extra", "history"),
                logic.getCommandHistory());
        assertThrows(UnsupportedOperationException.class, () -> logic.getCommandHistory().add("bad"));
        assertEquals(List.of(), logic(new ModelManager()).getCommandHistory());
    }

    @Test
    public void execute_historyPreservesFilterContactsStorageAndUndo() throws Exception {
        Model model = new ModelManager();
        model.addPerson(PersonBuilder.named("Alice").build());
        model.addPerson(PersonBuilder.named("Bob").build());
        LogicManager logic = logic(model);
        logic.execute("project 1 pr/Orbital");
        logic.execute("find Alice");
        String saved = Files.readString(temporaryFolder.resolve("contacts.json"));
        List<String> snapshot = logic.getCommandHistory();
        logic.execute("history");
        assertEquals(2, snapshot.size());
        assertEquals(1, model.getFilteredPersonList().size());
        assertEquals(saved, Files.readString(temporaryFolder.resolve("contacts.json")));
        logic.execute("undo");
        assertFalse(model.getAddressBook().getPersonList().get(0).getProjects()
                .contains(new seedu.address.model.project.Project("Orbital")));
    }

    @Test
    public void execute_historyDoesNotSave_andFailedSaveIsRecorded() throws Exception {
        Files.createDirectory(temporaryFolder.resolve("contacts.json"));
        LogicManager logic = logic(new ModelManager());
        assertThrows(CommandException.class, () -> logic.execute("list"));
        assertEquals(HistoryCommand.MESSAGE_HEADER + "list", logic.execute("history").getFeedbackToUser());
    }

    @Test
    public void parse_historyUsesSnapshotAndRejectsArguments() throws Exception {
        assertEquals(HistoryCommand.MESSAGE_EMPTY,
                new AddressBookParser().parseCommand(" history ").execute(new ModelManager()).getFeedbackToUser());
        assertThrows(ParseException.class, () -> new AddressBookParser().parseCommand("history 1"));
        assertThrows(NullPointerException.class, () -> new HistoryCommand(null));
    }
}
