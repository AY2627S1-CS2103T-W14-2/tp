package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.ProjectCommand;
import seedu.address.model.project.Project;

public class ProjectCommandParserTest {
    private final ProjectCommandParser parser = new ProjectCommandParser();

    @Test
    public void parse_validInput_normalizesProject() {
        assertParseSuccess(parser, " 2 pr/ Team   Alpha ",
                new ProjectCommand(Index.fromOneBased(2), new Project("Team Alpha")));
        assertParseSuccess(parser, " 1 pr/" + "x".repeat(40),
                new ProjectCommand(Index.fromOneBased(1), new Project("x".repeat(40))));
    }

    @Test
    public void parse_missingOrInvalidIndexOrPrefix_failure() {
        String expected = String.format(MESSAGE_INVALID_COMMAND_FORMAT, ProjectCommand.MESSAGE_USAGE);
        for (String input : new String[] {"", "1", "pr/Orbital", "0 pr/Orbital", "-1 pr/Orbital",
            "one pr/Orbital", "1.5 pr/Orbital", "2147483648 pr/Orbital", "1 2 pr/Orbital",
            "pr/Orbital 1"}) {
            assertParseFailure(parser, input, expected);
        }
    }

    @Test
    public void parse_invalidProject_failure() {
        assertParseFailure(parser, "1 pr/   ", Project.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, "1 pr/" + "x".repeat(41), Project.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, "1 pr/Team\u0000Alpha", Project.MESSAGE_CONSTRAINTS);
    }

    @Test
    public void parse_repeatedProjectPrefix_failure() {
        assertParseFailure(parser, "1 pr/Orbital pr/CS2103T",
                Messages.getErrorMessageForDuplicatePrefixes(CliSyntax.PREFIX_PROJECT));
    }
}
