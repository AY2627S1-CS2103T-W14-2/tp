package seedu.address;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.logging.Logger;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import seedu.address.commons.core.LogsCenter;
import seedu.address.commons.exceptions.DataLoadingException;
import seedu.address.commons.util.StringUtil;
import seedu.address.logic.Logic;
import seedu.address.logic.LogicManager;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.ReadOnlyUserPrefs;
import seedu.address.model.UserPrefs;
import seedu.address.model.util.SampleDataUtil;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.Storage;
import seedu.address.storage.StorageManager;
import seedu.address.ui.Ui;
import seedu.address.ui.UiManager;

/**
 * Runs the application.
 */
public class MainApp extends Application {

    public static final String VERSION = "V1.5";

    static final String MESSAGE_BACKUP_FAILED = "LinkUp could not load %1$s, and could not back it up (%2$s).\n\n"
            + "LinkUp will not start, so that the file is not overwritten. Copy the file somewhere safe, "
            + "fix the storage or permission problem, then start LinkUp again.";

    static final String MESSAGE_UNREADABLE_DATA_FILE = "Could not read %1$s, so LinkUp started with an empty "
            + "contact list.\nClose LinkUp before running any command, then see \"Editing the data file\" in the "
            + "User Guide.%2$s";
    static final String MESSAGE_UNREADABLE_FILE_BACKUP = "\nBackup: %1$s";

    private static final Logger logger = LogsCenter.getLogger(MainApp.class);
    private static final Path USER_PREFS_FILE_PATH = Paths.get("preferences.json");
    private static final Path ADDRESS_BOOK_FILE_PATH = Paths.get("data", "addressbook.json");

    protected Ui ui;
    protected Logic logic;
    protected Storage storage;
    protected Model model;

    /** Set when startup must stop before the UI opens; {@code null} if startup succeeded. */
    private String startupErrorMessage;

    /** Shown in the result box once the UI opens; {@code null} if the data file loaded normally. */
    private String startupMessage;

    @Override
    public void init() throws Exception {
        logger.info("===============================[ Initializing LinkUp ]==============================");
        super.init();

        JsonUserPrefsStorage userPrefsStorage = new JsonUserPrefsStorage(USER_PREFS_FILE_PATH);
        UserPrefs userPrefs = initPrefs(userPrefsStorage);
        JsonAddressBookStorage addressBookStorage = new JsonAddressBookStorage(ADDRESS_BOOK_FILE_PATH);
        storage = new StorageManager(addressBookStorage, userPrefsStorage);

        try {
            model = initModelManager(storage, userPrefs);
        } catch (IOException e) {
            logger.severe("Could not back up the unreadable data file: " + StringUtil.getDetails(e));
            startupErrorMessage = String.format(MESSAGE_BACKUP_FAILED, storage.getAddressBookFilePath(),
                    e.getMessage());
            // Logic and UI are not created, so nothing can save over the unreadable data file
            return;
        }

        logic = new LogicManager(model, storage);

        ui = new UiManager(logic, storage.getAddressBookFilePath(), startupMessage);
    }

    /**
     * Returns a {@code ModelManager} with the data from {@code storage}'s address book and {@code userPrefs}. <br>
     * The data from the sample address book will be used instead if {@code storage}'s address book is not found,
     * or an empty address book will be used instead if errors occur when reading {@code storage}'s address book,
     * but only after the unreadable file has been backed up.
     *
     * @throws IOException if the unreadable data file could not be backed up.
     */
    Model initModelManager(Storage storage, ReadOnlyUserPrefs userPrefs) throws IOException {
        logger.info("Using data file : " + storage.getAddressBookFilePath());

        Optional<ReadOnlyAddressBook> addressBookOptional;
        ReadOnlyAddressBook initialData;
        try {
            addressBookOptional = storage.readAddressBook();
            if (addressBookOptional.isEmpty()) {
                logger.info("Creating a new data file " + storage.getAddressBookFilePath()
                        + " populated with a sample AddressBook.");
            }
            initialData = addressBookOptional.orElseGet(SampleDataUtil::getSampleAddressBook);
        } catch (DataLoadingException e) {
            logger.warning("Data file at " + storage.getAddressBookFilePath() + " could not be loaded.");
            Optional<Path> backup = backUpUnreadableAddressBook(storage);
            startupMessage = createUnreadableDataFileMessage(storage.getAddressBookFilePath(), backup);
            logger.warning("Will be starting with an empty AddressBook.");
            initialData = new AddressBook();
        }

        return new ModelManager(initialData, userPrefs);
    }

