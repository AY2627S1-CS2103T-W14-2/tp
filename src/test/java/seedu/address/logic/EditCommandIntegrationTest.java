package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.commands.EditCommand;
import seedu.address.logic.commands.UndoCommand;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Person;
import seedu.address.model.person.TelegramUsername;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.PersonBuilder;

/**
 * Exercises typed {@code edit} commands, including {@code tele/} and the rejected {@code pr/}, through the real
 * parser, {@code LogicManager}, model and JSON storage in an isolated directory.
 */
public class EditCommandIntegrationTest {
    private static final String ADD_ALEX = "add n/Alex Tan p/+65 9123-4567 e/alex@example.com "
            + "tele/@Alex_Tan pr/Team Alpha";
    private static final String ADD_BETTY = "add n/Betty Lim p/+65 8765-4321 e/betty@example.com "
            + "tele/betty_lim pr/Orbital";
    private static final Person ALEX = new PersonBuilder().withName("Alex Tan").withPhone("+6591234567")
            .withEmail("alex@example.com").withTelegramUsername("alex_tan")
            .withProjects("Team Alpha", "CS2103T").build();
    private static final Person BETTY = new PersonBuilder().withName("Betty Lim").withPhone("+6587654321")
            .withEmail("betty@example.com").withTelegramUsername("betty_lim").withProjects("Orbital").build();
    /** A contact saved by an older version: local phone, mixed-case email, no Telegram username. */
    private static final Person LEGACY = new PersonBuilder().withName("Legacy Lee").withPhone("91234567")
            .withStoredEmail("Legacy.Lee@Example.COM").withoutTelegramUsername()
            .withProjects("Orbital", "CS2103T").build();

    @TempDir
    public Path temporaryFolder;

    private Model model;
    private Logic logic;
    private JsonAddressBookStorage storage;
    private int saveCount;

    /**
     * Starts each test with Alex (two ordered projects) and Betty saved through real commands, so the undo history
     * holds two adds and one project association. Saves are counted so tests can check that a command saved once
     * or not at all, since rewriting identical contents would leave the file unchanged.
     */
    @BeforeEach
    public void setUp() throws Exception {
        storage = new JsonAddressBookStorage(temporaryFolder.resolve("contacts.json")) {
            @Override
            public void saveAddressBook(ReadOnlyAddressBook addressBook) throws IOException {
                saveCount++;
                super.saveAddressBook(addressBook);
            }
        };
        model = new ModelManager();
        logic = createLogic(model);
        logic.execute(ADD_ALEX);
        logic.execute(ADD_BETTY);
        logic.execute("project 1 pr/CS2103T");
        assertSavedContacts(ALEX, BETTY);
    }

    @Test
    public void execute_multiFieldEdit_savesOnceAndUndoesInOneStep() throws Exception {
        AddressBook beforeEdit = new AddressBook(model.getAddressBook());
        Person edited = new PersonBuilder(ALEX).withName("Alex Lee").withPhone("+6590001111")
                .withEmail("alex.lee@example.com").build();
        int savesBefore = saveCount;

        String feedback = logic.execute("edit 1 n/Alex Lee p/+65 9000-1111 e/Alex.Lee@Example.COM")
                .getFeedbackToUser();

        assertEquals(savesBefore + 1, saveCount);
        assertEquals("Edited contact: Alex Lee; Phone: +6590001111; Email: alex.lee@example.com; "
                + "Telegram: @alex_tan; Projects: Team Alpha, CS2103T", feedback);
        assertSavedContacts(edited, BETTY);
        assertEquals("alex.lee@example.com", savedPerson(0).getEmail().value);

        // One undo restores every edited field; the next undo reverses the earlier project association.
        assertEquals(UndoCommand.MESSAGE_SUCCESS, logic.execute("undo").getFeedbackToUser());
        assertEquals(beforeEdit, model.getAddressBook());
        assertSavedContacts(ALEX, BETTY);
        logic.execute("undo");
        assertSavedContacts(new PersonBuilder(ALEX).withProjects("Team Alpha").build(), BETTY);
    }

    @Test
    public void execute_nameAndTelegramEdit_oneUndoRestoresBoth() throws Exception {
        AddressBook beforeEdit = new AddressBook(model.getAddressBook());
        Person edited = new PersonBuilder(ALEX).withName("Alex Lee").withTelegramUsername("alex_lee").build();
        int savesBefore = saveCount;

        String feedback = logic.execute("edit 1 n/Alex Lee tele/@Alex_Lee").getFeedbackToUser();

        assertEquals(savesBefore + 1, saveCount);
        assertEquals("Edited contact: Alex Lee; Phone: +6591234567; Email: alex@example.com; "
                + "Telegram: @alex_lee; Projects: Team Alpha, CS2103T", feedback);
        assertSavedContacts(edited, BETTY);

        logic.execute("undo");
        assertEquals(beforeEdit, model.getAddressBook());
        assertSavedContacts(ALEX, BETTY);
    }

