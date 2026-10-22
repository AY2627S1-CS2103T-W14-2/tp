package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_PROJECT;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import seedu.address.commons.core.LogsCenter;
import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.DeleteProjectCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.project.Project;

/**
 * Parses displayed contact indices and one project name.
 */
public class DeleteProjectCommandParser implements Parser<DeleteProjectCommand> {
    private static final Logger logger = LogsCenter.getLogger(DeleteProjectCommandParser.class);

    @Override
    public DeleteProjectCommand parse(String args) throws ParseException {
        requireNonNull(args);
        ArgumentMultimap arguments = ArgumentTokenizer.tokenize(args, PREFIX_PROJECT);
        List<Index> indices = new ArrayList<>();
        for (String token : arguments.getPreamble().strip().split("\\s+")) {
            Index index;
            try {
                index = ParserUtil.parseIndex(token);
            } catch (ParseException e) {
                logger.fine("Project parsing rejected: invalid contact index");
                throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT,
                        DeleteProjectCommand.MESSAGE_USAGE), e);
            }
            if (indices.contains(index)) {
                logger.fine("Project parsing rejected: repeated contact index " + index.getOneBased());
                throw new ParseException(String.format(DeleteProjectCommand.MESSAGE_DUPLICATE_INDEX,
                        index.getOneBased()));
            }
            indices.add(index);
        }
        if (arguments.getValue(PREFIX_PROJECT).isEmpty()) {
            logger.fine("Project parsing rejected: missing project prefix");
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT,
                    DeleteProjectCommand.MESSAGE_USAGE));
        }
        try {
            arguments.verifyNoDuplicatePrefixesFor(PREFIX_PROJECT);
            Project project = ParserUtil.parseProject(arguments.getValue(PREFIX_PROJECT).get());
            return new DeleteProjectCommand(indices, project);
        } catch (ParseException e) {
            logger.fine("Project parsing rejected: invalid project or repeated prefix");
            throw e;
        }
    }
}
