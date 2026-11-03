package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.commands.CommandTestUtil.EMAIL_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.EMAIL_DESC_BOB;
import static seedu.address.logic.commands.CommandTestUtil.INVALID_EMAIL_DESC;
import static seedu.address.logic.commands.CommandTestUtil.INVALID_NAME_DESC;
import static seedu.address.logic.commands.CommandTestUtil.INVALID_PHONE_DESC;
import static seedu.address.logic.commands.CommandTestUtil.INVALID_PROJECT_DESC;
import static seedu.address.logic.commands.CommandTestUtil.INVALID_TELEGRAM_DESC;
import static seedu.address.logic.commands.CommandTestUtil.NAME_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.NAME_DESC_BOB;
import static seedu.address.logic.commands.CommandTestUtil.PHONE_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.PHONE_DESC_BOB;
import static seedu.address.logic.commands.CommandTestUtil.PREAMBLE_NON_EMPTY;
import static seedu.address.logic.commands.CommandTestUtil.PREAMBLE_WHITESPACE;
import static seedu.address.logic.commands.CommandTestUtil.PROJECT_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.PROJECT_DESC_BOB;
import static seedu.address.logic.commands.CommandTestUtil.TELEGRAM_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.TELEGRAM_DESC_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_EMAIL_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_NAME_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_PHONE_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_PROJECT_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_TELEGRAM_USERNAME_BOB;
import static seedu.address.logic.parser.CliSyntax.PREFIX_EMAIL;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NAME;
import static seedu.address.logic.parser.CliSyntax.PREFIX_PHONE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_PROJECT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_TELEGRAM;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.testutil.TypicalPersons.BOB;

import org.junit.jupiter.api.Test;

import seedu.address.logic.Messages;
import seedu.address.logic.commands.AddCommand;
import seedu.address.model.person.Email;
import seedu.address.model.person.Name;
import seedu.address.model.person.Person;
import seedu.address.model.person.Phone;
import seedu.address.model.person.TelegramUsername;
import seedu.address.model.project.Project;
import seedu.address.testutil.PersonBuilder;

public class AddCommandParserTest {
    private AddCommandParser parser = new AddCommandParser();

    @Test
    public void parse_removedFields_failure() {
        String validInput = NAME_DESC_BOB + PHONE_DESC_BOB + EMAIL_DESC_BOB + TELEGRAM_DESC_BOB + PROJECT_DESC_BOB;
        for (String removedField : new String[] {" a/NUS", " a/", " t/friend", " t/", " a/NUS t/friend"}) {
            assertParseFailure(parser, validInput + removedField, ParserUtil.MESSAGE_REMOVED_CONTACT_FIELDS);
            assertParseFailure(parser, removedField + validInput, ParserUtil.MESSAGE_REMOVED_CONTACT_FIELDS);
        }
    }

    @Test
    public void parse_projectSymbols_success() {
        Person expectedPerson = new PersonBuilder(BOB).withProjects("Team *()!%$#&").build();
        assertParseSuccess(parser, NAME_DESC_BOB + PHONE_DESC_BOB + EMAIL_DESC_BOB + TELEGRAM_DESC_BOB
                + " pr/Team *()!%$#&", new AddCommand(expectedPerson));
    }

    @Test
    public void parse_allFieldsPresent_success() {
        Person expectedPerson = new PersonBuilder(BOB).withProjects(VALID_PROJECT_BOB)
                .build();

        // EP: whitespace only preamble
        assertParseSuccess(parser, PREAMBLE_WHITESPACE + NAME_DESC_BOB + PHONE_DESC_BOB + EMAIL_DESC_BOB
                + TELEGRAM_DESC_BOB + PROJECT_DESC_BOB,
                new AddCommand(expectedPerson));

    }

    @Test
    public void parse_nameWithRepeatedWhitespace_storesSingleSpaces() {
        Person expectedPerson = new PersonBuilder(BOB).withProjects(VALID_PROJECT_BOB).build();
        assertParseSuccess(parser, " n/Bob \t\u00A0 Choo" + PHONE_DESC_BOB + EMAIL_DESC_BOB
                + TELEGRAM_DESC_BOB + PROJECT_DESC_BOB, new AddCommand(expectedPerson));
    }

    @Test
    public void parse_formattedInternationalPhone_storesCanonicalValue() {
        Person expectedPerson = new PersonBuilder(BOB).withPhone("+6591234567")
                .withProjects(VALID_PROJECT_BOB).build();
        assertParseSuccess(parser, NAME_DESC_BOB + " p/+65 9123-4567" + EMAIL_DESC_BOB
                + TELEGRAM_DESC_BOB + PROJECT_DESC_BOB, new AddCommand(expectedPerson));
    }