    @Test
    public void execute_telegramSameAfterNormalisation_keepsStateAndUndoesPreviousChange() throws Exception {
        logic.execute("edit 1 tele/@New_Handle");
        Person handled = new PersonBuilder(ALEX).withTelegramUsername("new_handle").build();
        logic.execute("find Alex");
        String savedData = readSavedFile();
        AddressBook before = new AddressBook(model.getAddressBook());
        int savesBefore = saveCount;

        CommandException failure = assertThrows(CommandException.class, () ->
                logic.execute("edit 1 tele/@NEW_HANDLE"));
        assertEquals(EditCommand.MESSAGE_NO_CHANGES, failure.getMessage());
        assertRejectedState(before, savedData, List.of(handled), savesBefore);

        logic.execute("undo");
        assertSavedContacts(ALEX, BETTY);
    }

    @Test
    public void execute_editAfterSearch_showsAllContactsAndReloads() throws Exception {
        logic.execute("find Betty");
        logic.execute("edit 1 n/Betty Tan");
        Person edited = new PersonBuilder(BETTY).withName("Betty Tan").build();

        assertEquals(List.of(ALEX, edited), logic.getFilteredPersonList());
        assertSavedContacts(ALEX, edited);
        reloadSession();
        assertSavedContacts(ALEX, edited);
    }

    @Test
    public void execute_repeatedEditIsNoOp_keepsFileFilterAndUndoesPreviousChange() throws Exception {
        logic.execute("edit 1 n/Alicia Tan");
        Person alicia = new PersonBuilder(ALEX).withName("Alicia Tan").build();
        logic.execute("find Alicia");
        String savedData = readSavedFile();
        AddressBook before = new AddressBook(model.getAddressBook());
        int savesBefore = saveCount;

        for (String command : List.of("edit 1 n/Alicia Tan", "edit 1 n/Alicia Tan p/+6591234567",
                "edit 1 e/ALEX@example.com", "edit 1 n/Alicia   Tan")) {
            CommandException failure = assertThrows(CommandException.class, () -> logic.execute(command), command);
            assertEquals(EditCommand.MESSAGE_NO_CHANGES, failure.getMessage());
            assertRejectedState(before, savedData, List.of(alicia), savesBefore);
        }

        logic.execute("undo");
        assertSavedContacts(ALEX, BETTY);
    }

    @Test
    public void execute_rejectedEdits_keepFileFilterAndUndoHistory() throws Exception {
        logic.execute("find Alex");
        String savedData = readSavedFile();
        AddressBook before = new AddressBook(model.getAddressBook());
        int savesBefore = saveCount;

        // Betty is hidden by the filter, but her details still conflict.
        for (String command : List.of("edit 1 p/+6587654321", "edit 1 n/Alicia e/BETTY@example.com",
                "edit 2 n/Alicia")) {
            assertThrows(CommandException.class, () -> logic.execute(command), command);
            assertRejectedState(before, savedData, List.of(ALEX), savesBefore);
        }
        CommandException telegramConflict = assertThrows(CommandException.class, () ->
                logic.execute("edit 1 tele/@BETTY_LIM"));
        assertEquals("Telegram username @betty_lim is already used by Betty Lim. "
                + "Please enter a different Telegram username.", telegramConflict.getMessage());
        assertRejectedState(before, savedData, List.of(ALEX), savesBefore);

        for (String command : List.of("edit 1", "edit n/Alicia", "edit 1 n/Alicia n/Alicia",
                "edit 1 n/Alicia p/91234567", "edit 1 n/Alicia e/", "edit 1 n/Alicia a/Old Address")) {
            assertThrows(ParseException.class, () -> logic.execute(command), command);
            assertRejectedState(before, savedData, List.of(ALEX), savesBefore);
        }
        // A blank Telegram username rejects the whole command, so the valid name is not applied either.
        Map<String, String> messageByCommand = Map.of(
                "edit 1 n/Weien tele/", TelegramUsername.MESSAGE_CONSTRAINTS,
                "edit 1 tele/@alex_lee tele/@alex_lee",
                "Multiple values specified for the following single-valued field(s): tele/",
                "edit 1 n/Alicia pr/Orbital", EditCommand.MESSAGE_PROJECT_NOT_EDITABLE);
        for (Map.Entry<String, String> entry : messageByCommand.entrySet()) {
            ParseException failure = assertThrows(ParseException.class, () -> logic.execute(entry.getKey()),
                    entry.getKey());
            assertEquals(entry.getValue(), failure.getMessage(), entry.getKey());
            assertRejectedState(before, savedData, List.of(ALEX), savesBefore);
        }

        // The failures neither consumed nor added undo history: undo reverses the project association.
        logic.execute("undo");
        assertSavedContacts(new PersonBuilder(ALEX).withProjects("Team Alpha").build(), BETTY);
    }

