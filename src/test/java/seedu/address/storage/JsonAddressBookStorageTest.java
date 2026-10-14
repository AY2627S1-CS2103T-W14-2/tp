package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.HOON;
import static seedu.address.testutil.TypicalPersons.IDA;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.commons.exceptions.DataLoadingException;
import seedu.address.model.AddressBook;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.person.TelegramUsername;

public class JsonAddressBookStorageTest {
    private static final Path TEST_DATA_FOLDER = Paths.get("src", "test", "data", "JsonAddressBookStorageTest");

    @TempDir
    public Path testFolder;

    @Test
    public void save_legacyData_backsUpOriginalOnceAndPreservesContacts() throws Exception {
        Path file = testFolder.resolve("contacts.json");
        String legacy = """
                {"persons":[
                  {"name":"Alex Tan","phone":"91234567","email":"alex@example.com",
                   "telegramUsername":"alex_tan","projects":["Team *()!%$#&","Orbital"],
                   "address":"NUS","tags":["friend"]},
                  {"name":"Wen Jie","phone":"87654321","email":"wen@example.com",
                   "address":null,"tags":{"retired":"ignored"}},
                  {"name":"Jordan Lee","phone":"81234567","email":"jordan@example.com"}
                ]}
                """;
        Files.writeString(file, legacy);
        byte[] original = Files.readAllBytes(file);
        JsonAddressBookStorage storage = new JsonAddressBookStorage(file);
        ReadOnlyAddressBook contacts = storage.readAddressBook().orElseThrow();
        assertEquals(3, contacts.getPersonList().size());
        assertEquals("91234567", contacts.getPersonList().getFirst().getPhone().value);
        assertEquals(TelegramUsername.EMPTY, contacts.getPersonList().get(1).getTelegramUsername());
        assertTrue(contacts.getPersonList().get(1).getProjects().isEmpty());
        assertEquals(List.of(), backupFiles()); // Loading alone does not migrate the file.

        storage.saveAddressBook(contacts);
        assertEquals(1, backupFiles().size());
        Path backup = backupFiles().getFirst();
        assertArrayEquals(original, Files.readAllBytes(backup));
        assertFalse(Files.readString(file).contains("\"address\""));
        assertFalse(Files.readString(file).contains("\"tags\""));
        assertTrue(Files.readString(file).contains("\"phone\" : \"91234567\""));
        assertEquals(contacts, storage.readAddressBook().orElseThrow());

        new JsonAddressBookStorage(file).saveAddressBook(contacts);
        assertEquals(List.of(backup), backupFiles());
        assertArrayEquals(original, Files.readAllBytes(backup));

        // Importing another legacy file creates a new backup without replacing the first.
        Files.writeString(file, legacy.replace("NUS", "New address"));
        storage.saveAddressBook(storage.readAddressBook().orElseThrow());
        assertEquals(2, backupFiles().size());
        assertArrayEquals(original, Files.readAllBytes(backup));
    }

    @Test
    public void readAndSave_legacyNameAndEmail_preservesOriginalValues() throws Exception {
        Path file = testFolder.resolve("legacy-contact.json");
        Files.writeString(file, """
                {"persons":[{"name":"12345 ","phone":"91234567","email":"Alex@Example"}]}
                """);
        JsonAddressBookStorage storage = new JsonAddressBookStorage(file);
        ReadOnlyAddressBook contacts = storage.readAddressBook().orElseThrow();

        assertEquals("12345 ", contacts.getPersonList().getFirst().getName().fullName);
        assertEquals("Alex@Example", contacts.getPersonList().getFirst().getEmail().value);

        storage.saveAddressBook(contacts);
        String saved = Files.readString(file);
        assertTrue(saved.contains("\"name\" : \"12345 \""));
        assertTrue(saved.contains("\"email\" : \"Alex@Example\""));
        assertEquals(contacts, storage.readAddressBook().orElseThrow());
    }

    @Test
    public void save_backupFailure_leavesOriginalFileUntouched() throws Exception {
        Path file = testFolder.resolve("contacts.json");
        Files.writeString(file, "{\"persons\":[{\"address\":\"NUS\"}]}");
        byte[] original = Files.readAllBytes(file);
        JsonAddressBookStorage storage = new JsonAddressBookStorage(file) {
            @Override
            void backupLegacyData(Path filePath) throws IOException {
                throw new IOException("Backup failed");
            }
        };
        assertThrows(IOException.class, "Backup failed", () -> storage.saveAddressBook(getTypicalAddressBook()));
        assertArrayEquals(original, Files.readAllBytes(file));
    }

