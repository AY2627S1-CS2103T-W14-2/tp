package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.logging.StreamHandler;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.commons.core.LogsCenter;
import seedu.address.commons.exceptions.DataLoadingException;
import seedu.address.commons.util.JsonUtil;

/**
 * Checks that failed address book loads do not write contact details to the application log.
 */
public class JsonAddressBookStorageLoggingTest {

    @TempDir
    public Path testFolder;

    private final ByteArrayOutputStream logOutput = new ByteArrayOutputStream();
    private final StreamHandler logHandler = new StreamHandler(logOutput, new SimpleFormatter());
    private Logger baseLogger;

    @BeforeEach
    public void setUp() {
        baseLogger = LogsCenter.getLogger(JsonUtil.class).getParent();
        logHandler.setLevel(Level.ALL);
        baseLogger.addHandler(logHandler);
    }

    @AfterEach
    public void tearDown() {
        baseLogger.removeHandler(logHandler);
        logHandler.close();
    }

    @Test
    public void read_malformedJson_doesNotLogFileContent() throws Exception {
        Path file = testFolder.resolve("malformed.json");
        Files.writeString(file, """
                {"persons":[{"name":"Private Person","phone":"94351253",
                "email":"private@example.com",}]}
                """);

        DataLoadingException exception = assertThrows(DataLoadingException.class, () ->
                JsonUtil.readJsonFile(file, JsonSerializableAddressBook.class));

        String logs = getLogOutput();
        assertTrue(logs.contains(file.toString()));
        assertTrue(logs.contains(exception.getCause().getClass().getSimpleName()));
        assertFalse(logs.contains("Private Person"));
        assertFalse(logs.contains("94351253"));
        assertFalse(logs.contains("private@example.com"));
    }

    @Test
    public void read_duplicateContacts_doesNotLogValidationDetails() throws Exception {
        Path file = testFolder.resolve("duplicate.json");
        Files.writeString(file, """
                {"persons":[
                  {"name":"Private Alice","phone":"94351253","email":"alice@example.com",
                   "telegramUsername":"private_alice","projects":[]},
                  {"name":"Private Bob","phone":"94351253","email":"bob@example.com",
                   "telegramUsername":"private_bob","projects":[]}
                ]}
                """);

        DataLoadingException exception = assertThrows(DataLoadingException.class, () ->
                new JsonAddressBookStorage(file).readAddressBook());

        assertEquals("IllegalValueException", exception.getCause().getClass().getSimpleName());
        String logs = getLogOutput();
        assertTrue(logs.contains(file.toString()));
        assertTrue(logs.contains("IllegalValueException"));
        assertFalse(logs.contains("Private Alice"));
        assertFalse(logs.contains("Private Bob"));
        assertFalse(logs.contains("94351253"));
        assertFalse(logs.contains("alice@example.com"));
        assertFalse(logs.contains("bob@example.com"));
    }

    private String getLogOutput() {
        logHandler.flush();
        return logOutput.toString(StandardCharsets.UTF_8);
    }
}
