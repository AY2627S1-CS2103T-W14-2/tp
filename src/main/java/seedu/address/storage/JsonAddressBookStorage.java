package seedu.address.storage;

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
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
        Path backup = Files.createTempFile(filePath.toAbsolutePath().getParent(),
                filePath.getFileName() + ".unreadable-", ".bak");
        Files.copy(filePath, backup, StandardCopyOption.REPLACE_EXISTING);
        logger.info("Preserved unreadable contact data in " + backup);
        return Optional.of(backup);
    }

    /**
     * Preserves the original bytes before retired contact fields are lost on save.
     * A unique sibling file prevents overwriting earlier backups. Any backup failure
     * propagates to the caller before the original file is modified.
     */
    void backupLegacyData(Path filePath) throws IOException {
        byte[] original;
        try {
            original = Files.readAllBytes(filePath);
        } catch (NoSuchFileException e) {
            return;
        }
        if (original.length == 0 || !needsLegacyBackup(original)) {
            return;
        }
        Path backup = Files.createTempFile(filePath.toAbsolutePath().getParent(),
                filePath.getFileName() + ".pre-v1.3-", ".bak");
        Files.write(backup, original);
        logger.info("Preserved legacy contact data in " + backup);
    }

    /**
     * Returns true if any saved contact still has a retired address or tags field.
     * Malformed JSON returns false: an unreadable file is already preserved at startup by
     * {@link #backupAddressBook()}, so it is not backed up a second time as a migration.
     */
    private boolean needsLegacyBackup(byte[] original) throws IOException {
        JsonNode data;
        try {
            data = JsonUtil.fromJsonString(new String(original, StandardCharsets.UTF_8), JsonNode.class);
        } catch (JsonProcessingException e) {
            return false;
        }
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