    @Test
    public void save_newData_doesNotCreateLegacyBackup() throws Exception {
        JsonAddressBookStorage storage = new JsonAddressBookStorage(testFolder.resolve("new.json"));
        storage.saveAddressBook(getTypicalAddressBook());
        storage.saveAddressBook(getTypicalAddressBook());
        assertTrue(backupFiles().isEmpty());
    }

    @Test
    public void save_malformedData_preservesOriginalBeforeReplacement() throws Exception {
        Path file = testFolder.resolve("broken.json");
        Files.writeString(file, "{broken JSON");
        byte[] original = Files.readAllBytes(file);
        JsonAddressBookStorage storage = new JsonAddressBookStorage(file);
        storage.saveAddressBook(getTypicalAddressBook());
        assertArrayEquals(original, Files.readAllBytes(backupFiles().getFirst()));
        assertEquals(getTypicalAddressBook(), storage.readAddressBook().orElseThrow());
    }

    private List<Path> backupFiles() throws IOException {
        try (var files = Files.list(testFolder)) {
            return files.filter(path -> path.getFileName().toString().endsWith(".bak")).sorted().toList();
        }
    }

    @Test
    public void readAddressBook_nullFilePath_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> readAddressBook(null));
    }

    private java.util.Optional<ReadOnlyAddressBook> readAddressBook(String filePath) throws Exception {
        return new JsonAddressBookStorage(Paths.get(filePath)).readAddressBook(addToTestDataPathIfNotNull(filePath));
    }

    private Path addToTestDataPathIfNotNull(String prefsFileInTestDataFolder) {
        return prefsFileInTestDataFolder != null
                ? TEST_DATA_FOLDER.resolve(prefsFileInTestDataFolder)
                : null;
    }

    @Test
    public void read_missingFile_emptyResult() throws Exception {
        assertFalse(readAddressBook("NonExistentFile.json").isPresent());
    }

    @Test
    public void read_notJsonFormat_exceptionThrown() {
        assertThrows(DataLoadingException.class, () -> readAddressBook("notJsonFormatAddressBook.json"));
    }

    @Test
    public void readAddressBook_invalidPersonAddressBook_throwDataLoadingException() {
        assertThrows(DataLoadingException.class, () -> readAddressBook("invalidPersonAddressBook.json"));
    }

    @Test
    public void readAddressBook_invalidAndValidPersonAddressBook_throwDataLoadingException() {
        assertThrows(DataLoadingException.class, () -> readAddressBook("invalidAndValidPersonAddressBook.json"));
    }

    @Test
    public void readAndSaveAddressBook_allInOrder_success() throws Exception {
        Path filePath = testFolder.resolve("TempAddressBook.json");
        AddressBook original = getTypicalAddressBook();
        JsonAddressBookStorage jsonAddressBookStorage = new JsonAddressBookStorage(filePath);

        // Save in new file and read back
        jsonAddressBookStorage.saveAddressBook(original, filePath);
        ReadOnlyAddressBook readBack = jsonAddressBookStorage.readAddressBook(filePath).get();
        assertEquals(original, new AddressBook(readBack));

        // Modify data, overwrite existing file, and read back
        original.addPerson(HOON);
        original.removePerson(ALICE);
        jsonAddressBookStorage.saveAddressBook(original, filePath);
        readBack = jsonAddressBookStorage.readAddressBook(filePath).get();
        assertEquals(original, new AddressBook(readBack));

        // Save and read without specifying file path
        original.addPerson(IDA);
        jsonAddressBookStorage.saveAddressBook(original); // file path not specified
        readBack = jsonAddressBookStorage.readAddressBook().get(); // file path not specified
        assertEquals(original, new AddressBook(readBack));

    }

    @Test
    public void saveAddressBook_nullAddressBook_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> saveAddressBook(null, "SomeFile.json"));
    }

    /**
     * Saves {@code addressBook} at the specified {@code filePath}.
     */
    private void saveAddressBook(ReadOnlyAddressBook addressBook, String filePath) {
        try {
            new JsonAddressBookStorage(Paths.get(filePath))
                    .saveAddressBook(addressBook, addToTestDataPathIfNotNull(filePath));
        } catch (IOException ioe) {
            throw new AssertionError("There should not be an error writing to the file.", ioe);
        }
    }

    @Test
    public void saveAddressBook_nullFilePath_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> saveAddressBook(new AddressBook(), null));
    }
}
