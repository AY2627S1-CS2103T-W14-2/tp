package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeFalse;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.AccessDeniedException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.model.AddressBook;

public class AtomicFileWriterTest {
    @TempDir
    public Path testFolder;

    @Test
    public void save_partialWriteFailure_preservesOriginalAndReloadsContacts() throws Exception {
        assertFailedSavePreservesContacts(new AtomicFileWriter() {
            @Override
            void writeTemporaryFile(Path temporary, String content) throws IOException {
                Files.writeString(temporary, content.substring(0, content.length() / 2));
                throw new IOException("Partial write failed");
            }
        });
    }

    @Test
    public void save_replacementFailure_preservesOriginalAndReloadsContacts() throws Exception {
        assertFailedSavePreservesContacts(failingReplacement(new IOException("Replacement failed")));
    }

    @Test
    public void save_atomicMoveUnsupported_preservesOriginalAndReloadsContacts() throws Exception {
        assertFailedSavePreservesContacts(failingReplacement(
                new AtomicMoveNotSupportedException("temporary", "contacts", "Unsupported")));
    }

    private void assertFailedSavePreservesContacts(AtomicFileWriter writer) throws Exception {
        Path file = testFolder.resolve("contacts.json");
        new JsonAddressBookStorage(file).saveAddressBook(getTypicalAddressBook());
        byte[] original = Files.readAllBytes(file);

        assertThrows(IOException.class, () ->
                new JsonAddressBookStorage(file, writer).saveAddressBook(new AddressBook()));

        assertArrayEquals(original, Files.readAllBytes(file));
        assertEquals(getTypicalAddressBook(), new JsonAddressBookStorage(file).readAddressBook().orElseThrow());
        assertEquals(List.of(file), directoryFiles(testFolder));
    }

    @Test
    public void save_firstWriteFails_doesNotLeaveEmptyDestination() throws Exception {
        Path file = testFolder.resolve("nested/contacts.json");
        AtomicFileWriter writer = new AtomicFileWriter() {
            @Override
            void writeTemporaryFile(Path temporary, String content) throws IOException {
                Files.writeString(temporary, "{\"persons\":");
                throw new IOException("Partial write failed");
            }
        };

        assertThrows(IOException.class, () ->
                new JsonAddressBookStorage(file, writer).saveAddressBook(getTypicalAddressBook()));

        assertFalse(Files.exists(file));
        assertTrue(directoryFiles(file.getParent()).isEmpty());
        assertTrue(new JsonAddressBookStorage(file).readAddressBook().isEmpty());
    }

    @Test
    public void save_firstReplacementFails_doesNotLeaveEmptyDestination() throws Exception {
        Path file = testFolder.resolve("contacts.json");
        AtomicFileWriter writer = failingReplacement(new IOException("Replacement failed"));

        assertThrows(IOException.class, () ->
                new JsonAddressBookStorage(file, writer).saveAddressBook(getTypicalAddressBook()));

        assertFalse(Files.exists(file));
        assertTrue(directoryFiles(testFolder).isEmpty());
    }

    @Test
    public void save_cleanupFails_preservesOriginalFailureAndContacts() throws Exception {
        Path file = testFolder.resolve("contacts.json");
        new JsonAddressBookStorage(file).saveAddressBook(getTypicalAddressBook());
        byte[] original = Files.readAllBytes(file);
        IOException failure = new IOException("Partial write failed");
        AtomicFileWriter writer = new AtomicFileWriter() {
            @Override
            void writeTemporaryFile(Path temporary, String content) throws IOException {
                Files.writeString(temporary, "{");
                throw failure;
            }

            @Override
            void deleteTemporaryFile(Path temporary) throws IOException {
                throw new IOException("Cleanup failed");
            }
        };

        assertSame(failure, assertThrows(IOException.class, () ->
                new JsonAddressBookStorage(file, writer).saveAddressBook(new AddressBook())));
        assertArrayEquals(original, Files.readAllBytes(file));
        assertEquals(getTypicalAddressBook(), new JsonAddressBookStorage(file).readAddressBook().orElseThrow());
        assertEquals(2, directoryFiles(testFolder).size());
    }

    @Test
    public void save_success_createsParentsReplacesFileAndDoesNotRunCleanupAfterCommit() throws Exception {
        Path file = testFolder.resolve("nested/contacts.json");
        AtomicFileWriter writer = new AtomicFileWriter() {
            @Override
            void deleteTemporaryFile(Path temporary) {
                throw new AssertionError("Cleanup must not run after a successful replacement");
            }
        };
        JsonAddressBookStorage storage = new JsonAddressBookStorage(file, writer);
        storage.saveAddressBook(getTypicalAddressBook());
        assertEquals(getTypicalAddressBook(), new JsonAddressBookStorage(file).readAddressBook().orElseThrow());

        storage.saveAddressBook(new AddressBook());

        assertEquals(new AddressBook(), new JsonAddressBookStorage(file).readAddressBook().orElseThrow());
        assertEquals(List.of(file), directoryFiles(file.getParent()));
    }

