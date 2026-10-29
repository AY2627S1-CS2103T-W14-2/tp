package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.logic.parser.ParserUtil.MESSAGE_INVALID_INDEX;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;

import org.junit.jupiter.api.Test;

import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.Email;
import seedu.address.model.person.Name;
import seedu.address.model.person.Phone;
import seedu.address.model.person.TelegramUsername;
import seedu.address.model.project.Project;

public class ParserUtilTest {
    private static final String INVALID_NAME = "R@chel";
    private static final String INVALID_PHONE = "91234567";
    private static final String INVALID_EMAIL = "example.com";
    private static final String INVALID_TELEGRAM_USERNAME = "r@ch";
    private static final String INVALID_PROJECT = " ";

    private static final String VALID_NAME = "Rachel Walker";
    private static final String VALID_PHONE = "+6591234567";
    private static final String VALID_EMAIL = "rachel@example.com";
    private static final String VALID_TELEGRAM_USERNAME = "rachel_walker";
    private static final String VALID_PROJECT = "CS2103T Team Project";

    private static final String WHITESPACE = " \t\r\n";

    @Test
    public void parseIndex_invalidInput_throwsParseException() {
        assertThrows(ParseException.class, () -> ParserUtil.parseIndex("10 a"));
    }

    @Test
    public void parseIndex_outOfRangeInput_throwsParseException() {
        assertThrows(ParseException.class, MESSAGE_INVALID_INDEX, ()
            -> ParserUtil.parseIndex(Long.toString(Integer.MAX_VALUE + 1)));
    }

    @Test
    public void parseIndex_validInput_success() throws Exception {
        // No whitespaces
        assertEquals(INDEX_FIRST_PERSON, ParserUtil.parseIndex("1"));

        // Leading and trailing whitespaces
        assertEquals(INDEX_FIRST_PERSON, ParserUtil.parseIndex("  1  "));
    }

