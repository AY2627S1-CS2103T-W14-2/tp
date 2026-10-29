package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.DeleteProjectCommand;
import seedu.address.model.project.Project;

public class DeleteProjectCommandParserTest {
    private final DeleteProjectCommandParser parser = new DeleteProjectCommandParser();

    @Test
    public void parse_validInput_normalizesProject() {
        assertParseSuccess(parser, " 2 pr/ Team   Alpha ",
                new DeleteProjectCommand(Index.fromOneBased(2), new Project("Team Alpha")));
        assertParseSuccess(parser, " 1 pr/" + "x".repeat(40),
                new DeleteProjectCommand(Index.fromOneBased(1), new Project("x".repeat(40))));
    }

    @Test
    public void parse_missingOrInvalidIndexOrPrefix_failure() {
        String expected = String.format(MESSAGE_INVALID_COMMAND_FORMAT, DeleteProjectCommand.MESSAGE_USAGE);
        for (String input : new String[] {"", "1", "pr/Orbital", "0 pr/Orbital", "-1 pr/Orbital",
            "one pr/Orbital", "1.5 pr/Orbital", "2147483648 pr/Orbital",
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
    public void parse_projectContainsPrefix_preservesLiteralName() {
        for (String name : new String[] {"Team pr/X", "Orbital pr/CS2103T", "pr/X", "Team pr/", "A pr/B pr/C"}) {
            assertParseSuccess(parser, "1 pr/" + name,
                    new DeleteProjectCommand(Index.fromOneBased(1), new Project(name)));
        }
        assertParseSuccess(parser, " 2 1 pr/ Team   pr/X ",
                new DeleteProjectCommand(List.of(Index.fromOneBased(2), Index.fromOneBased(1)),
                        new Project("Team pr/X")));
    }
    @Test
    public void parse_multipleIndices_acceptsWhitespaceAndSelectionOrder() {
        assertParseSuccess(parser, " 3  1\t2 pr/ Team   Alpha ",
                new DeleteProjectCommand(List.of(Index.fromOneBased(3), Index.fromOneBased(1), Index.fromOneBased(2)),
                        new Project("Team Alpha")));
    }

    @Test
    public void parse_invalidLaterIndex_rejected() {
        String expected = String.format(MESSAGE_INVALID_COMMAND_FORMAT, DeleteProjectCommand.MESSAGE_USAGE);
        for (String input : new String[] {"1 0", "1 -2", "1 word", "1 2.5", "1 2147483648"}) {
            assertParseFailure(parser, input + " pr/Orbital", expected);
        }
    }

    @Test
    public void parse_repeatedIndices_identifiesRepeatedIndex() {
        assertParseFailure(parser, "1 1 pr/Orbital", "Contact index 1 is repeated.");
        assertParseFailure(parser, "3 2 1 2 pr/Orbital", "Contact index 2 is repeated.");
    }

}
