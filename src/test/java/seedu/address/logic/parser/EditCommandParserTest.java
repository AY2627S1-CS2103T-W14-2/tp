package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.commands.CommandTestUtil.EMAIL_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.EMAIL_DESC_BOB;
import static seedu.address.logic.commands.CommandTestUtil.INVALID_EMAIL_DESC;
import static seedu.address.logic.commands.CommandTestUtil.INVALID_NAME_DESC;
import static seedu.address.logic.commands.CommandTestUtil.INVALID_PHONE_DESC;
import static seedu.address.logic.commands.CommandTestUtil.INVALID_TELEGRAM_DESC;
import static seedu.address.logic.commands.CommandTestUtil.NAME_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.PHONE_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.PHONE_DESC_BOB;
import static seedu.address.logic.commands.CommandTestUtil.TELEGRAM_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.TELEGRAM_DESC_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_EMAIL_AMY;
import static seedu.address.logic.commands.CommandTestUtil.VALID_NAME_AMY;
import static seedu.address.logic.commands.CommandTestUtil.VALID_PHONE_AMY;
import static seedu.address.logic.commands.CommandTestUtil.VALID_PHONE_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_TELEGRAM_USERNAME_AMY;
import static seedu.address.logic.commands.CommandTestUtil.VALID_TELEGRAM_USERNAME_BOB;
import static seedu.address.logic.parser.CliSyntax.PREFIX_EMAIL;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NAME;
import static seedu.address.logic.parser.CliSyntax.PREFIX_PHONE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_TELEGRAM;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_PERSON;
import static seedu.address.testutil.TypicalIndexes.INDEX_THIRD_PERSON;

import java.util.Map;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.EditCommand;
import seedu.address.logic.commands.EditCommand.EditPersonDescriptor;
import seedu.address.model.person.Email;
import seedu.address.model.person.Name;
import seedu.address.model.person.Phone;
import seedu.address.model.person.TelegramUsername;
import seedu.address.testutil.EditPersonDescriptorBuilder;

public class EditCommandParserTest {

    private static final String MESSAGE_INVALID_FORMAT =
            String.format(MESSAGE_INVALID_COMMAND_FORMAT, EditCommand.MESSAGE_USAGE);

    private EditCommandParser parser = new EditCommandParser();

    @Test
    public void parse_removedFields_failure() {
        for (String removedField : new String[] {" a/NUS", " a/", " t/friend", " t/", " a/NUS t/friend"}) {
            assertParseFailure(parser, "1" + removedField, ParserUtil.MESSAGE_REMOVED_CONTACT_FIELDS);
            assertParseFailure(parser, "1" + NAME_DESC_AMY + removedField,
                    ParserUtil.MESSAGE_REMOVED_CONTACT_FIELDS);
            assertParseFailure(parser, "1" + removedField + NAME_DESC_AMY,
                    ParserUtil.MESSAGE_REMOVED_CONTACT_FIELDS);
        }
    }

    @Test
    public void parse_projectPrefix_rejectsWholeCommand() {
        assertEachFails(EditCommand.MESSAGE_PROJECT_NOT_EDITABLE,
                "1 pr/Orbital", // alone
                "1 pr/", // blank
                "1 pr/Orbital pr/CS2103T", // repeated
                "1" + NAME_DESC_AMY + " pr/Orbital", // after a valid field
                "1 pr/Orbital" + NAME_DESC_AMY, // before a valid field
                "1" + INVALID_NAME_DESC + " pr/Orbital", // reported before an invalid value
                "1" + NAME_DESC_AMY + NAME_DESC_AMY + " pr/Orbital", // reported before a repeated prefix
                "abc pr/Orbital", // reported before an invalid index
                " pr/Orbital"); // reported before a missing index (as passed on by AddressBookParser)
    }

    @Test
    public void parse_retiredAndProjectPrefixes_reportsRetiredFieldFirst() {
        assertEachFails(ParserUtil.MESSAGE_REMOVED_CONTACT_FIELDS, "1 a/NUS pr/Orbital", "1 pr/Orbital t/friend");
    }

