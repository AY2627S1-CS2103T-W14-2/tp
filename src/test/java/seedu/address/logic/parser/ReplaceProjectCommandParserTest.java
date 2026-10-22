package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.ReplaceProjectCommand;
import seedu.address.model.project.Project;

public class ReplaceProjectCommandParserTest {
    private final ReplaceProjectCommandParser parser = new ReplaceProjectCommandParser();

    @Test
    public void parse_validInput_normalizesNamesAndAcceptsEitherPrefixOrder() {
        ReplaceProjectCommand expected = new ReplaceProjectCommand(Index.fromOneBased(2),
                new Project("Team Alpha"), new Project("Orbital"));
        assertParseSuccess(parser, "2 old/ Team   Alpha new/ Orbital ", expected);
        assertParseSuccess(parser, "2 new/Orbital old/Team Alpha", expected);
        assertParseSuccess(parser, "1 old/A new/" + "x".repeat(40),
                new ReplaceProjectCommand(Index.fromOneBased(1), new Project("A"), new Project("x".repeat(40))));
    }

    @Test
    public void parse_missingArgumentsOrInvalidIndex_failure() {
        String expected = String.format(MESSAGE_INVALID_COMMAND_FORMAT, ReplaceProjectCommand.MESSAGE_USAGE);
        for (String input : new String[] {"", "1", "1 old/A", "1 new/B", "old/A new/B",
            "0 old/A new/B", "-1 old/A new/B", "one old/A new/B", "1.5 old/A new/B",
            "2147483648 old/A new/B", "1 2 old/A new/B"}) {
            assertParseFailure(parser, input, expected);
        }
    }

    @Test
    public void parse_invalidNames_failure() {
        for (String name : new String[] {" ", "x".repeat(41), "Team\u0000Alpha"}) {
            assertParseFailure(parser, "1 old/" + name + " new/B", Project.MESSAGE_CONSTRAINTS);
            assertParseFailure(parser, "1 old/A new/" + name, Project.MESSAGE_CONSTRAINTS);
        }
    }

    @Test
    public void parse_duplicatePrefixes_failure() {
        assertParseFailure(parser, "1 old/A old/B new/C",
                Messages.getErrorMessageForDuplicatePrefixes(CliSyntax.PREFIX_OLD_PROJECT));
        assertParseFailure(parser, "1 old/A new/B new/C",
                Messages.getErrorMessageForDuplicatePrefixes(CliSyntax.PREFIX_NEW_PROJECT));
    }
}
