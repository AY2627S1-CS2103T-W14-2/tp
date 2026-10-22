package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NEW_PROJECT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_OLD_PROJECT;

import seedu.address.logic.commands.RenameProjectCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.project.Project;

/**
 * Parses old and new project names using the shared prefix syntax. Quotes and backslashes are literal.
 */
public class RenameProjectCommandParser implements Parser<RenameProjectCommand> {
    @Override
    public RenameProjectCommand parse(String args) throws ParseException {
        requireNonNull(args);
        ArgumentMultimap arguments = ArgumentTokenizer.tokenize(args, PREFIX_OLD_PROJECT, PREFIX_NEW_PROJECT);
        if (!arguments.getPreamble().isEmpty()
                || arguments.getValue(PREFIX_OLD_PROJECT).isEmpty()
                || arguments.getValue(PREFIX_NEW_PROJECT).isEmpty()) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT,
                    RenameProjectCommand.MESSAGE_USAGE));
        }
        arguments.verifyNoDuplicatePrefixesFor(PREFIX_OLD_PROJECT, PREFIX_NEW_PROJECT);
        Project oldProject = ParserUtil.parseProject(arguments.getValue(PREFIX_OLD_PROJECT).get());
        Project newProject = ParserUtil.parseProject(arguments.getValue(PREFIX_NEW_PROJECT).get());
        return new RenameProjectCommand(oldProject, newProject);
    }
}