    /**
     * Preserves the unreadable data file before the next save replaces it with the empty address book.
     *
     * @return the backup's path, or an empty {@code Optional} if the data file no longer exists.
     * @throws IOException if the backup could not be written.
     */
    private Optional<Path> backUpUnreadableAddressBook(Storage storage) throws IOException {
        Optional<Path> backup = storage.backupAddressBook();
        backup.ifPresent(path -> logger.warning("The unreadable data file was copied to " + path));
        return backup;
    }

    /**
     * Explains that {@code dataFile} could not be read and names its {@code backup}, if one was made,
     * so that users know their contacts can still be recovered.
     */
    static String createUnreadableDataFileMessage(Path dataFile, Optional<Path> backup) {
        String backupSentence = backup
                .map(path -> String.format(MESSAGE_UNREADABLE_FILE_BACKUP, path.getFileName()))
                .orElse("");
        return String.format(MESSAGE_UNREADABLE_DATA_FILE, dataFile, backupSentence);
    }

    /**
     * Returns the message to show once the UI opens, if loading the data file needs the user's attention.
     */
    Optional<String> getStartupMessage() {
        return Optional.ofNullable(startupMessage);
    }

    /**
     * Returns a {@code UserPrefs} using the file at {@code storage}'s user prefs file path,
     * or a new {@code UserPrefs} with default configuration if errors occur when
     * reading from the file.
     */
    protected UserPrefs initPrefs(JsonUserPrefsStorage storage) {
        Path prefsFilePath = storage.getUserPrefsFilePath();
        logger.info("Using preference file : " + prefsFilePath);

        UserPrefs initializedPrefs;
        try {
            Optional<UserPrefs> prefsOptional = storage.readUserPrefs();
            if (prefsOptional.isEmpty()) {
                logger.info("Creating new preference file " + prefsFilePath);
            }
            initializedPrefs = prefsOptional.orElse(new UserPrefs());
        } catch (DataLoadingException e) {
            logger.warning("Preference file at " + prefsFilePath + " could not be loaded."
                    + " Using default preferences.");
            initializedPrefs = new UserPrefs();
        }

        //Update prefs file in case it was missing to begin with or there are new/unused fields
        try {
            storage.saveUserPrefs(initializedPrefs);
        } catch (IOException e) {
            logger.warning("Failed to save preference file : " + StringUtil.getDetails(e));
        }

        return initializedPrefs;
    }

    @Override
    public void start(Stage primaryStage) {
        logger.info("Starting LinkUp " + MainApp.VERSION);
        if (startupErrorMessage != null) {
            showStartupErrorAndExit(startupErrorMessage);
            return;
        }
        ui.start(primaryStage);
    }

    /**
     * Shows {@code message} in an error dialog and exits without opening the main window.
     */
    private void showStartupErrorAndExit(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("LinkUp cannot start");
        alert.setHeaderText("Your data file could not be loaded or backed up");
        alert.setContentText(message);
        alert.showAndWait();
        Platform.exit();
    }

    @Override
    public void stop() {
        logger.info("=============================== [ Stopping LinkUp ] ================================");
        if (model == null) {
            return; // startup stopped before any data was loaded, so there is nothing to save
        }
        try {
            storage.saveUserPrefs(model.getUserPrefs());
        } catch (IOException e) {
            logger.severe("Failed to save preferences " + StringUtil.getDetails(e));
        }
    }
}
