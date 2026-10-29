package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;

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
 * Verifies typed renames and immediate merges through real JSON storage, including exact display spelling.
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
        assertEquals(List.of("David"), filteredNames());
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
    public void execute_merge_combinesImmediatelyAndUndoesInOneStep() throws Exception {
        logic.execute("find Alice");
        int savesBefore = storage.saves;
        CommandResult result = logic.execute(MERGE);
        assertEquals("Merged project \"CS2103T\" into \"Orbital\" for 3 contact(s). "
                + "Use undo to reverse this change.", result.getFeedbackToUser());
        assertEquals(savesBefore + 1, storage.saves);
        assertProjects(List.of(List.of("Orbital", "Internship"), List.of("Internship", "Orbital"),
                List.of("Orbital", "Unrelated"), List.of("CS2103T Team B")));
        assertEquals(List.of("Alice"), filteredNames());
        assertPersisted();
        assertThrows(CommandException.class, () -> logic.execute(MERGE));
        assertEquals(savesBefore + 1, storage.saves);
        logic.execute("undo");
        assertEquals(original, snapshot(model.getAddressBook()));
        assertPersisted();
        assertFalse(model.canUndo());
    }

    @Test
    public void execute_rename_preservesProjectFilterAndReevaluatesMembership() throws Exception {
        logic.execute("findproject CS2103T");
        logic.execute("renameproject old/CS2103T new/Team Alpha");
        assertEquals(List.of("David"), filteredNames());
        assertPersisted();
        logic.execute("undo");
        assertEquals(original, snapshot(model.getAddressBook()));
        assertFalse(model.canUndo());
    }

    @Test
    public void execute_merge_preservesDestinationFilterAndIncludesNewMembers() throws Exception {
        logic.execute("findproject orbital");
        assertEquals(List.of("Bob", "Carol"), filteredNames());
        logic.execute(MERGE);
        assertEquals(List.of("Alice", "Bob", "Carol"), filteredNames());
        assertPersisted();
    }

    @Test
    public void execute_rename_keepsEmptySearchAndStillUpdatesHiddenContacts() throws Exception {
        logic.execute("find Nobody");
        logic.execute("renameproject old/CS2103T new/Team Alpha");
        assertTrue(model.getFilteredPersonList().isEmpty());
        assertEquals("Team Alpha", projectNames(model.getAddressBook()).getFirst().getFirst());
        assertPersisted();
    }

    @Test
    public void execute_saveFailure_restoresCommandAndKeepsUndoHistoryAndFilter() throws Exception {
        logic.execute("project 4 pr/Extra");
        logic.execute("find Alice");
        String before = snapshot(model.getAddressBook());
        String diskBefore = Files.readString(storage.getAddressBookFilePath());
        int savesBefore = storage.saves;
        storage.fail = true;
        CommandException failure = assertThrows(CommandException.class, () -> logic.execute(MERGE));
        assertTrue(failure.getMessage().contains("Simulated write failure"));
        assertEquals(before, snapshot(model.getAddressBook()));
        assertEquals(diskBefore, Files.readString(storage.getAddressBookFilePath()));
        assertEquals(savesBefore, storage.saves);
        assertEquals(List.of("Alice"), filteredNames());
        assertTrue(model.canUndo());
        storage.fail = false;
        logic.execute("undo");
        assertEquals(original, snapshot(model.getAddressBook()));
        assertPersisted();
        assertFalse(model.canUndo());
    }

    @Test
    public void execute_quotedSource_matchesOnlyNameContainingLiteralQuotes() throws Exception {
        assertThrows(CommandException.class, () -> logic.execute("renameproject old/\"CS2103T\" new/Final"));
        assertEquals(original, snapshot(model.getAddressBook()));
        assertEquals(0, storage.saves);
        assertFalse(model.canUndo());
        logic.execute("project 4 pr/\"CS2103T\"");
        logic.execute("renameproject old/\"CS2103T\" new/Final");
        assertProjects(List.of(List.of("CS2103T", "Internship"),
                List.of("cs2103t", "Internship", "orbital"), List.of("ORBITAL", "Unrelated"),
                List.of("CS2103T Team B", "Final")));
        assertPersisted();
    }

    @Test
    public void execute_literalQuotesAndBackslashes_roundTripsAndUndoes() throws Exception {
        String literalName = "\"Team\\Blue\"";
        logic.execute("renameproject old/CS2103T new/" + literalName);
        assertEquals(literalName, projectNames(model.getAddressBook()).getFirst().getFirst());
        assertPersisted();
        logic.execute("renameproject old/" + literalName + " new/Final");
        assertEquals("Final", projectNames(model.getAddressBook()).getFirst().getFirst());
        logic.execute("undo");
        assertEquals(literalName, projectNames(model.getAddressBook()).getFirst().getFirst());
        logic.execute("undo");
        assertEquals(original, snapshot(model.getAddressBook()));
    }

    @Test
    public void execute_nonLatinDigitLocale_usesAsciiCountsForRenameAndMerge() throws Exception {
        Locale originalFormatLocale = Locale.getDefault(Locale.Category.FORMAT);
        try {
            Locale.setDefault(Locale.Category.FORMAT, Locale.forLanguageTag("ar-EG"));
            assertEquals("Renamed project \"orbital\" to \"Orbital\" for 2 contact(s). "
                    + "Use undo to reverse this change.",
                    logic.execute("renameproject old/orbital new/Orbital").getFeedbackToUser());
            logic.execute("undo");
            assertEquals("Merged project \"CS2103T\" into \"Orbital\" for 3 contact(s). "
                    + "Use undo to reverse this change.", logic.execute(MERGE).getFeedbackToUser());
        } finally {
            Locale.setDefault(Locale.Category.FORMAT, originalFormatLocale);
        }
    }

    @Test
    public void execute_invalidOrMissingSource_doesNotSaveOrChangeHistory() throws Exception {
        assertThrows(CommandException.class, () -> logic.execute("renameproject old/Unknown new/Orbital"));
        assertThrows(ParseException.class, () -> logic.execute("renameproject old/CS2103T new/"));
        assertThrows(ParseException.class, () -> logic.execute("renameproject old/ new/Space"));
        assertThrows(ParseException.class, () -> logic.execute("renameproject 1 old/Orbital new/Space"));
        assertEquals(original, snapshot(model.getAddressBook()));
        assertEquals(0, storage.saves);
        assertFalse(model.canUndo());
    }

    @Test
    public void execute_directMerge_updatesModelImmediately() throws Exception {
        RenameProjectCommand command = new RenameProjectCommand(new Project("CS2103T"), new Project("Orbital"));
        assertTrue(command.execute(model).getFeedbackToUser().startsWith("Merged project"));
        assertProjects(List.of(List.of("Orbital", "Internship"), List.of("Internship", "Orbital"),
                List.of("Orbital", "Unrelated"), List.of("CS2103T Team B")));
    }

    private Logic createLogic(Model targetModel) {
        return new LogicManager(targetModel, new StorageManager(storage,
                new JsonUserPrefsStorage(temporaryFolder.resolve("preferences.json"))));
    }

    private List<String> filteredNames() {
        return model.getFilteredPersonList().stream().map(person -> person.getName().fullName).toList();
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
