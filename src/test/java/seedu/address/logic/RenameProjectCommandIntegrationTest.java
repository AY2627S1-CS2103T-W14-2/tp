package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.commons.util.JsonUtil;
import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.commands.RenameProjectCommand;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Person;
import seedu.address.model.project.Project;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.PersonBuilder;

/**
 * Verifies typed renames and confirmed merges through real JSON storage, including exact display spelling.
 */
public class RenameProjectCommandIntegrationTest {
    private static final String MERGE = "renameproject old/CS2103T new/Orbital";

    @TempDir
    public Path temporaryFolder;

    private Model model;
    private Logic logic;
    private CountingStorage storage;
    private String original;

    @BeforeEach
    public void setUp() throws Exception {
        model = new ModelManager();
        model.addPerson(PersonBuilder.named("Alice").withProjects("CS2103T", "Internship")
                .withPhone("91234567").withStoredEmail("Alice@Example.com").withoutTelegramUsername().build());
        model.addPerson(PersonBuilder.named("Bob").withProjects("cs2103t", "Internship", "orbital").build());
        model.addPerson(PersonBuilder.named("Carol").withProjects("ORBITAL", "Unrelated").build());
        model.addPerson(PersonBuilder.named("David").withProjects("CS2103T Team B").build());
        storage = new CountingStorage(temporaryFolder.resolve("contacts.json"));
        storage.saveAddressBook(model.getAddressBook());
        storage.saves = 0;
        logic = createLogic(model);
        original = snapshot(model.getAddressBook());
    }

    @Test
    public void execute_rename_updatesHiddenContactsAndPreservesDetailsThenUndoes() throws Exception {
        logic.execute("find David");
        int savesBefore = storage.saves;
        CommandResult result = logic.execute("renameproject old/CS2103T new/Team Alpha");
        assertTrue(result.getFeedbackToUser().contains("2 contact(s)"));
        assertEquals(savesBefore + 1, storage.saves);
        assertProjects(List.of(List.of("Team Alpha", "Internship"),
                List.of("Team Alpha", "Internship", "orbital"), List.of("ORBITAL", "Unrelated"),
                List.of("CS2103T Team B")));
        Person alice = model.getAddressBook().getPersonList().getFirst();
        assertEquals("91234567", alice.getPhone().value);
        assertEquals("Alice@Example.com", alice.getEmail().value);
        assertTrue(alice.getTelegramUsername().isEmpty());
        assertEquals(4, model.getFilteredPersonList().size());
        assertPersisted();
        logic.execute("undo");
        assertEquals(original, snapshot(model.getAddressBook()));
        assertPersisted();
        assertFalse(model.canUndo());
    }

    @Test
    public void execute_caseOnly_updatesMixedSpellingAndRestoresExactOriginals() throws Exception {
        logic.execute("renameproject old/orbital new/Orbital");
        assertEquals(1, storage.saves);
        assertProjects(List.of(List.of("CS2103T", "Internship"),
                List.of("cs2103t", "Internship", "Orbital"), List.of("Orbital", "Unrelated"),
                List.of("CS2103T Team B")));
        assertPersisted();
        assertThrows(CommandException.class, () -> logic.execute("renameproject old/ORBITAL new/Orbital"));
        assertEquals(1, storage.saves);
        logic.execute("undo");
        assertEquals(original, snapshot(model.getAddressBook()));
        assertPersisted();
        assertFalse(model.canUndo());
    }

    @Test
    public void execute_merge_warnsWithoutSideEffectsThenCombinesInOneUndoStep() throws Exception {
        logic.execute("find Alice");
        String diskBefore = Files.readString(storage.getAddressBookFilePath());
        int savesBefore = storage.saves;
        assertWarning(logic.execute(MERGE));
        assertEquals(original, snapshot(model.getAddressBook()));
        assertEquals(diskBefore, Files.readString(storage.getAddressBookFilePath()));
        assertEquals(savesBefore, storage.saves);
        assertEquals(1, model.getFilteredPersonList().size());
        assertFalse(model.canUndo());

        CommandResult result = logic.execute("renameproject new/ Orbital old/ cs2103t ");
        assertEquals("Merged project \"cs2103t\" into \"Orbital\" for 3 contact(s).", result.getFeedbackToUser());
        assertEquals(savesBefore + 1, storage.saves);
        assertProjects(List.of(List.of("Orbital", "Internship"), List.of("Internship", "Orbital"),
                List.of("Orbital", "Unrelated"), List.of("CS2103T Team B")));
        assertPersisted();
        logic.execute("undo");
        assertEquals(original, snapshot(model.getAddressBook()));
        assertPersisted();
        assertFalse(model.canUndo());
    }

    @Test
    public void execute_interveningCommands_cancelConfirmationIncludingFailures() throws Exception {
        for (String intervening : List.of("list", "notacommand", "renameproject old/A",
                "delete 999", "renameproject old/Missing new/Other",
                "renameproject old/CS2103T Team B new/CS2103T Team B")) {
            assertWarning(logic.execute(MERGE));
            try {
                logic.execute(intervening);
            } catch (CommandException | ParseException expected) {
                // Both successfully executed commands and rejected submissions cancel confirmation.
            }
            assertWarning(logic.execute(MERGE));
            assertEquals(original, snapshot(model.getAddressBook()));
            assertFalse(model.canUndo());
            logic = createLogic(model); // A new session must also discard pending confirmation.
        }
        assertWarning(logic.execute(MERGE));
    }