    @Test
    public void parse_missingParts_failure() {
        // no index specified
        assertParseFailure(parser, VALID_NAME_AMY, MESSAGE_INVALID_FORMAT);
        assertParseFailure(parser, NAME_DESC_AMY, MESSAGE_INVALID_FORMAT);

        // no field specified
        assertEachFails(EditCommand.MESSAGE_NOT_EDITED, "1", "1   ");

        // no index and no field specified
        assertParseFailure(parser, "", MESSAGE_INVALID_FORMAT);
    }

    @Test
    public void parse_invalidPreamble_failure() {
        assertEachFails(MESSAGE_INVALID_FORMAT,
                "-5" + NAME_DESC_AMY, // negative index
                "0" + NAME_DESC_AMY, // zero index
                "+1" + NAME_DESC_AMY, // explicit sign
                "2147483648" + NAME_DESC_AMY, // beyond the largest integer
                "abc" + NAME_DESC_AMY, // not a number
                "1 abc" + NAME_DESC_AMY, // extra text after the index
                "1 some random string", // invalid arguments being parsed as preamble
                "1 i/ string", // invalid prefix being parsed as preamble
                "1 N/Alice", // prefixes are case-sensitive
                "1 TELE/alice_tan", // including the Telegram prefix
                "1n/Alice", // a prefix needs a space before it
                "0" + PHONE_DESC_AMY + PHONE_DESC_AMY); // the index is checked before repeated prefixes
    }

    @Test
    public void parse_blankValue_failure() {
        Map<Prefix, String> constraintsByPrefix = Map.of(PREFIX_NAME, Name.MESSAGE_CONSTRAINTS,
                PREFIX_PHONE, Phone.MESSAGE_CONSTRAINTS, PREFIX_EMAIL, Email.MESSAGE_CONSTRAINTS,
                PREFIX_TELEGRAM, TelegramUsername.MESSAGE_CONSTRAINTS);
        constraintsByPrefix.forEach((prefix, constraints) ->
                assertEachFails(constraints, "1 " + prefix, "1 " + prefix + "   "));

        assertParseFailure(parser, "1 tele/@", TelegramUsername.MESSAGE_CONSTRAINTS);
        // a blank field rejects the whole command, even when another field is valid
        assertParseFailure(parser, "1 n/Weien tele/", TelegramUsername.MESSAGE_CONSTRAINTS);
    }

    @Test
    public void parse_telegramUsernameBreaksTelegramRules_failure() {
        // issue #189
        assertEachFails(TelegramUsername.MESSAGE_CONSTRAINTS, "1 tele/1alex", "1 tele/_alex", "1 tele/alex_",
                "1 tele/@Alex_");
    }

    @Test
    public void parse_invalidValues_reportsFirstInFixedFieldOrder() {
        // fields are checked as name, phone, email, Telegram username, whatever order they are typed in
        assertParseFailure(parser, "1" + INVALID_TELEGRAM_DESC + INVALID_NAME_DESC, Name.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, "1" + INVALID_EMAIL_DESC + INVALID_PHONE_DESC, Phone.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, "1" + INVALID_TELEGRAM_DESC + INVALID_EMAIL_DESC, Email.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, "1" + INVALID_TELEGRAM_DESC + NAME_DESC_AMY,
                TelegramUsername.MESSAGE_CONSTRAINTS);
        // without a space, the next prefix becomes part of the previous value
        assertParseFailure(parser, "1 n/Alicetele/alice_tan", Name.MESSAGE_CONSTRAINTS);
    }

