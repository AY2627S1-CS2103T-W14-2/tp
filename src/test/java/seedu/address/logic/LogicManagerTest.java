package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX;
import static seedu.address.logic.Messages.MESSAGE_UNKNOWN_COMMAND;
import static seedu.address.logic.commands.CommandTestUtil.EMAIL_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.INVALID_PROJECT_DESC;
import static seedu.address.logic.commands.CommandTestUtil.NAME_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.PHONE_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.PROJECT_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.TELEGRAM_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.VALID_PROJECT_AMY;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.AMY;
import static seedu.address.testutil.TypicalPersons.BENSON;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.commands.AddCommand;
import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.commands.ListCommand;
import seedu.address.logic.commands.UndoCommand;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.ParserUtil;
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
import seedu.address.testutil.PersonUtil;

public class LogicManagerTest {
    private static final IOException DUMMY_IO_EXCEPTION = new IOException("dummy IO exception");
    private static final IOException DUMMY_AD_EXCEPTION = new AccessDeniedException("dummy access denied exception");
    private static final Person ALICE_WITH_PROJECT = new PersonBuilder(ALICE).withProjects("CS2103T").build();

    @TempDir
    public Path temporaryFolder;

    private Model model = new ModelManager();
    private Logic logic;

    @BeforeEach
    public void setUp() {
        JsonAddressBookStorage addressBookStorage =
                new JsonAddressBookStorage(temporaryFolder.resolve("addressBook.json"));
        JsonUserPrefsStorage userPrefsStorage = new JsonUserPrefsStorage(temporaryFolder.resolve("userPrefs.json"));
        StorageManager storage = new StorageManager(addressBookStorage, userPrefsStorage);
        logic = new LogicManager(model, storage);
    }

    @Test
    public void execute_invalidCommandFormat_throwsParseException() {
        String invalidCommand = "uicfhmowqewca";
        assertParseException(invalidCommand, MESSAGE_UNKNOWN_COMMAND);
    }

