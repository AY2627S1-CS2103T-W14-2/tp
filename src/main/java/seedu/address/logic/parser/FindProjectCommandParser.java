package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;

import java.util.regex.Pattern;

import seedu.address.logic.commands.FindProjectCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.ProjectContainsKeywordPredicate;

/**
 * Parses a project search phrase and creates a {@code FindProjectCommand}.
 */
public class FindProjectCommandParser implements Parser<FindProjectCommand> {
    // Project.normalize() collapses whitespace/separators, then strip() removes Java whitespace at the edges.
    private static final Pattern SURROUNDING_WHITESPACE =
            Pattern.compile("^[\\s\\p{Z}\\p{javaWhitespace}]+|[\\s\\p{Z}\\p{javaWhitespace}]+$");

    /**
     * Parses the whole argument as one phrase, validating its length before internal whitespace is collapsed.
     *
     * @throws ParseException if the phrase is blank or exceeds the search keyword limit.
     */
    @Override
    public FindProjectCommand parse(String args) throws ParseException {
        requireNonNull(args);
        String trimmedKeyword = SURROUNDING_WHITESPACE.matcher(args).replaceAll("");
        if (trimmedKeyword.isEmpty()) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, FindProjectCommand.MESSAGE_USAGE));
        }
        if (trimmedKeyword.codePointCount(0, trimmedKeyword.length()) > FindProjectCommand.MAX_KEYWORD_LENGTH) {
            throw new ParseException(FindProjectCommand.MESSAGE_KEYWORD_TOO_LONG);
        }

        return new FindProjectCommand(new ProjectContainsKeywordPredicate(trimmedKeyword));
    }
}
