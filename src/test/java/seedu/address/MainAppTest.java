package seedu.address;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.testutil.Assert.assertThrows;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.UserPrefs;
import seedu.address.model.util.SampleDataUtil;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.Storage;
import seedu.address.storage.StorageManager;

public class MainAppTest {

    private static final byte[] UNREADABLE_DATA = "{ \"persons\": [ { \"name\": \"No Phone\" } ] }".getBytes();

    @TempDir
    public Path temporaryFolder;

    private Path dataFile;

    @BeforeEach
    public void setUp() {
        dataFile = temporaryFolder.resolve("addressbook.json");
    }

    @Test
    public void initModelManager_unreadableFileAndBackupFails_throwsIoExceptionAndKeepsFile() throws Exception {
        Files.write(dataFile, UNREADABLE_DATA);
        JsonAddressBookStorage storageWithFailingBackup = new JsonAddressBookStorage(dataFile) {
            @Override
            public Optional<Path> backupAddressBook() throws IOException {
                throw new IOException("Disk full");
            }
        };

        assertThrows(IOException.class, "Disk full", () ->
                new MainApp().initModelManager(storageWith(storageWithFailingBackup), new UserPrefs()));
        assertArrayEquals(UNREADABLE_DATA, Files.readAllBytes(dataFile));
    }

    @Test
    public void initModelManager_unreadableFileAndBackupSucceeds_startsEmptyWithBackup() throws Exception {
        Files.write(dataFile, UNREADABLE_DATA);

        Model model = new MainApp().initModelManager(storageWith(new JsonAddressBookStorage(dataFile)),
                new UserPrefs());

        assertEquals(new AddressBook(), model.getAddressBook());
        List<Path> backups = backupFiles();
        assertEquals(1, backups.size());
        assertArrayEquals(UNREADABLE_DATA, Files.readAllBytes(backups.get(0)));
    }

    @Test
    public void initModelManager_missingFile_startsWithSampleData() throws Exception {
        Model model = new MainApp().initModelManager(storageWith(new JsonAddressBookStorage(dataFile)),
                new UserPrefs());

        assertEquals(SampleDataUtil.getSampleAddressBook(), model.getAddressBook());
        assertEquals(List.of(), backupFiles());
    }

    private Storage storageWith(JsonAddressBookStorage addressBookStorage) {
        return new StorageManager(addressBookStorage,
                new JsonUserPrefsStorage(temporaryFolder.resolve("preferences.json")));
    }

    private List<Path> backupFiles() throws IOException {
        try (Stream<Path> files = Files.list(temporaryFolder)) {
            return files.filter(file -> file.getFileName().toString().endsWith(".bak")).toList();
        }
    }
}
