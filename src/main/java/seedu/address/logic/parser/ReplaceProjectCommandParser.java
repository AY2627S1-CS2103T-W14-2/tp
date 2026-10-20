package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NEW_PROJECT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_OLD_PROJECT;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.ReplaceProjectCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.project.Project;

/**
 * Parses a displayed contact index and old and new project names.
 */
public class ReplaceProjectCommandParser implements Parser<ReplaceProjectCommand> {
    @Override
    public ReplaceProjectCommand parse(String args) throws ParseException {
        requireNonNull(args);
        ArgumentMultimap arguments = ArgumentTokenizer.tokenize(args, PREFIX_OLD_PROJECT, PREFIX_NEW_PROJECT);
        Index index;
        try {
            index = ParserUtil.parseIndex(arguments.getPreamble());
        } catch (ParseException e) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT,
                    ReplaceProjectCommand.MESSAGE_USAGE), e);
        }
        if (arguments.getValue(PREFIX_OLD_PROJECT).isEmpty()
                || arguments.getValue(PREFIX_NEW_PROJECT).isEmpty()) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT,
                    ReplaceProjectCommand.MESSAGE_USAGE));
        }
        arguments.verifyNoDuplicatePrefixesFor(PREFIX_OLD_PROJECT, PREFIX_NEW_PROJECT);
        Project oldProject = ParserUtil.parseProject(arguments.getValue(PREFIX_OLD_PROJECT).get());
        Project project = ParserUtil.parseProject(arguments.getValue(PREFIX_NEW_PROJECT).get());
        return new ReplaceProjectCommand(index, oldProject, project);
    }
}
