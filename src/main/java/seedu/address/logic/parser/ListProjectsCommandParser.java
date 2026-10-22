package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;

import seedu.address.logic.commands.ListProjectsCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/**
 * Parses input arguments and creates a new {@code ListProjectsCommand}.
 * Unlike {@code list}, extra arguments are rejected so that a mistyped {@code project INDEX pr/PROJECT}
 * cannot silently succeed as a project listing.
 */
public class ListProjectsCommandParser implements Parser<ListProjectsCommand> {

    /**
     * Returns a {@code ListProjectsCommand} if {@code args} contains only whitespace.
     *
     * @throws ParseException if any non-whitespace argument is given.
     */
    @Override
    public ListProjectsCommand parse(String args) throws ParseException {
        requireNonNull(args);
        if (!args.isBlank()) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, ListProjectsCommand.MESSAGE_USAGE));
        }
        return new ListProjectsCommand();
    }
}