    @Test
    public void parseName_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> ParserUtil.parseName((String) null));
    }

    @Test
    public void parseName_invalidValue_throwsParseException() {
        assertThrows(ParseException.class, () -> ParserUtil.parseName(INVALID_NAME));
        assertThrows(ParseException.class, Name.MESSAGE_CONSTRAINTS, () -> ParserUtil.parseName("12345"));
        assertThrows(ParseException.class, Name.MESSAGE_CONSTRAINTS, () -> ParserUtil.parseName("A".repeat(61)));
    }

    @Test
    public void parseName_validValueWithoutWhitespace_returnsName() throws Exception {
        Name expectedName = new Name(VALID_NAME);
        assertEquals(expectedName, ParserUtil.parseName(VALID_NAME));
    }

    @Test
    public void parseName_validValueWithWhitespace_returnsTrimmedName() throws Exception {
        String nameWithWhitespace = WHITESPACE + VALID_NAME + WHITESPACE;
        Name expectedName = new Name(VALID_NAME);
        assertEquals(expectedName, ParserUtil.parseName(nameWithWhitespace));
        assertEquals(expectedName, ParserUtil.parseName("Rachel" + WHITESPACE + "\u00A0 Walker"));
    }

    @Test
    public void parsePhone_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> ParserUtil.parsePhone((String) null));
    }

    @Test
    public void parsePhone_invalidValue_throwsParseException() {
        assertThrows(ParseException.class, () -> ParserUtil.parsePhone(INVALID_PHONE));
    }

    @Test
    public void parsePhone_validValueWithoutWhitespace_returnsPhone() throws Exception {
        Phone expectedPhone = new Phone(VALID_PHONE);
        assertEquals(expectedPhone, ParserUtil.parsePhone(VALID_PHONE));
    }

    @Test
    public void parsePhone_validValueWithWhitespace_returnsTrimmedPhone() throws Exception {
        String phoneWithWhitespace = WHITESPACE + VALID_PHONE + WHITESPACE;
        Phone expectedPhone = new Phone(VALID_PHONE);
        assertEquals(expectedPhone, ParserUtil.parsePhone(phoneWithWhitespace));
    }

    @Test
    public void parsePhone_separators_returnsCanonicalPhone() throws Exception {
        assertEquals(VALID_PHONE, ParserUtil.parsePhone("+65 9123-4567").value);
    }

    @Test
    public void parsePhone_unverifiedCallingCode_acceptsValidFormat() throws Exception {
        assertEquals("+9991234567", ParserUtil.parsePhone("+9991234567").value);
    }

    @Test
    public void parseEmail_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> ParserUtil.parseEmail((String) null));
    }

    @Test
    public void parseEmail_invalidValue_throwsParseException() {
        assertThrows(ParseException.class, () -> ParserUtil.parseEmail(INVALID_EMAIL));
        assertThrows(ParseException.class, Email.MESSAGE_CONSTRAINTS, () -> ParserUtil.parseEmail("alex@example"));
        assertThrows(ParseException.class, Email.MESSAGE_CONSTRAINTS, () -> ParserUtil.parseEmail("alex@example..com"));
    }

    @Test
    public void parseEmail_tooLong_throwsSpecificParseException() {
        assertThrows(ParseException.class, Email.MESSAGE_TOO_LONG, () ->
                ParserUtil.parseEmail("a".repeat(65) + "@example.com"));
        assertThrows(ParseException.class, Email.MESSAGE_TOO_LONG, () ->
                ParserUtil.parseEmail("a@" + "b".repeat(250) + ".com"));
        assertThrows(ParseException.class, Email.MESSAGE_TOO_LONG, () ->
                ParserUtil.parseEmail("a.".repeat(17_770) + "a@example.com")); // issue #196
    }

    @Test
    public void parseEmail_validValueWithoutWhitespace_returnsEmail() throws Exception {
        Email expectedEmail = new Email(VALID_EMAIL);
        assertEquals(expectedEmail, ParserUtil.parseEmail(VALID_EMAIL));
    }

    @Test
    public void parseEmail_validValueWithWhitespace_returnsTrimmedEmail() throws Exception {
        String emailWithWhitespace = WHITESPACE + VALID_EMAIL + WHITESPACE;
        Email expectedEmail = new Email(VALID_EMAIL);
        assertEquals(expectedEmail, ParserUtil.parseEmail(emailWithWhitespace));
    }

    @Test
    public void parseEmail_mixedCase_storesLowercase() throws Exception {
        assertEquals("alex@example.com", ParserUtil.parseEmail("Alex@Example.COM").value);
    }

    @Test
    public void parseTelegramUsername_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> ParserUtil.parseTelegramUsername(null));
    }

    @Test
    public void parseTelegramUsername_invalidValue_throwsParseException() {
        assertThrows(ParseException.class, () -> ParserUtil.parseTelegramUsername(INVALID_TELEGRAM_USERNAME));
    }

    @Test
    public void parseTelegramUsername_validValue_returnsNormalizedTelegramUsername() throws Exception {
        TelegramUsername expectedTelegramUsername = new TelegramUsername(VALID_TELEGRAM_USERNAME);
        assertEquals(expectedTelegramUsername,
                ParserUtil.parseTelegramUsername(WHITESPACE + "@Rachel_Walker" + WHITESPACE));
    }

    @Test
    public void parseProject_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> ParserUtil.parseProject(null));
    }

    @Test
    public void parseProject_invalidValue_throwsParseException() {
        assertThrows(ParseException.class, () -> ParserUtil.parseProject(INVALID_PROJECT));
    }

    @Test
    public void parseProject_validValue_returnsNormalizedProject() throws Exception {
        Project expectedProject = new Project(VALID_PROJECT);
        assertEquals(expectedProject, ParserUtil.parseProject("  CS2103T   Team Project  "));
        assertEquals(VALID_PROJECT, ParserUtil.parseProject("  CS2103T   Team Project  ").projectName);
    }

}
