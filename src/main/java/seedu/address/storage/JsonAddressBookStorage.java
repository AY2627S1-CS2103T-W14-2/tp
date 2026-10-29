package seedu.address.storage;

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Optional;
import java.util.logging.Logger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;

import seedu.address.commons.core.LogsCenter;
import seedu.address.commons.exceptions.DataLoadingException;
import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.commons.util.FileUtil;
import seedu.address.commons.util.JsonUtil;
import seedu.address.model.ReadOnlyAddressBook;

/**
 * A class to access AddressBook data stored as a JSON file on the hard disk.
 */
public class JsonAddressBookStorage {

    private static final Logger logger = LogsCenter.getLogger(JsonAddressBookStorage.class);

    private Path filePath;
    // The exact bytes last backed up as unreadable, so a save does not back up the same broken file twice.
    private byte[] unreadableBytesBackedUp;

    public JsonAddressBookStorage(Path filePath) {
        this.filePath = filePath;
    }

    public Path getAddressBookFilePath() {
        return filePath;
    }

    /**
     * Returns AddressBook data as a {@link ReadOnlyAddressBook}.
     * Returns {@code Optional.empty()} if storage file is not found.
     *
     * @throws DataLoadingException if loading the data from storage failed.
     */
    public Optional<ReadOnlyAddressBook> readAddressBook() throws DataLoadingException {
        return readAddressBook(filePath);
    }

    /**
     * Similar to {@link #readAddressBook()}.
     *
     * @param filePath location of the data. Cannot be null.
     * @throws DataLoadingException if loading the data from storage failed.
     */
    public Optional<ReadOnlyAddressBook> readAddressBook(Path filePath) throws DataLoadingException {
        requireNonNull(filePath);

        Optional<JsonSerializableAddressBook> jsonAddressBook = JsonUtil.readJsonFile(
                filePath, JsonSerializableAddressBook.class);
        if (!jsonAddressBook.isPresent()) {
            return Optional.empty();
        }

        try {
            return Optional.of(jsonAddressBook.get().toModelType());
        } catch (IllegalValueException ive) {
            logger.info("Illegal values found in " + filePath + ": " + ive.getMessage());
            throw new DataLoadingException(ive);
        }
    }

    /**
     * Saves the given {@link ReadOnlyAddressBook} to the storage.
     * @param addressBook cannot be null.
     * @throws IOException if there was any problem writing to the file.
     */
    public void saveAddressBook(ReadOnlyAddressBook addressBook) throws IOException {
        saveAddressBook(addressBook, filePath);
    }

    /**
     * Similar to {@link #saveAddressBook(ReadOnlyAddressBook)}.
     *
     * @param filePath location of the data. Cannot be null.
     */
    public void saveAddressBook(ReadOnlyAddressBook addressBook, Path filePath) throws IOException {
        requireNonNull(addressBook);
        requireNonNull(filePath);

        backupLegacyData(filePath);
        FileUtil.createIfMissing(filePath);
        JsonUtil.saveJsonFile(new JsonSerializableAddressBook(addressBook), filePath);
    }

    /**
     * Copies the data file to a unique sibling backup file, so that a file that could not be loaded is not lost
     * when the next save replaces it.
     *
     * @return the path of the backup, or an empty {@code Optional} if the data file does not exist.
     * @throws IOException if the backup could not be written.
     */
    public Optional<Path> backupAddressBook() throws IOException {
        if (!Files.exists(filePath)) {
            return Optional.empty();
        }
        byte[] original = Files.readAllBytes(filePath);
        Path backup = writeBackup(filePath, ".unreadable-", original);
        unreadableBytesBackedUp = original;
        logger.info("Preserved unreadable contact data in " + backup);
        return Optional.of(backup);
    }

    /**
     * Preserves the original bytes before a save replaces them: a file with retired contact fields gets a
     * {@code .pre-v1.3} backup and malformed JSON gets an {@code .unreadable} backup. A unique sibling file
     * prevents overwriting earlier backups. Any backup failure propagates to the caller before the original
     * file is modified.
     */
    void backupLegacyData(Path filePath) throws IOException {
        byte[] original;
        try {
            original = Files.readAllBytes(filePath);
        } catch (NoSuchFileException e) {
            return;
        }
        if (original.length == 0) {
            return;
        }
        JsonNode data;
        try {
            data = JsonUtil.fromJsonString(new String(original, StandardCharsets.UTF_8), JsonNode.class);
        } catch (JsonProcessingException e) {
            backupMalformedData(filePath, original);
            return;
        }
        if (!hasRetiredFields(data)) {
            return;
        }
        Path backup = writeBackup(filePath, ".pre-v1.3-", original);
        logger.info("Preserved legacy contact data in " + backup);
    }

    /**
     * Backs up malformed JSON, unless {@link #backupAddressBook()} already backed up these exact bytes at
     * startup, so one broken file gets one backup but a file broken after startup is never lost.
     */
    private void backupMalformedData(Path filePath, byte[] original) throws IOException {
        boolean isDataFile = filePath.equals(this.filePath);
        if (isDataFile && Arrays.equals(original, unreadableBytesBackedUp)) {
            return;
        }
        Path backup = writeBackup(filePath, ".unreadable-", original);
        if (isDataFile) {
            unreadableBytesBackedUp = original;
        }
        logger.info("Preserved unreadable contact data in " + backup);
    }

    private static Path writeBackup(Path filePath, String infix, byte[] bytes) throws IOException {
        Path backup = Files.createTempFile(filePath.toAbsolutePath().getParent(),
                filePath.getFileName() + infix, ".bak");
        Files.write(backup, bytes);
        return backup;
    }

    /**
     * Returns true if any saved contact still has a retired address or tags field.
     */
    private static boolean hasRetiredFields(JsonNode data) {
        if (data == null) {
            return false;
        }
        for (JsonNode person : data.path("persons")) {
            if (person.has("address") || person.has("tags")) {
                return true;
            }
        }
        return false;
    }

}