    @Test
    public void execute_legacyContact_keepsUntouchedValuesAndLowercasesEditedEmail() throws Exception {
        startSessionWith(LEGACY);

        logic.execute("edit 1 n/Legacy Tan");
        Person saved = savedPerson(0);
        assertEquals("Legacy Tan", saved.getName().fullName);
        assertEquals("91234567", saved.getPhone().value);
        assertEquals("Legacy.Lee@Example.COM", saved.getEmail().value);
        assertTrue(saved.getTelegramUsername().isEmpty());
        assertEquals(LEGACY.getProjects(), saved.getProjects());

        // Same address in a different case: a real change of the saved spelling.
        logic.execute("edit 1 e/legacy.lee@example.com");
        assertEquals("legacy.lee@example.com", savedPerson(0).getEmail().value);
        logic.execute("undo");
        assertEquals("Legacy.Lee@Example.COM", savedPerson(0).getEmail().value);
    }

    @Test
    public void execute_noOpWithoutHistory_undoReportsNoCommand() throws Exception {
        startSessionWith(LEGACY);
        String savedData = readSavedFile();
        int savesBefore = saveCount;

        CommandException noChange = assertThrows(CommandException.class, () ->
                logic.execute("edit 1 n/Legacy Lee"));
        assertEquals(EditCommand.MESSAGE_NO_CHANGES, noChange.getMessage());
        assertEquals(savedData, readSavedFile());
        assertEquals(savesBefore, saveCount);

        CommandException noHistory = assertThrows(CommandException.class, () -> logic.execute("undo"));
        assertEquals(UndoCommand.MESSAGE_FAILURE, noHistory.getMessage());
        assertFalse(model.canUndo());
    }

    @Test
    public void execute_typedTelegramEdits_fillLegacyReplaceAndReload() throws Exception {
        startSessionWith(LEGACY, ALEX);
        int savesBefore = saveCount;

        logic.execute("edit 1 tele/@Legacy_Lee");
        assertEquals(savesBefore + 1, saveCount);
        logic.execute("edit 2 tele/alex_tan_sg");
        assertEquals(savesBefore + 2, saveCount);
        reloadSession();

        Person legacy = savedPerson(0);
        assertEquals("legacy_lee", legacy.getTelegramUsername().value);
        assertEquals("91234567", legacy.getPhone().value);
        assertEquals("Legacy.Lee@Example.COM", legacy.getEmail().value);
        assertEquals(LEGACY.getProjects(), legacy.getProjects());
        assertEquals(new PersonBuilder(ALEX).withTelegramUsername("alex_tan_sg").build(), savedPerson(1));
    }

    private void assertRejectedState(AddressBook before, String savedData, List<Person> displayed,
            int expectedSaveCount) throws Exception {
        assertEquals(before, model.getAddressBook());
        assertEquals(displayed, logic.getFilteredPersonList());
        assertEquals(savedData, readSavedFile());
        assertEquals(expectedSaveCount, saveCount);
    }

    private void assertSavedContacts(Person... expected) throws Exception {
        assertEquals(List.of(expected), model.getAddressBook().getPersonList());
        assertEquals(List.of(expected), storage.readAddressBook().orElseThrow().getPersonList());
    }

    private Person savedPerson(int zeroBasedIndex) throws Exception {
        return storage.readAddressBook().orElseThrow().getPersonList().get(zeroBasedIndex);
    }

    private String readSavedFile() throws Exception {
        return Files.readString(storage.getAddressBookFilePath());
    }

    /**
     * Replaces the saved data with {@code persons} and starts a new session on it, with no undo history.
     */
    private void startSessionWith(Person... persons) throws Exception {
        AddressBook addressBook = new AddressBook();
        for (Person person : persons) {
            addressBook.addPerson(person);
        }
        storage.saveAddressBook(addressBook);
        reloadSession();
    }

    private void reloadSession() throws Exception {
        model = new ModelManager(storage.readAddressBook().orElseThrow(), new UserPrefs());
        logic = createLogic(model);
    }

    private Logic createLogic(Model sessionModel) {
        return new LogicManager(sessionModel, new StorageManager(storage,
                new JsonUserPrefsStorage(temporaryFolder.resolve("preferences.json"))));
    }
}
