package seedu.address;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
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

import seedu.address.commons.core.GuiSettings;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyUserPrefs;
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

    @Test
    public void init_validStorage_connectsModelLogicAndUi() throws Exception {
        MainApp app = new IsolatedMainApp();
        app.init();
        assertNotNull(app.logic);
        assertNotNull(app.ui);
        assertEquals(SampleDataUtil.getSampleAddressBook(), app.model.getAddressBook());
        boolean[] started = {false};
        app.ui = stage -> started[0] = true;
        app.start(null);
        assertTrue(started[0]);
    }

    @Test
    public void init_backupFailure_doesNotCreateLogicOrUi() throws Exception {
        MainApp app = new IsolatedMainApp() {
            @Override
            Model initModelManager(Storage storage, ReadOnlyUserPrefs prefs) throws IOException {
                throw new IOException("Backup unavailable");
            }
        };
        app.init();
        assertNull(app.model);
        assertNull(app.logic);
        assertNull(app.ui);
        app.stop();
    }

    /** Redirects startup file access to the test directory while exercising application wiring. */
    private class IsolatedMainApp extends MainApp {
        @Override
        protected UserPrefs initPrefs(JsonUserPrefsStorage ignored) {
            return super.initPrefs(new JsonUserPrefsStorage(temporaryFolder.resolve("preferences.json")));
        }

        @Override
        Model initModelManager(Storage ignored, ReadOnlyUserPrefs prefs) throws IOException {
            storage = storageWith(new JsonAddressBookStorage(dataFile));
            return super.initModelManager(storage, prefs);
        }
    }

    @Test
    public void initPrefs_missingAndCorruptFiles_recreatesDefaults() throws Exception {
        JsonUserPrefsStorage prefs = new JsonUserPrefsStorage(temporaryFolder.resolve("preferences.json"));
        MainApp app = new MainApp();
        assertEquals(new UserPrefs(), app.initPrefs(prefs));
        assertEquals(new UserPrefs(), prefs.readUserPrefs().orElseThrow());
        Files.writeString(prefs.getUserPrefsFilePath(), "not JSON");
        assertEquals(new UserPrefs(), app.initPrefs(prefs));
        assertEquals(new UserPrefs(), prefs.readUserPrefs().orElseThrow());
    }

    @Test
    public void initPrefs_existingSettings_preservesWindowPosition() throws Exception {
        JsonUserPrefsStorage prefs = new JsonUserPrefsStorage(temporaryFolder.resolve("preferences.json"));
        UserPrefs expected = new UserPrefs();
        expected.setGuiSettings(new GuiSettings(900, 700, 20, 30));
        prefs.saveUserPrefs(expected);
        assertEquals(expected, new MainApp().initPrefs(prefs));
        assertEquals(expected, prefs.readUserPrefs().orElseThrow());
    }

    @Test
    public void initPrefs_saveFails_stillReturnsUsableDefaults() {
        JsonUserPrefsStorage prefs = new JsonUserPrefsStorage(temporaryFolder.resolve("preferences.json")) {
            @Override
            public void saveUserPrefs(ReadOnlyUserPrefs userPrefs) throws IOException {
                throw new IOException("Disk full");
            }
        };
        assertEquals(new UserPrefs(), new MainApp().initPrefs(prefs));
    }

    @Test
    public void stop_savesPreferencesAndHandlesStorageFailure() throws Exception {
        MainApp app = new MainApp();
        app.stop(); // Failed startup must not attempt a save.
        app.model = new ModelManager();
        app.storage = storageWith(new JsonAddressBookStorage(dataFile));
        app.stop();
        assertEquals(app.model.getUserPrefs(), app.storage.readUserPrefs().orElseThrow());
        app.storage = new StorageManager(new JsonAddressBookStorage(dataFile),
                new JsonUserPrefsStorage(temporaryFolder.resolve("failingPrefs.json"))) {
            @Override
            public void saveUserPrefs(ReadOnlyUserPrefs prefs) throws IOException {
                throw new IOException("Disk full");
            }
        };
        app.stop(); // Shutdown must not propagate a preferences-save failure.
        assertEquals(new UserPrefs(), app.model.getUserPrefs());
    }

    private List<Path> backupFiles() throws IOException {
        try (Stream<Path> files = Files.list(temporaryFolder)) {
            return files.filter(file -> file.getFileName().toString().endsWith(".bak")).toList();
        }
    }
}