    @Test
    public void parse_overlongEmail_rejectedWithSpecificMessage() {
        assertParseFailure(parser, NAME_DESC_BOB + PHONE_DESC_BOB + " e/" + "a.".repeat(17_770) + "a@example.com"
                + TELEGRAM_DESC_BOB + PROJECT_DESC_BOB, Email.MESSAGE_TOO_LONG);
    }

    @Test
    public void parse_overlongDomainLabel_rejected() {
        assertParseFailure(parser, NAME_DESC_BOB + PHONE_DESC_BOB + " e/a@"
                + "b".repeat(Email.MAX_DOMAIN_LABEL_LENGTH + 1) + ".com" + TELEGRAM_DESC_BOB + PROJECT_DESC_BOB,
                Email.MESSAGE_CONSTRAINTS);
    }

    @Test
    public void parse_localPhone_rejectedForNewContact() {
        assertParseFailure(parser, NAME_DESC_BOB + " p/91234567" + EMAIL_DESC_BOB
                + TELEGRAM_DESC_BOB + PROJECT_DESC_BOB, Phone.MESSAGE_CONSTRAINTS);
    }

    @Test
    public void parse_newNameAndMixedCaseEmail_storesExpectedValues() {
        Person expectedPerson = new PersonBuilder(BOB).withName("O'Connor-Smith Jr. 2")
                .withEmail("alex!mail@example.com").withProjects(VALID_PROJECT_BOB).build();

        assertParseSuccess(parser, " n/O'Connor-Smith Jr. 2" + PHONE_DESC_BOB
                + " e/Alex!Mail@Example.COM" + TELEGRAM_DESC_BOB + PROJECT_DESC_BOB,
                new AddCommand(expectedPerson));
    }

    @Test
    public void parse_oldNameAndEmailInputs_rejectedForNewContact() {
        assertParseFailure(parser, " n/12345" + PHONE_DESC_BOB + EMAIL_DESC_BOB + TELEGRAM_DESC_BOB + PROJECT_DESC_BOB,
                Name.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, NAME_DESC_BOB + PHONE_DESC_BOB + " e/alex@example"
                + TELEGRAM_DESC_BOB + PROJECT_DESC_BOB,
                Email.MESSAGE_CONSTRAINTS);
    }

