package seedu.address.logic.parser;

import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.logic.parser.ProjectParserTestUtil.assertInvalidLaterIndex;
import static seedu.address.logic.parser.ProjectParserTestUtil.assertInvalidProject;
import static seedu.address.logic.parser.ProjectParserTestUtil.assertInvalidSelection;
import static seedu.address.logic.parser.ProjectParserTestUtil.assertNormalizedProject;
import static seedu.address.logic.parser.ProjectParserTestUtil.assertRepeatedIndices;
import static seedu.address.logic.parser.ProjectParserTestUtil.assertSelectionOrder;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.DeleteProjectCommand;
import seedu.address.model.project.Project;

public class DeleteProjectCommandParserTest {
    private final DeleteProjectCommandParser parser = new DeleteProjectCommandParser();

    @Test
    public void parse_validInput_normalizesProject() {
        assertNormalizedProject(parser, DeleteProjectCommand::new);
    }

    @Test
    public void parse_missingOrInvalidIndexOrPrefix_failure() {
        assertInvalidSelection(parser, DeleteProjectCommand.MESSAGE_USAGE);
    }

    @Test
    public void parse_invalidProject_failure() {
        assertInvalidProject(parser);
    }

    @Test
    public void parse_projectContainsPrefix_preservesLiteralName() {
        // EP: project name contains literal pr/ text after the first delimiter.
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
        assertSelectionOrder(parser, DeleteProjectCommand::new);
    }

    @Test
    public void parse_invalidLaterIndex_rejected() {
        assertInvalidLaterIndex(parser, DeleteProjectCommand.MESSAGE_USAGE);
    }

    @Test
    public void parse_repeatedIndices_identifiesRepeatedIndex() {
        assertRepeatedIndices(parser);
    }

}
