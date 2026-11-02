package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.TypicalPersons.ALICE;

import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.LogicManager;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Person;
import seedu.address.testutil.PersonBuilder;
import seedu.address.testutil.PersonUtil;

public class AtomicSaveIntegrationTest {
    @TempDir
    public Path testFolder;

    @Test
    public void execute_editAndUndoReplacementFailure_preservesDiskModelSearchAndHistory() throws Exception {
        Path file = testFolder.resolve("contacts.json");
        ToggleFileWriter writer = new ToggleFileWriter();
        JsonAddressBookStorage storage = new JsonAddressBookStorage(file, writer);
        Model model = new ModelManager();
        LogicManager logic = new LogicManager(model, new StorageManager(storage,
                new JsonUserPrefsStorage(testFolder.resolve("preferences.json"))));
        Person alice = new PersonBuilder(ALICE).withProjects("CS2103T").build();
        logic.execute(PersonUtil.getAddCommand(alice));
        logic.execute("find Alice");
        AddressBook before = new AddressBook(model.getAddressBook());
        byte[] original = Files.readAllBytes(file);

        writer.failReplacement = true;
        CommandException error = assertThrows(CommandException.class, () ->
                logic.execute("edit 1 n/Alice With A Much Longer Name"));

        assertEquals(LogicManager.FILE_OPS_ATOMIC_MOVE_ERROR, error.getMessage());
        assertArrayEquals(original, Files.readAllBytes(file));
        assertEquals(before, new JsonAddressBookStorage(file).readAddressBook().orElseThrow());
        assertEquals(before, model.getAddressBook());
        assertEquals(List.of(alice), model.getFilteredPersonList());
        assertEquals(List.of(alice), logic.getRecentFindPersonList());
        assertEquals(alice, logic.mostSearchedPersonProperty().get());
        assertTrue(model.canUndo());

        writer.failReplacement = false;
        logic.execute("edit 1 n/Alice With A Much Longer Name");
        AddressBook afterEdit = new AddressBook(model.getAddressBook());
        byte[] afterEditBytes = Files.readAllBytes(file);
        writer.failReplacement = true;

        assertThrows(CommandException.class, () -> logic.execute("undo"));

        assertEquals(afterEdit, model.getAddressBook());
        assertEquals(afterEdit.getPersonList(), model.getFilteredPersonList());
        assertEquals(afterEdit.getPersonList(), logic.getRecentFindPersonList());
        assertArrayEquals(afterEditBytes, Files.readAllBytes(file));
        assertEquals(afterEdit, new JsonAddressBookStorage(file).readAddressBook().orElseThrow());
        assertTrue(model.canUndo());

        writer.failReplacement = false;
        logic.execute("undo");
        assertEquals(before, model.getAddressBook());
        assertEquals(before, new JsonAddressBookStorage(file).readAddressBook().orElseThrow());
        logic.execute("undo");
        assertEquals(new AddressBook(), model.getAddressBook());
        assertEquals(new AddressBook(), new JsonAddressBookStorage(file).readAddressBook().orElseThrow());
        assertFalse(model.canUndo());
    }

    @Test
    public void execute_partialWriteFailure_rollsBackAndReloadsPreviousContacts() throws Exception {
        Path file = testFolder.resolve("contacts.json");
        AddressBook before = new AddressBook();
        before.addPerson(ALICE);
        new JsonAddressBookStorage(file).saveAddressBook(before);
        byte[] original = Files.readAllBytes(file);
        AtomicFileWriter writer = new AtomicFileWriter() {
            @Override
            void writeTemporaryFile(Path temporary, String content) throws IOException {
                Files.writeString(temporary, content.substring(0, content.length() / 2));
                throw new IOException("Partial write failed");
            }
        };
        Model model = new ModelManager(before, new UserPrefs());
        LogicManager logic = new LogicManager(model, new StorageManager(new JsonAddressBookStorage(file, writer),
                new JsonUserPrefsStorage(testFolder.resolve("preferences.json"))));
        logic.execute("find Alice");

        assertThrows(CommandException.class, () -> logic.execute("edit 1 n/Longer Contact Name"));

        assertEquals(before, model.getAddressBook());
        assertEquals(List.of(ALICE), model.getFilteredPersonList());
        assertEquals(List.of(ALICE), logic.getRecentFindPersonList());
        assertEquals(ALICE, logic.mostSearchedPersonProperty().get());
        assertFalse(model.canUndo());
        assertArrayEquals(original, Files.readAllBytes(file));
        assertEquals(before, new JsonAddressBookStorage(file).readAddressBook().orElseThrow());
    }

    private static class ToggleFileWriter extends AtomicFileWriter {
        private boolean failReplacement;

        @Override
        void replaceFile(Path temporary, Path target) throws IOException {
            if (failReplacement) {
                throw new AtomicMoveNotSupportedException(temporary.toString(), target.toString(), "Test failure");
            }
            super.replaceFile(temporary, target);
        }
    }
}