    @Test
    public void parse_repeatedValue_failure() {
        String validExpectedPersonString = NAME_DESC_BOB + PHONE_DESC_BOB + EMAIL_DESC_BOB
                + TELEGRAM_DESC_BOB + PROJECT_DESC_BOB;

        assertRepeatedField(PREFIX_NAME, NAME_DESC_AMY, INVALID_NAME_DESC, validExpectedPersonString);
        assertRepeatedField(PREFIX_PHONE, PHONE_DESC_AMY, INVALID_PHONE_DESC, validExpectedPersonString);
        assertRepeatedField(PREFIX_EMAIL, EMAIL_DESC_AMY, INVALID_EMAIL_DESC, validExpectedPersonString);
        assertRepeatedField(PREFIX_TELEGRAM, TELEGRAM_DESC_AMY, INVALID_TELEGRAM_DESC, validExpectedPersonString);
        assertRepeatedField(PREFIX_PROJECT, PROJECT_DESC_AMY, INVALID_PROJECT_DESC, validExpectedPersonString);

        // EP: multiple fields repeated
        assertParseFailure(parser,
                validExpectedPersonString + PHONE_DESC_AMY + EMAIL_DESC_AMY + NAME_DESC_AMY
                        + TELEGRAM_DESC_AMY + PROJECT_DESC_AMY + validExpectedPersonString,
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_NAME, PREFIX_EMAIL, PREFIX_PHONE,
                        PREFIX_TELEGRAM, PREFIX_PROJECT));
    }

    /** Checks duplicate-prefix rejection takes precedence over value validation in either input order. */
    private void assertRepeatedField(Prefix prefix, String validField, String invalidField, String validInput) {
        String expectedMessage = Messages.getErrorMessageForDuplicatePrefixes(prefix);
        // EP: both repeated values are valid.
        assertParseFailure(parser, validField + validInput, expectedMessage);
        // EPs: an invalid value before or after a valid value still produces the duplicate-prefix error.
        assertParseFailure(parser, invalidField + validInput, expectedMessage);
        assertParseFailure(parser, validInput + invalidField, expectedMessage);
    }

    @Test
    public void parse_projectMissing_failure() {
        assertParseFailure(parser, NAME_DESC_AMY + PHONE_DESC_AMY + EMAIL_DESC_AMY + TELEGRAM_DESC_AMY,
                AddCommand.MESSAGE_PROJECT_REQUIRED);
    }

    @Test
    public void parse_projectWithRepeatedWhitespace_success() {
        Person expectedPerson = new PersonBuilder(BOB).withProjects("CS2103T Team Project").build();

        assertParseSuccess(parser, NAME_DESC_BOB + PHONE_DESC_BOB + EMAIL_DESC_BOB + TELEGRAM_DESC_BOB
                        + " " + PREFIX_PROJECT + "  CS2103T   Team Project  ",
                new AddCommand(expectedPerson));
    }

    @Test
    public void parse_compulsoryFieldMissing_failure() {
        String expectedMessage = String.format(MESSAGE_INVALID_COMMAND_FORMAT, AddCommand.MESSAGE_USAGE);

        // EP: missing name prefix
        assertParseFailure(parser, VALID_NAME_BOB + PHONE_DESC_BOB + EMAIL_DESC_BOB + TELEGRAM_DESC_BOB
                + PROJECT_DESC_BOB,
                expectedMessage);

        // EP: missing phone prefix
        assertParseFailure(parser, NAME_DESC_BOB + VALID_PHONE_BOB + EMAIL_DESC_BOB + TELEGRAM_DESC_BOB
                + PROJECT_DESC_BOB,
                expectedMessage);

        // EP: missing email prefix
        assertParseFailure(parser, NAME_DESC_BOB + PHONE_DESC_BOB + VALID_EMAIL_BOB + TELEGRAM_DESC_BOB
                + PROJECT_DESC_BOB,
                expectedMessage);

        // EP: missing Telegram username prefix
        assertParseFailure(parser, NAME_DESC_BOB + PHONE_DESC_BOB + EMAIL_DESC_BOB + VALID_TELEGRAM_USERNAME_BOB
                + PROJECT_DESC_BOB,
                expectedMessage);

        // EP: all prefixes missing
        assertParseFailure(parser, VALID_NAME_BOB + VALID_PHONE_BOB + VALID_EMAIL_BOB,
                expectedMessage);
    }

    @Test
    public void parse_invalidValue_failure() {
        // invalid name
        assertParseFailure(parser, INVALID_NAME_DESC + PHONE_DESC_BOB + EMAIL_DESC_BOB
                + TELEGRAM_DESC_BOB + PROJECT_DESC_BOB, Name.MESSAGE_CONSTRAINTS);

        // invalid phone
        assertParseFailure(parser, NAME_DESC_BOB + INVALID_PHONE_DESC + EMAIL_DESC_BOB
                + TELEGRAM_DESC_BOB + PROJECT_DESC_BOB, Phone.MESSAGE_CONSTRAINTS);

        // invalid email
        assertParseFailure(parser, NAME_DESC_BOB + PHONE_DESC_BOB + INVALID_EMAIL_DESC
                + TELEGRAM_DESC_BOB + PROJECT_DESC_BOB, Email.MESSAGE_CONSTRAINTS);

        // invalid Telegram username
        assertParseFailure(parser, NAME_DESC_BOB + PHONE_DESC_BOB + EMAIL_DESC_BOB + INVALID_TELEGRAM_DESC
                + PROJECT_DESC_BOB,
                TelegramUsername.MESSAGE_CONSTRAINTS);

        // Telegram username that Telegram itself rejects (issue #189)
        for (String username : new String[] {"1alex", "_alex", "alex_", "@12345"}) {
            assertParseFailure(parser, NAME_DESC_BOB + PHONE_DESC_BOB + EMAIL_DESC_BOB + " tele/" + username
                    + PROJECT_DESC_BOB, TelegramUsername.MESSAGE_CONSTRAINTS);
        }

        // invalid project
        assertParseFailure(parser, NAME_DESC_BOB + PHONE_DESC_BOB + EMAIL_DESC_BOB + TELEGRAM_DESC_BOB
                + INVALID_PROJECT_DESC, Project.MESSAGE_CONSTRAINTS);

        // two invalid values, only first invalid value reported
        assertParseFailure(parser, INVALID_NAME_DESC + INVALID_PHONE_DESC + EMAIL_DESC_BOB + TELEGRAM_DESC_BOB
                + PROJECT_DESC_BOB,
                Name.MESSAGE_CONSTRAINTS);

        // EP: non-empty preamble
        assertParseFailure(parser, PREAMBLE_NON_EMPTY + NAME_DESC_BOB + PHONE_DESC_BOB + EMAIL_DESC_BOB
                + TELEGRAM_DESC_BOB + PROJECT_DESC_BOB,
                String.format(MESSAGE_INVALID_COMMAND_FORMAT, AddCommand.MESSAGE_USAGE));
    }
}
