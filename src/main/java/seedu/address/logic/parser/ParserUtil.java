package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.parser.CliSyntax.PREFIX_ADDRESS;
import static seedu.address.logic.parser.CliSyntax.PREFIX_TAG;

import seedu.address.commons.core.index.Index;
import seedu.address.commons.util.StringUtil;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.Email;
import seedu.address.model.person.Name;
import seedu.address.model.person.Phone;
import seedu.address.model.person.TelegramUsername;
import seedu.address.model.project.Project;

/**
 * Contains utility methods used for parsing strings in the various *Parser classes.
 */
public class ParserUtil {

    public static final String MESSAGE_REMOVED_CONTACT_FIELDS =
            "Address and tags are no longer supported. Remove a/ and t/ from your command.";

    public static final String MESSAGE_INVALID_INDEX = "Index must be a positive integer.";

    /**
     * Parses {@code oneBasedIndex} into an {@code Index} and returns it. Leading and trailing whitespaces will be
     * trimmed.
     * @throws ParseException if the specified index is invalid (not a non-zero unsigned integer).
     */
    public static Index parseIndex(String oneBasedIndex) throws ParseException {
        String trimmedIndex = oneBasedIndex.trim();
        if (!StringUtil.isNonZeroUnsignedInteger(trimmedIndex)) {
            throw new ParseException(MESSAGE_INVALID_INDEX);
        }
        return Index.fromOneBased(Integer.parseInt(trimmedIndex));
    }

    /**
     * Parses a {@code String name} into a {@code Name}.
     * Leading and trailing whitespaces will be trimmed.
     *
     * @throws ParseException if the given {@code name} is invalid.
     */
    public static Name parseName(String name) throws ParseException {
        requireNonNull(name);
        String trimmedName = name.trim();
        if (!Name.isValidName(trimmedName)) {
            throw new ParseException(Name.MESSAGE_CONSTRAINTS);
        }
        return new Name(trimmedName);
    }

    /**
     * Parses a {@code String phone} into a {@code Phone}.
     * Leading and trailing whitespaces will be trimmed.
     *
     * @throws ParseException if the given {@code phone} is invalid.
     */
    public static Phone parsePhone(String phone) throws ParseException {
        requireNonNull(phone);
        String trimmedPhone = phone.trim();
        if (!Phone.isValidPhone(trimmedPhone)) {
            throw new ParseException(Phone.MESSAGE_CONSTRAINTS);
        }
        return new Phone(trimmedPhone);
    }

    /**
     * Parses a {@code String email} into an {@code Email}.
     * Leading and trailing whitespaces will be trimmed.
     *
     * @throws ParseException if the given {@code email} is invalid.
     */
    public static Email parseEmail(String email) throws ParseException {
        requireNonNull(email);
        String trimmedEmail = email.trim();
        if (Email.isTooLong(trimmedEmail)) {
            throw new ParseException(Email.MESSAGE_TOO_LONG);
        }
        if (!Email.isValidEmail(trimmedEmail)) {
            throw new ParseException(Email.MESSAGE_CONSTRAINTS);
        }
        return new Email(trimmedEmail);
    }

    /**
     * Parses a {@code String telegramUsername} into a {@code TelegramUsername}.
     * Leading and trailing whitespaces will be trimmed. A leading {@code @} is accepted.
     *
     * @throws ParseException if the given {@code telegramUsername} is invalid.
     */
    public static TelegramUsername parseTelegramUsername(String telegramUsername) throws ParseException {
        requireNonNull(telegramUsername);
        if (!TelegramUsername.isValidTelegramUsername(telegramUsername)) {
            throw new ParseException(TelegramUsername.MESSAGE_CONSTRAINTS);
        }
        return new TelegramUsername(telegramUsername);
    }

    /**
     * Parses a {@code String projectName} into a {@code Project}.
     * Surrounding whitespace is removed and repeated internal whitespace is collapsed.
     *
     * @throws ParseException if the given {@code projectName} is invalid.
     */
    public static Project parseProject(String projectName) throws ParseException {
        requireNonNull(projectName);
        if (!Project.isValidProjectName(projectName)) {
            throw new ParseException(Project.MESSAGE_CONSTRAINTS);
        }
        return new Project(projectName);
    }

    /**
     * Rejects retired prefixes before their values can be interpreted as another contact field.
     */
    public static void rejectRemovedContactFields(ArgumentMultimap arguments) throws ParseException {
        if (arguments.getValue(PREFIX_ADDRESS).isPresent() || arguments.getValue(PREFIX_TAG).isPresent()) {
            throw new ParseException(MESSAGE_REMOVED_CONTACT_FIELDS);
        }
    }
}
