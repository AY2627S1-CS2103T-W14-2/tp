package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_ADDRESS;
import static seedu.address.logic.parser.CliSyntax.PREFIX_EMAIL;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NAME;
import static seedu.address.logic.parser.CliSyntax.PREFIX_PHONE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_PROJECT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_TAG;
import static seedu.address.logic.parser.CliSyntax.PREFIX_TELEGRAM;

import java.util.Optional;
import java.util.function.Consumer;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.EditCommand;
import seedu.address.logic.commands.EditCommand.EditPersonDescriptor;
import seedu.address.logic.parser.exceptions.ParseException;

/**
 * Parses input arguments and creates a new EditCommand object
 */
public class EditCommandParser implements Parser<EditCommand> {

    /**
     * Parses the given {@code String} of arguments in the context of the EditCommand
     * and returns an EditCommand object for execution.
     * Problems are reported one at a time, in this order: retired fields, projects, the index, repeated prefixes,
     * then the supplied values (name, phone, email, Telegram username), and finally a missing field.
     *
     * @throws ParseException if the user input does not conform to the expected format
     */
    public EditCommand parse(String args) throws ParseException {
        requireNonNull(args);
        ArgumentMultimap argMultimap = ArgumentTokenizer.tokenize(args, PREFIX_NAME, PREFIX_PHONE, PREFIX_EMAIL,
                PREFIX_TELEGRAM, PREFIX_PROJECT, PREFIX_ADDRESS, PREFIX_TAG);

        ParserUtil.rejectRemovedContactFields(argMultimap);
        rejectProjectField(argMultimap);
        Index index = parseIndex(argMultimap);
        argMultimap.verifyNoDuplicatePrefixesFor(PREFIX_NAME, PREFIX_PHONE, PREFIX_EMAIL, PREFIX_TELEGRAM);
        EditPersonDescriptor editPersonDescriptor = parseEditedFields(argMultimap);

        if (!editPersonDescriptor.isAnyFieldEdited()) {
            throw new ParseException(EditCommand.MESSAGE_NOT_EDITED);
        }

        return new EditCommand(index, editPersonDescriptor);
    }

    /**
     * Rejects {@code pr/}, whatever its value, because projects can only be added with the project command.
     */
    private static void rejectProjectField(ArgumentMultimap argMultimap) throws ParseException {
        if (argMultimap.getValue(PREFIX_PROJECT).isPresent()) {
            throw new ParseException(EditCommand.MESSAGE_PROJECT_NOT_EDITABLE);
        }
    }

    private static Index parseIndex(ArgumentMultimap argMultimap) throws ParseException {
        try {
            return ParserUtil.parseIndex(argMultimap.getPreamble());
        } catch (ParseException pe) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, EditCommand.MESSAGE_USAGE), pe);
        }
    }

    /**
     * Returns a descriptor holding only the supplied fields, each parsed with the same rules as {@code add}.
     * Fields are parsed in a fixed order, so the first invalid one is the one reported.
     */
    private static EditPersonDescriptor parseEditedFields(ArgumentMultimap argMultimap) throws ParseException {
        EditPersonDescriptor descriptor = new EditPersonDescriptor();
        parseIfPresent(argMultimap, PREFIX_NAME, ParserUtil::parseName, descriptor::setName);
        parseIfPresent(argMultimap, PREFIX_PHONE, ParserUtil::parsePhone, descriptor::setPhone);
        parseIfPresent(argMultimap, PREFIX_EMAIL, ParserUtil::parseEmail, descriptor::setEmail);
        parseIfPresent(argMultimap, PREFIX_TELEGRAM, ParserUtil::parseTelegramUsername,
                descriptor::setTelegramUsername);
        return descriptor;
    }

    /**
     * Parses the value of {@code prefix} with {@code parser} and passes it to {@code setter}, if the prefix was
     * supplied. Fields that were not supplied are left unset, so they keep their current values.
     */
    private static <T> void parseIfPresent(ArgumentMultimap argMultimap, Prefix prefix, FieldParser<T> parser,
            Consumer<T> setter) throws ParseException {
        Optional<String> value = argMultimap.getValue(prefix);
        if (value.isEmpty()) {
            return;
        }
        T parsedValue = parser.parse(value.get());
        assert parsedValue != null : "a field parser must return a value or throw";
        setter.accept(parsedValue);
    }

    /**
     * Parses a single field value, throwing a {@code ParseException} if it is invalid.
     */
    @FunctionalInterface
    private interface FieldParser<T> {
        T parse(String value) throws ParseException;
    }
}
