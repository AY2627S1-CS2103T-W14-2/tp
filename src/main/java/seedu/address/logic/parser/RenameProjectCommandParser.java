package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;
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
        if (!arguments.getPreamble().isEmpty()) {
            throw new ParseException("renameproject renames a project for all contacts and does not take an index. "
                    + "Remove the text before old/ and new/: " + arguments.getPreamble());
        }
        arguments.verifyNoDuplicatePrefixesFor(PREFIX_OLD_PROJECT, PREFIX_NEW_PROJECT);
        Project oldProject = parseProjectName(arguments, PREFIX_OLD_PROJECT, "old");
        Project newProject = parseProjectName(arguments, PREFIX_NEW_PROJECT, "new");
        return new RenameProjectCommand(oldProject, newProject);
    }

    /** Identifies which required project field is missing, empty or invalid. */
    private Project parseProjectName(ArgumentMultimap arguments, Prefix prefix, String field) throws ParseException {
        String value = arguments.getValue(prefix).orElseThrow(() -> new ParseException(
                "The " + field + " project name (" + prefix + ") is required. Use " + prefix + "PROJECT."));
        if (Project.normalize(value).isEmpty()) {
            throw new ParseException("The " + field + " project name (" + prefix + ") is empty. "
                    + "Enter a project name after " + prefix + ".");
        }
        try {
            return ParserUtil.parseProject(value);
        } catch (ParseException e) {
            throw new ParseException("Invalid " + field + " project name (" + prefix + "): " + e.getMessage(), e);
        }
    }
}