    @Test
    public void execute_commandExecutionError_throwsCommandException() {
        String deleteCommand = "delete 9";
        assertCommandException(deleteCommand, MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    @Test
    public void execute_validCommand_success() throws Exception {
        String listCommand = ListCommand.COMMAND_WORD;
        assertCommandSuccess(listCommand, ListCommand.MESSAGE_NO_CONTACTS, model);
    }

    @Test
    public void execute_addWithProject_success() throws Exception {
        String addCommand = AddCommand.COMMAND_WORD + NAME_DESC_AMY + PHONE_DESC_AMY + EMAIL_DESC_AMY
                + TELEGRAM_DESC_AMY + PROJECT_DESC_AMY;
        Person expectedPerson = new PersonBuilder(AMY).withProjects(VALID_PROJECT_AMY).build();
        Model expectedModel = new ModelManager();
        expectedModel.addPerson(expectedPerson);

        assertCommandSuccess(addCommand, "Added contact: Amy Bee [" + VALID_PROJECT_AMY + "]", expectedModel);
    }

    @Test
    public void execute_addWithBlankProject_throwsParseExceptionAndLeavesModelUnchanged() {
        String addCommand = AddCommand.COMMAND_WORD + NAME_DESC_AMY + PHONE_DESC_AMY + EMAIL_DESC_AMY
                + TELEGRAM_DESC_AMY + INVALID_PROJECT_DESC;

        assertParseException(addCommand, Project.MESSAGE_CONSTRAINTS);
    }

    @Test
    public void execute_addWithoutProject_leavesModelFileAndUndoHistoryUnchanged() throws Exception {
        logic.execute(PersonUtil.getAddCommand(ALICE_WITH_PROJECT));
        Path file = temporaryFolder.resolve("addressBook.json");
        String savedData = Files.readString(file);
        AddressBook before = new AddressBook(model.getAddressBook());
        String addWithoutProject = AddCommand.COMMAND_WORD + NAME_DESC_AMY + PHONE_DESC_AMY
                + EMAIL_DESC_AMY + TELEGRAM_DESC_AMY;

        assertThrows(ParseException.class, AddCommand.MESSAGE_PROJECT_REQUIRED, () ->
                logic.execute(addWithoutProject));
        assertEquals(before, model.getAddressBook());
        assertEquals(savedData, Files.readString(file));

        logic.execute("undo");
        assertEquals(new AddressBook(), model.getAddressBook());
    }

    @Test
    public void execute_removedFields_leavesModelFileAndUndoHistoryUnchanged() throws Exception {
        logic.execute(PersonUtil.getAddCommand(new PersonBuilder(AMY).withProjects(VALID_PROJECT_AMY).build()));
        Path file = temporaryFolder.resolve("addressBook.json");
        String savedData = Files.readString(file);
        AddressBook before = new AddressBook(model.getAddressBook());
        for (String removedField : new String[] {" a/NUS", " t/friend", " a/", " t/"}) {
            assertThrows(ParseException.class, ParserUtil.MESSAGE_REMOVED_CONTACT_FIELDS, () ->
                    logic.execute(PersonUtil.getAddCommand(ALICE_WITH_PROJECT) + removedField));
            assertThrows(ParseException.class, ParserUtil.MESSAGE_REMOVED_CONTACT_FIELDS, () ->
                    logic.execute("edit 1 n/Changed Name" + removedField));
            assertEquals(before, model.getAddressBook());
            assertEquals(savedData, Files.readString(file));
        }
        logic.execute("undo");
        assertEquals(new AddressBook(), model.getAddressBook());
    }

    @Test
    public void execute_addThenUndo_restoresAndSavesPreviousState() throws Exception {
        Person person = new PersonBuilder(AMY).withProjects(VALID_PROJECT_AMY).build();
        logic.execute(PersonUtil.getAddCommand(person));
        assertTrue(model.hasPerson(person));

        CommandResult result = logic.execute("undo");

        assertEquals(UndoCommand.MESSAGE_SUCCESS, result.getFeedbackToUser());
        assertEquals(new AddressBook(), model.getAddressBook());
        JsonAddressBookStorage savedData = new JsonAddressBookStorage(temporaryFolder.resolve("addressBook.json"));
        assertEquals(new AddressBook(), savedData.readAddressBook().orElseThrow());
        assertFalse(model.canUndo());
        assertCommandException("undo", UndoCommand.MESSAGE_FAILURE);
    }

    @Test
    public void execute_editThenUndo_restoresContactDetails() throws Exception {
        Person person = new PersonBuilder(AMY).withProjects(VALID_PROJECT_AMY).build();
        model.addPerson(person);
        AddressBook before = new AddressBook(model.getAddressBook());

        logic.execute("edit 1 n/Changed Name p/+6591234567");
        assertFalse(before.equals(model.getAddressBook()));
        logic.execute("undo");

        assertEquals(before, model.getAddressBook());
    }

    @Test
    public void execute_filteredDeleteThenUndo_restoresOrderAndShowsAllPersons() throws Exception {
        model.addPerson(ALICE);
        model.addPerson(BENSON);
        AddressBook before = new AddressBook(model.getAddressBook());

        logic.execute("find Benson");
        logic.execute("delete 1");
        assertFalse(model.hasPerson(BENSON));
        logic.execute("undo");

        assertEquals(before, model.getAddressBook());
        assertEquals(before.getPersonList(), model.getFilteredPersonList());
    }

    @Test
    public void execute_clearThenUndo_restoresAllContacts() throws Exception {
        model.addPerson(ALICE);
        model.addPerson(BENSON);
        AddressBook before = new AddressBook(model.getAddressBook());

        logic.execute("clear");
        assertTrue(model.getAddressBook().getPersonList().isEmpty());
        logic.execute("undo");

        assertEquals(before, model.getAddressBook());
    }

    @Test
    public void execute_twoChangesThenUndo_restoresChangesInReverseOrder() throws Exception {
        logic.execute(PersonUtil.getAddCommand(ALICE_WITH_PROJECT));
        AddressBook afterFirstCommand = new AddressBook(model.getAddressBook());
        logic.execute(PersonUtil.getAddCommand(new PersonBuilder(BENSON).withProjects("CS2103T").build()));

        logic.execute("undo");

        assertEquals(afterFirstCommand, model.getAddressBook());
        assertTrue(model.canUndo());
        logic.execute("undo");
        assertEquals(new AddressBook(), model.getAddressBook());
        assertCommandException("undo", UndoCommand.MESSAGE_FAILURE);
    }

    @Test
    public void execute_mixedChangesThenUndo_restoresAndSavesEachPreviousState() throws Exception {
        model.addPerson(BENSON);
        AddressBook[] previousStates = new AddressBook[5];
        String[] commands = {
            PersonUtil.getAddCommand(ALICE_WITH_PROJECT),
            "edit 2 n/Changed Name",
            "project 2 pr/Undo Test",
            "delete 2",
            "clear"
        };
        for (int i = 0; i < commands.length; i++) {
            previousStates[i] = new AddressBook(model.getAddressBook());
            logic.execute(commands[i]);
        }

        JsonAddressBookStorage savedData = new JsonAddressBookStorage(temporaryFolder.resolve("addressBook.json"));
        for (int i = commands.length - 1; i >= 0; i--) {
            assertEquals(UndoCommand.MESSAGE_SUCCESS, logic.execute("undo").getFeedbackToUser());
            assertEquals(previousStates[i], model.getAddressBook());
            assertEquals(previousStates[i], savedData.readAddressBook().orElseThrow());
            assertEquals(previousStates[i].getPersonList(), model.getFilteredPersonList());
        }
        assertFalse(model.canUndo());
        assertCommandException("undo", UndoCommand.MESSAGE_FAILURE);
    }

    @Test
    public void execute_failedCommands_preservesUndoState() throws Exception {
        logic.execute(PersonUtil.getAddCommand(ALICE_WITH_PROJECT));
        AddressBook afterFirstCommand = new AddressBook(model.getAddressBook());
        logic.execute(PersonUtil.getAddCommand(new PersonBuilder(BENSON).withProjects("CS2103T").build()));
        assertThrows(CommandException.class, () -> logic.execute(PersonUtil.getAddCommand(ALICE_WITH_PROJECT)));
        assertCommandException("delete 99", MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
        assertParseException("unknownCommand", MESSAGE_UNKNOWN_COMMAND);

        logic.execute("undo");

        assertEquals(afterFirstCommand, model.getAddressBook());
        logic.execute("undo");
        assertEquals(new AddressBook(), model.getAddressBook());
        assertFalse(model.canUndo());
    }

    @Test
    public void execute_nonUndoableCommands_preservesUndoState() throws Exception {
        logic.execute(PersonUtil.getAddCommand(ALICE_WITH_PROJECT));
        AddressBook afterFirstCommand = new AddressBook(model.getAddressBook());
        logic.execute(PersonUtil.getAddCommand(new PersonBuilder(BENSON).withProjects("CS2103T").build()));
        logic.execute("find Alice");
        logic.execute("list");
        logic.execute("help");

        logic.execute("undo");

        assertEquals(afterFirstCommand, model.getAddressBook());
        logic.execute("undo");
        assertEquals(new AddressBook(), model.getAddressBook());
        assertFalse(model.canUndo());
    }

    @Test
    public void execute_newChangeAfterUndo_canUndoNewChange() throws Exception {
        logic.execute(PersonUtil.getAddCommand(ALICE_WITH_PROJECT));
        AddressBook afterFirstCommand = new AddressBook(model.getAddressBook());
        logic.execute("edit 1 n/Changed Name");
        logic.execute("undo");
        logic.execute(PersonUtil.getAddCommand(new PersonBuilder(BENSON).withProjects("CS2103T").build()));
        logic.execute("undo");

        assertEquals(afterFirstCommand, model.getAddressBook());
        logic.execute("undo");
        assertEquals(new AddressBook(), model.getAddressBook());
        assertFalse(model.canUndo());
    }

    @Test
    public void execute_undoStorageThrowsIoException_preservesStateAndCanRetry() throws Exception {
        assertFailedUndoCanRetry(DUMMY_IO_EXCEPTION, String.format(
                LogicManager.FILE_OPS_ERROR_FORMAT, DUMMY_IO_EXCEPTION.getMessage()));
    }

    @Test
    public void execute_undoStorageThrowsAdException_preservesStateAndCanRetry() throws Exception {
        assertFailedUndoCanRetry(DUMMY_AD_EXCEPTION, String.format(
                LogicManager.FILE_OPS_PERMISSION_ERROR_FORMAT, DUMMY_AD_EXCEPTION.getMessage()));
    }

    private void assertFailedUndoCanRetry(IOException exception, String expectedMessage) throws Exception {
        Path dataPath = temporaryFolder.resolve("undoFailure.json");
        boolean[] failSave = {false};
        JsonAddressBookStorage addressBookStorage = new JsonAddressBookStorage(dataPath) {
            @Override
            public void saveAddressBook(ReadOnlyAddressBook addressBook) throws IOException {
                if (failSave[0]) {
                    throw exception;
                }
                super.saveAddressBook(addressBook);
            }
        };
        logic = new LogicManager(model, new StorageManager(addressBookStorage,
                new JsonUserPrefsStorage(temporaryFolder.resolve("undoFailurePrefs.json"))));
        logic.execute(PersonUtil.getAddCommand(ALICE_WITH_PROJECT));
        AddressBook afterFirstAdd = new AddressBook(model.getAddressBook());
        Person benson = new PersonBuilder(BENSON).withProjects("CS2103T").build();
        logic.execute(PersonUtil.getAddCommand(benson));
        logic.execute("find Benson");
        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        expectedModel.updateFilteredPersonList(person -> person.equals(benson));

        failSave[0] = true;
        assertCommandFailure("undo", CommandException.class, expectedMessage, expectedModel);
        assertEquals(expectedModel.getAddressBook(), addressBookStorage.readAddressBook().orElseThrow());
        assertTrue(model.canUndo());

        failSave[0] = false;
        assertEquals(UndoCommand.MESSAGE_SUCCESS, logic.execute("undo").getFeedbackToUser());
        assertEquals(afterFirstAdd, model.getAddressBook());
        assertEquals(afterFirstAdd, addressBookStorage.readAddressBook().orElseThrow());
        assertEquals(afterFirstAdd.getPersonList(), model.getFilteredPersonList());
        assertTrue(model.canUndo());
        logic.execute("undo");
        assertEquals(new AddressBook(), model.getAddressBook());
        assertEquals(new AddressBook(), addressBookStorage.readAddressBook().orElseThrow());
        assertFalse(model.canUndo());
    }

    @Test
    public void execute_storageThrowsIoException_throwsCommandException() {
        assertCommandFailureForExceptionFromStorage(DUMMY_IO_EXCEPTION, String.format(
                LogicManager.FILE_OPS_ERROR_FORMAT, DUMMY_IO_EXCEPTION.getMessage()));
    }

    @Test
    public void execute_storageThrowsAdException_throwsCommandException() {
        assertCommandFailureForExceptionFromStorage(DUMMY_AD_EXCEPTION, String.format(
                LogicManager.FILE_OPS_PERMISSION_ERROR_FORMAT, DUMMY_AD_EXCEPTION.getMessage()));
    }

    @Test
    public void getFilteredPersonList_modifyList_throwsUnsupportedOperationException() {
        assertThrows(UnsupportedOperationException.class, () -> logic.getFilteredPersonList().remove(0));
    }

    /**
     * Executes the command and confirms that
     * - no exceptions are thrown <br>
     * - the feedback message is equal to {@code expectedMessage} <br>
     * - the internal model manager state is the same as that in {@code expectedModel} <br>
     * @see #assertCommandFailure(String, Class, String, Model)
     */
    private void assertCommandSuccess(String inputCommand, String expectedMessage,
            Model expectedModel) throws CommandException, ParseException {
        CommandResult result = logic.execute(inputCommand);
        assertEquals(expectedMessage, result.getFeedbackToUser());
        assertEquals(expectedModel, model);
    }

    /**
     * Executes the command, confirms that a ParseException is thrown and that the result message is correct.
     * @see #assertCommandFailure(String, Class, String, Model)
     */
    private void assertParseException(String inputCommand, String expectedMessage) {
        assertCommandFailure(inputCommand, ParseException.class, expectedMessage);
    }

    /**
     * Executes the command, confirms that a CommandException is thrown and that the result message is correct.
     * @see #assertCommandFailure(String, Class, String, Model)
     */
    private void assertCommandException(String inputCommand, String expectedMessage) {
        assertCommandFailure(inputCommand, CommandException.class, expectedMessage);
    }

    /**
     * Executes the command, confirms that the exception is thrown and that the result message is correct.
     * @see #assertCommandFailure(String, Class, String, Model)
     */
    private void assertCommandFailure(String inputCommand, Class<? extends Throwable> expectedException,
            String expectedMessage) {
        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        assertCommandFailure(inputCommand, expectedException, expectedMessage, expectedModel);
    }

    /**
     * Executes the command and confirms that
     * - the {@code expectedException} is thrown <br>
     * - the resulting error message is equal to {@code expectedMessage} <br>
     * - the internal model manager state is the same as that in {@code expectedModel} <br>
     * @see #assertCommandSuccess(String, String, Model)
     */
    private void assertCommandFailure(String inputCommand, Class<? extends Throwable> expectedException,
            String expectedMessage, Model expectedModel) {
        assertThrows(expectedException, expectedMessage, () -> logic.execute(inputCommand));
        assertEquals(expectedModel, model);
    }

    /**
     * Tests the Logic component's handling of an {@code IOException} thrown by the Storage component.
     *
     * @param e the exception to be thrown by the Storage component
     * @param expectedMessage the message expected inside exception thrown by the Logic component
     */
    private void assertCommandFailureForExceptionFromStorage(IOException e, String expectedMessage) {
        Path prefPath = temporaryFolder.resolve("ExceptionUserPrefs.json");

        // Inject LogicManager with a JsonAddressBookStorage that throws the IOException e when saving
        JsonAddressBookStorage addressBookStorage = new JsonAddressBookStorage(prefPath) {
            @Override
            public void saveAddressBook(ReadOnlyAddressBook addressBook) throws IOException {
                throw e;
            }
        };

        JsonUserPrefsStorage userPrefsStorage =
                new JsonUserPrefsStorage(temporaryFolder.resolve("ExceptionUserPrefs.json"));
        StorageManager storage = new StorageManager(addressBookStorage, userPrefsStorage);

        logic = new LogicManager(model, storage);

        // Triggers the saveAddressBook method by executing an add command
        String addCommand = AddCommand.COMMAND_WORD + NAME_DESC_AMY + PHONE_DESC_AMY
                + EMAIL_DESC_AMY + TELEGRAM_DESC_AMY + PROJECT_DESC_AMY;
        Person expectedPerson = new PersonBuilder(AMY).withProjects(VALID_PROJECT_AMY).build();
        ModelManager expectedModel = new ModelManager();
        expectedModel.addPerson(expectedPerson);
        assertCommandFailure(addCommand, CommandException.class, expectedMessage, expectedModel);
        assertTrue(model.canUndo());
        model.undo();
        assertEquals(new AddressBook(), model.getAddressBook());
    }
}
