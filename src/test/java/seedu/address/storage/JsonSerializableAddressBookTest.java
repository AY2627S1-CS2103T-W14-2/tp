package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.ConflictTestUtil.ALICE_NAMESAKE;
import static seedu.address.testutil.TypicalPersons.ALICE;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.commons.util.JsonUtil;
import seedu.address.model.AddressBook;
import seedu.address.model.person.Email;
import seedu.address.model.person.TelegramUsername;
import seedu.address.testutil.AddressBookBuilder;
import seedu.address.testutil.TypicalPersons;

public class JsonSerializableAddressBookTest {

    private static final Path TEST_DATA_FOLDER = Paths.get("src", "test", "data", "JsonSerializableAddressBookTest");
    private static final Path TYPICAL_PERSONS_FILE = TEST_DATA_FOLDER.resolve("typicalPersonsAddressBook.json");
    private static final Path INVALID_PERSON_FILE = TEST_DATA_FOLDER.resolve("invalidPersonAddressBook.json");
    private static final Path DUPLICATE_PERSON_FILE = TEST_DATA_FOLDER.resolve("duplicatePersonAddressBook.json");
    private static final Path SAME_NAME_DISTINCT_DETAILS_FILE =
            TEST_DATA_FOLDER.resolve("sameNameDistinctDetailsAddressBook.json");
    private static final Path SAME_PHONE_DIFFERENT_NAMES_FILE =
            TEST_DATA_FOLDER.resolve("samePhoneDifferentNamesAddressBook.json");
    private static final Path CASE_DIFFERENT_EMAIL_FILE =
            TEST_DATA_FOLDER.resolve("caseDifferentEmailAddressBook.json");
    private static final Path SAME_TELEGRAM_USERNAME_FILE =
            TEST_DATA_FOLDER.resolve("sameTelegramUsernameAddressBook.json");
    private static final Path MISSING_TELEGRAM_USERNAME_FILE =
            TEST_DATA_FOLDER.resolve("missingTelegramUsernameAddressBook.json");

    @Test
    public void toModelType_typicalPersonsFile_success() throws Exception {
        AddressBook addressBookFromFile = readAddressBook(TYPICAL_PERSONS_FILE);
        AddressBook typicalPersonsAddressBook = TypicalPersons.getTypicalAddressBook();
        assertEquals(addressBookFromFile, typicalPersonsAddressBook);
    }

    @Test
    public void toModelType_invalidPersonFile_throwsIllegalValueException() throws Exception {
        assertLoadFails(INVALID_PERSON_FILE, Email.MESSAGE_CONSTRAINTS);
    }

    @Test
    public void toModelType_sameNameSamePhone_throwsIllegalValueException() throws Exception {
        assertLoadFails(DUPLICATE_PERSON_FILE, "Phone number 94351253 is already used by Alice Pauline. "
                + "Please enter a different phone number.");
    }

    @Test
    public void toModelType_samePhoneDifferentNames_throwsIllegalValueException() throws Exception {
        assertLoadFails(SAME_PHONE_DIFFERENT_NAMES_FILE, "Phone number 94351253 is already used by Alice Pauline. "
                + "Please enter a different phone number.");
    }

    @Test
    public void toModelType_emailDiffersOnlyInCase_throwsIllegalValueException() throws Exception {
        assertLoadFails(CASE_DIFFERENT_EMAIL_FILE, "Email Alice@Example.COM is already used by Alice Pauline. "
                + "Please enter a different email.");
    }

    @Test
    public void toModelType_sameTelegramUsername_throwsIllegalValueException() throws Exception {
        assertLoadFails(SAME_TELEGRAM_USERNAME_FILE, "Telegram username @alice_pauline is already used by "
                + "Alice Pauline. Please enter a different Telegram username.");
    }

    @Test
    public void toModelType_sameNameDistinctDetails_success() throws Exception {
        AddressBook expected = new AddressBookBuilder().withPerson(ALICE).withPerson(ALICE_NAMESAKE).build();
        assertEquals(expected, readAddressBook(SAME_NAME_DISTINCT_DETAILS_FILE));
    }

    @Test
    public void toModelType_contactWithoutTelegramUsername_throwsIllegalValueException() throws Exception {
        assertLoadFails(MISSING_TELEGRAM_USERNAME_FILE, String.format(JsonAdaptedPerson.MISSING_FIELD_MESSAGE_FORMAT,
                TelegramUsername.class.getSimpleName()));
    }

    private static JsonSerializableAddressBook readJsonFile(Path file) throws Exception {
        return JsonUtil.readJsonFile(file, JsonSerializableAddressBook.class).get();
    }

    private static AddressBook readAddressBook(Path file) throws Exception {
        return readJsonFile(file).toModelType();
    }

    private static void assertLoadFails(Path file, String expectedMessage) throws Exception {
        JsonSerializableAddressBook dataFromFile = readJsonFile(file);
        assertThrows(IllegalValueException.class, expectedMessage, dataFromFile::toModelType);
    }
}