    @Test
    public void parse_invalidValue_failure() {
        assertParseFailure(parser, "1" + INVALID_NAME_DESC, Name.MESSAGE_CONSTRAINTS); // invalid name
        assertParseFailure(parser, "1 n/12345", Name.MESSAGE_CONSTRAINTS); // previously allowed name
        assertParseFailure(parser, "1" + INVALID_PHONE_DESC, Phone.MESSAGE_CONSTRAINTS); // invalid phone
        assertParseFailure(parser, "1 p/91234567", Phone.MESSAGE_CONSTRAINTS); // local phone needs country code
        assertParseFailure(parser, "1" + INVALID_EMAIL_DESC, Email.MESSAGE_CONSTRAINTS); // invalid email
        assertParseFailure(parser, "1 e/alex@example", Email.MESSAGE_CONSTRAINTS); // domain needs a dot

        // invalid phone followed by valid email
        assertParseFailure(parser, "1" + INVALID_PHONE_DESC + EMAIL_DESC_AMY, Phone.MESSAGE_CONSTRAINTS);

        // multiple invalid values, but only the first invalid value is captured
        assertParseFailure(parser, "1" + INVALID_NAME_DESC + INVALID_EMAIL_DESC + PHONE_DESC_AMY,
                Name.MESSAGE_CONSTRAINTS);
    }

    @Test
    public void parse_formattedInternationalPhone_storesCanonicalValue() {
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder().withPhone("+6591234567").build();

        assertParseSuccess(parser, "1 p/+65 9123-4567", new EditCommand(INDEX_FIRST_PERSON, descriptor));
    }

    @Test
    public void parse_allEditableFieldsSpecified_success() {
        Index targetIndex = INDEX_SECOND_PERSON;
        String userInput = targetIndex.getOneBased() + PHONE_DESC_BOB + TELEGRAM_DESC_BOB
                + EMAIL_DESC_AMY + NAME_DESC_AMY;

        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder().withName(VALID_NAME_AMY)
                .withPhone(VALID_PHONE_BOB).withEmail(VALID_EMAIL_AMY)
                .withTelegramUsername(VALID_TELEGRAM_USERNAME_BOB).build();
        EditCommand expectedCommand = new EditCommand(targetIndex, descriptor);

        assertParseSuccess(parser, userInput, expectedCommand);
    }

    @Test
    public void parse_fieldsInAnyOrder_sameCommand() {
        EditCommand expectedCommand = new EditCommand(INDEX_FIRST_PERSON, new EditPersonDescriptorBuilder()
                .withName("Alice").withTelegramUsername("alice_tan").build());

        assertParseSuccess(parser, "1 tele/@alice_tan n/Alice", expectedCommand);
        assertParseSuccess(parser, "1 n/Alice tele/@alice_tan", expectedCommand);
    }

    @Test
    public void parse_telegramUsername_normalisedLikeAdd() {
        EditCommand expectedCommand = new EditCommand(INDEX_FIRST_PERSON,
                new EditPersonDescriptorBuilder().withTelegramUsername("alice_tan").build());

        assertParseSuccess(parser, "1 tele/@Alice_Tan", expectedCommand);
        assertParseSuccess(parser, "1 tele/   alice_tan   ", expectedCommand);
    }

    @Test
    public void parse_someFieldsSpecified_success() {
        Index targetIndex = INDEX_FIRST_PERSON;
        String userInput = targetIndex.getOneBased() + PHONE_DESC_BOB + EMAIL_DESC_AMY;

        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder().withPhone(VALID_PHONE_BOB)
                .withEmail(VALID_EMAIL_AMY).build();
        EditCommand expectedCommand = new EditCommand(targetIndex, descriptor);

        assertParseSuccess(parser, userInput, expectedCommand);
    }