    @Test
    public void execute_changedDestinationSpellingOrSource_startsNewConfirmation() throws Exception {
        assertWarning(logic.execute(MERGE));
        assertWarning(logic.execute("renameproject old/CS2103T new/ORBITAL"));
        assertEquals(original, snapshot(model.getAddressBook()));
        assertWarning(logic.execute("renameproject old/Internship new/ORBITAL"));
        assertEquals(original, snapshot(model.getAddressBook()));
        logic.execute("renameproject old/internship new/ORBITAL");
        assertPersisted();
        assertEquals(List.of("CS2103T", "ORBITAL"), projectNames(model.getAddressBook()).getFirst());
    }

    @Test
    public void execute_saveFailure_preservesDiskFilterAndEarlierUndoAndConsumesConfirmation() throws Exception {
        logic.execute("project 4 pr/Extra");
        logic.execute("find Alice");
        String before = snapshot(model.getAddressBook());
        String diskBefore = Files.readString(storage.getAddressBookFilePath());
        int savesBefore = storage.saves;
        assertWarning(logic.execute(MERGE));
        storage.fail = true;
        assertThrows(CommandException.class, () -> logic.execute(MERGE));
        assertEquals(before, snapshot(model.getAddressBook()));
        assertEquals(diskBefore, Files.readString(storage.getAddressBookFilePath()));
        assertEquals(savesBefore, storage.saves);
        assertEquals(1, model.getFilteredPersonList().size());
        assertWarning(logic.execute(MERGE)); // Failure consumed confirmation.
        storage.fail = false;
        logic.execute("undo");
        assertEquals(original, snapshot(model.getAddressBook())); // Only the earlier project addition is undone.
        assertFalse(model.canUndo());
    }

    @Test
    public void execute_ordinaryRenameSaveFailure_preservesStateAndCanRetry() throws Exception {
        storage.fail = true;
        assertThrows(CommandException.class, () -> logic.execute("renameproject old/orbital new/Orbital"));
        assertEquals(original, snapshot(model.getAddressBook()));
        assertFalse(model.canUndo());
        assertEquals(0, storage.saves);
        storage.fail = false;
        logic.execute("renameproject old/orbital new/Orbital");
        assertPersisted();
    }

    @Test
    public void execute_quotedNamesWithDelimitersAndEscapes_roundTripsAndUndoes() throws Exception {
        String tricky = "Team new/\"Blue\"\\old/Alpha";
        String encoded = tricky.replace("\\", "\\\\").replace("\"", "\\\"");
        logic.execute("renameproject old/CS2103T new/\"" + encoded + "\"");
        assertEquals(tricky, projectNames(model.getAddressBook()).getFirst().getFirst());
        assertPersisted();
        logic.execute("renameproject old/\"" + encoded + "\" new/Final");
        assertEquals("Final", projectNames(model.getAddressBook()).getFirst().getFirst());
        logic.execute("undo");
        assertEquals(tricky, projectNames(model.getAddressBook()).getFirst().getFirst());
        logic.execute("undo");
        assertEquals(original, snapshot(model.getAddressBook()));
    }

    @Test
    public void execute_invalidOrMissingSource_doesNotSaveOrChangeHistory() throws Exception {
        assertThrows(CommandException.class, () -> logic.execute("renameproject old/Unknown new/Orbital"));
        assertThrows(ParseException.class, () -> logic.execute("renameproject old/CS2103T new/"));
        assertEquals(original, snapshot(model.getAddressBook()));
        assertEquals(0, storage.saves);
        assertFalse(model.canUndo());
    }

    @Test
    public void execute_directMerge_cannotBypassConfirmation() {
        RenameProjectCommand command = new RenameProjectCommand(new Project("CS2103T"), new Project("Orbital"));
        assertThrows(CommandException.class, () -> command.execute(model));
        assertFalse(model.canUndo());
    }

    private Logic createLogic(Model targetModel) {
        return new LogicManager(targetModel, new StorageManager(storage,
                new JsonUserPrefsStorage(temporaryFolder.resolve("preferences.json"))));
    }

    private void assertWarning(CommandResult result) {
        assertTrue(result.getFeedbackToUser().contains("Enter the same command again to confirm."));
        assertTrue(result.getFeedbackToUser().contains("all destination associations"));
    }

    private void assertProjects(List<List<String>> expected) {
        assertEquals(expected, projectNames(model.getAddressBook()));
    }

    private List<List<String>> projectNames(ReadOnlyAddressBook addressBook) {
        return addressBook.getPersonList().stream().map(person -> person.getProjects().stream()
                .map(project -> project.projectName).toList()).toList();
    }

    private String snapshot(ReadOnlyAddressBook addressBook) throws IOException {
        return JsonUtil.toJsonString(addressBook.getPersonList().stream().map(person -> List.of(
                person.getName().fullName, person.getPhone().value, person.getEmail().value,
                person.getTelegramUsername().value,
                person.getProjects().stream().map(project -> project.projectName).toList())).toList());
    }

    private void assertPersisted() throws Exception {
        ReadOnlyAddressBook reloaded = storage.readAddressBook().orElseThrow();
        assertEquals(snapshot(model.getAddressBook()), snapshot(reloaded));
        Model restarted = new ModelManager(new AddressBook(reloaded), new UserPrefs());
        assertEquals(projectNames(model.getAddressBook()), projectNames(restarted.getAddressBook()));
        assertFalse(restarted.canUndo());
    }

    /**
     * Counts completed writes and injects deterministic save failures without depending on OS permissions.
     */
    private static class CountingStorage extends JsonAddressBookStorage {
        private int saves;
        private boolean fail;

        private CountingStorage(Path path) {
            super(path);
        }

        @Override
        public void saveAddressBook(ReadOnlyAddressBook addressBook) throws IOException {
            if (fail) {
                throw new IOException("Simulated write failure");
            }
            super.saveAddressBook(addressBook);
            saves++;
        }
    }
}