    @Test
    @EnabledOnOs({OS.MAC, OS.LINUX})
    public void save_symbolicLink_updatesTargetAndPreservesLink() throws Exception {
        Path target = testFolder.resolve("contacts.json");
        new JsonAddressBookStorage(target).saveAddressBook(getTypicalAddressBook());
        Path link = Files.createSymbolicLink(testFolder.resolve("linked.json"), target.getFileName());

        new JsonAddressBookStorage(link).saveAddressBook(new AddressBook());

        assertTrue(Files.isSymbolicLink(link));
        assertEquals(new AddressBook(), new JsonAddressBookStorage(target).readAddressBook().orElseThrow());
        assertEquals(2, directoryFiles(testFolder).size());
    }

    @Test
    @EnabledOnOs({OS.MAC, OS.LINUX})
    public void save_danglingLink_failsWithoutReplacingLink() throws Exception {
        Path target = testFolder.resolve("missing.json");
        Path link = Files.createSymbolicLink(testFolder.resolve("linked.json"), target.getFileName());

        assertThrows(IOException.class, () -> new JsonAddressBookStorage(link).saveAddressBook(new AddressBook()));

        assertTrue(Files.isSymbolicLink(link));
        assertFalse(Files.exists(target));
    }

    @Test
    @EnabledOnOs({OS.MAC, OS.LINUX})
    public void save_existingPermissions_preservesPermissions() throws Exception {
        Path file = testFolder.resolve("contacts.json");
        new JsonAddressBookStorage(file).saveAddressBook(getTypicalAddressBook());
        var permissions = PosixFilePermissions.fromString("rw-r-----");
        Files.setPosixFilePermissions(file, permissions);

        new JsonAddressBookStorage(file).saveAddressBook(new AddressBook());

        assertEquals(permissions, Files.getPosixFilePermissions(file));
    }

    @Test
    @EnabledOnOs({OS.MAC, OS.LINUX})
    public void save_readOnlyFile_failsAndPreservesOriginal() throws Exception {
        Path file = testFolder.resolve("contacts.json");
        new JsonAddressBookStorage(file).saveAddressBook(getTypicalAddressBook());
        byte[] original = Files.readAllBytes(file);
        var permissions = Files.getPosixFilePermissions(file);
        try {
            Files.setPosixFilePermissions(file, PosixFilePermissions.fromString("r--------"));
            assumeFalse(Files.isWritable(file), "Privileged user can still write the file");
            assertThrows(AccessDeniedException.class, () ->
                    new JsonAddressBookStorage(file).saveAddressBook(new AddressBook()));
            assertArrayEquals(original, Files.readAllBytes(file));
            assertEquals(List.of(file), directoryFiles(testFolder));
        } finally {
            Files.setPosixFilePermissions(file, permissions);
        }
    }

    @Test
    @EnabledOnOs({OS.MAC, OS.LINUX})
    public void save_readOnlyDirectory_failsAndPreservesOriginal() throws Exception {
        Path directory = Files.createDirectory(testFolder.resolve("read-only"));
        Path file = directory.resolve("contacts.json");
        new JsonAddressBookStorage(file).saveAddressBook(getTypicalAddressBook());
        byte[] original = Files.readAllBytes(file);
        var permissions = Files.getPosixFilePermissions(directory);
        try {
            Files.setPosixFilePermissions(directory, PosixFilePermissions.fromString("r-x------"));
            assumeFalse(Files.isWritable(directory), "Privileged user can still write the directory");
            assertThrows(IOException.class, () ->
                    new JsonAddressBookStorage(file).saveAddressBook(new AddressBook()));
            assertArrayEquals(original, Files.readAllBytes(file));
        } finally {
            Files.setPosixFilePermissions(directory, permissions);
        }
    }

    @Test
    @EnabledOnOs(OS.WINDOWS)
    public void save_windowsFileLock_preservesCompleteDataAndCanRetryAfterRelease() throws Exception {
        Path file = testFolder.resolve("contacts.json");
        JsonAddressBookStorage storage = new JsonAddressBookStorage(file);
        storage.saveAddressBook(new AddressBook());
        byte[] original = Files.readAllBytes(file);
        try (FileChannel channel = FileChannel.open(file, StandardOpenOption.READ, StandardOpenOption.WRITE);
                FileLock lock = channel.lock(original.length + 1, 64, false)) {
            assertTrue(lock.isValid());
            try {
                storage.saveAddressBook(getTypicalAddressBook());
                assertEquals(getTypicalAddressBook(), new JsonAddressBookStorage(file).readAddressBook().orElseThrow());
            } catch (IOException e) {
                assertArrayEquals(original, Files.readAllBytes(file));
                assertEquals(new AddressBook(), new JsonAddressBookStorage(file).readAddressBook().orElseThrow());
            }
        }

        storage.saveAddressBook(getTypicalAddressBook());
        assertEquals(getTypicalAddressBook(), new JsonAddressBookStorage(file).readAddressBook().orElseThrow());
        assertEquals(List.of(file), directoryFiles(testFolder));
    }

    private AtomicFileWriter failingReplacement(IOException failure) {
        return new AtomicFileWriter() {
            @Override
            void replaceFile(Path temporary, Path target) throws IOException {
                assertTrue(Files.size(temporary) > 0);
                assertEquals(target.getParent(), temporary.getParent());
                var saved = assertDoesNotThrow(() -> new JsonAddressBookStorage(temporary).readAddressBook());
                assertTrue(saved.isPresent());
                throw failure;
            }
        };
    }

    private List<Path> directoryFiles(Path directory) throws IOException {
        try (var paths = Files.list(directory)) {
            return paths.sorted().toList();
        }
    }
}