    @Test
    public void parse_oneFieldSpecified_success() {
        // name
        Index targetIndex = INDEX_THIRD_PERSON;
        String userInput = targetIndex.getOneBased() + NAME_DESC_AMY;
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder().withName(VALID_NAME_AMY).build();
        EditCommand expectedCommand = new EditCommand(targetIndex, descriptor);
        assertParseSuccess(parser, userInput, expectedCommand);

        // phone
        userInput = targetIndex.getOneBased() + PHONE_DESC_AMY;
        descriptor = new EditPersonDescriptorBuilder().withPhone(VALID_PHONE_AMY).build();
        expectedCommand = new EditCommand(targetIndex, descriptor);
        assertParseSuccess(parser, userInput, expectedCommand);

        // email
        userInput = targetIndex.getOneBased() + EMAIL_DESC_AMY;
        descriptor = new EditPersonDescriptorBuilder().withEmail(VALID_EMAIL_AMY).build();
        expectedCommand = new EditCommand(targetIndex, descriptor);
        assertParseSuccess(parser, userInput, expectedCommand);

        // Telegram username
        userInput = targetIndex.getOneBased() + TELEGRAM_DESC_AMY;
        descriptor = new EditPersonDescriptorBuilder().withTelegramUsername(VALID_TELEGRAM_USERNAME_AMY).build();
        expectedCommand = new EditCommand(targetIndex, descriptor);
        assertParseSuccess(parser, userInput, expectedCommand);
    }

    @Test
    public void parse_telegramAfterName_notSwallowedIntoName() {
        // Regression: tele/ used to be unrecognised, so it became part of the name value
        EditCommand expectedCommand = new EditCommand(INDEX_FIRST_PERSON, new EditPersonDescriptorBuilder()
                .withName("Alice").withTelegramUsername("alice_tan").build());

        assertParseSuccess(parser, "1 n/Alice tele/@alice_tan", expectedCommand);
    }

    @Test
    public void parse_repeatedPrefix_failure() {
        Map<Prefix, String> fieldsByPrefix = Map.of(PREFIX_NAME, NAME_DESC_AMY, PREFIX_PHONE, PHONE_DESC_AMY,
                PREFIX_EMAIL, EMAIL_DESC_AMY, PREFIX_TELEGRAM, TELEGRAM_DESC_AMY);
        // identical values are still rejected
        fieldsByPrefix.forEach((prefix, field) ->
                assertParseFailure(parser, "1" + field + field, Messages.getErrorMessageForDuplicatePrefixes(prefix)));

        // a repeated prefix is reported before an invalid value in another field
        assertParseFailure(parser, "1" + INVALID_NAME_DESC + PHONE_DESC_AMY + PHONE_DESC_AMY,
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_PHONE));
        assertParseFailure(parser, "1" + TELEGRAM_DESC_AMY + INVALID_TELEGRAM_DESC,
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_TELEGRAM));
    }

    @Test
    public void parse_multipleRepeatedFields_failure() {
        // More extensive testing of duplicate parameter detections is done in
        // AddCommandParserTest#parse_repeatedValue_failure()

        // invalid followed by valid
        Index targetIndex = INDEX_FIRST_PERSON;
        String userInput = targetIndex.getOneBased() + INVALID_PHONE_DESC + PHONE_DESC_BOB;

        assertParseFailure(parser, userInput, Messages.getErrorMessageForDuplicatePrefixes(PREFIX_PHONE));

        // valid followed by invalid
        userInput = targetIndex.getOneBased() + PHONE_DESC_BOB + INVALID_PHONE_DESC;

        assertParseFailure(parser, userInput, Messages.getErrorMessageForDuplicatePrefixes(PREFIX_PHONE));

        // multiple valid fields repeated
        userInput = targetIndex.getOneBased() + PHONE_DESC_AMY + EMAIL_DESC_AMY + PHONE_DESC_AMY + EMAIL_DESC_AMY
                + PHONE_DESC_BOB + EMAIL_DESC_BOB;

        assertParseFailure(parser, userInput,
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_PHONE, PREFIX_EMAIL));

        // multiple invalid values
        userInput = targetIndex.getOneBased() + INVALID_PHONE_DESC + INVALID_EMAIL_DESC
                + INVALID_PHONE_DESC + INVALID_EMAIL_DESC;

        assertParseFailure(parser, userInput,
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_PHONE, PREFIX_EMAIL));
    }

    private void assertEachFails(String expectedMessage, String... userInputs) {
        for (String userInput : userInputs) {
            assertParseFailure(parser, userInput, expectedMessage);
        }
    }
}
