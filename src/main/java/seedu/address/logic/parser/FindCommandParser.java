package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;

import java.util.List;
import java.util.regex.Pattern;

import seedu.address.logic.commands.FindCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.NameContainsKeywordsPredicate;

/**
 * Parses input arguments and creates a new FindCommand object
 */
public class FindCommandParser implements Parser<FindCommand> {
    private static final String WHITESPACE = "[\\s\\p{Z}\\p{javaWhitespace}]+";
    private static final Pattern SURROUNDING_WHITESPACE = Pattern.compile("^" + WHITESPACE + "|" + WHITESPACE + "$");

    /**
     * Parses the given {@code String} of arguments in the context of the FindCommand
     * and returns a FindCommand object for execution.
     * @throws ParseException if the user input does not conform to the expected format
     */
    public FindCommand parse(String args) throws ParseException {
        requireNonNull(args);
        String trimmedArgs = SURROUNDING_WHITESPACE.matcher(args).replaceAll("");
        if (trimmedArgs.isEmpty()) {
            throw new ParseException(
                    String.format(MESSAGE_INVALID_COMMAND_FORMAT, FindCommand.MESSAGE_USAGE));
        }
        if (trimmedArgs.codePointCount(0, trimmedArgs.length()) > FindCommand.MAX_KEYWORD_LENGTH) {
            throw new ParseException(FindCommand.MESSAGE_KEYWORD_TOO_LONG);
        }

        String[] nameKeywords = trimmedArgs.split(WHITESPACE);

        return new FindCommand(new NameContainsKeywordsPredicate(List.of(nameKeywords)));
    }

}
