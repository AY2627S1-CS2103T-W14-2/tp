package seedu.address.logic.parser;

import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.ProjectParserTestUtil.assertInvalidLaterIndex;
import static seedu.address.logic.parser.ProjectParserTestUtil.assertInvalidProject;
import static seedu.address.logic.parser.ProjectParserTestUtil.assertInvalidSelection;
import static seedu.address.logic.parser.ProjectParserTestUtil.assertNormalizedProject;
import static seedu.address.logic.parser.ProjectParserTestUtil.assertRepeatedIndices;
import static seedu.address.logic.parser.ProjectParserTestUtil.assertSelectionOrder;

import org.junit.jupiter.api.Test;

import seedu.address.logic.Messages;
import seedu.address.logic.commands.ProjectCommand;

public class ProjectCommandParserTest {
    private final ProjectCommandParser parser = new ProjectCommandParser();

    @Test
    public void parse_validInput_normalizesProject() {
        assertNormalizedProject(parser, ProjectCommand::new);
    }

    @Test
    public void parse_missingOrInvalidIndexOrPrefix_failure() {
        assertInvalidSelection(parser, ProjectCommand.MESSAGE_USAGE);
    }

    @Test
    public void parse_invalidProject_failure() {
        assertInvalidProject(parser);
    }

    @Test
    public void parse_repeatedProjectPrefix_failure() {
        // EP: project prefix occurs more than once.
        assertParseFailure(parser, "1 pr/Orbital pr/CS2103T",
                Messages.getErrorMessageForDuplicatePrefixes(CliSyntax.PREFIX_PROJECT));
    }

    @Test
    public void parse_multipleIndices_acceptsWhitespaceAndSelectionOrder() {
        assertSelectionOrder(parser, ProjectCommand::new);
    }

    @Test
    public void parse_invalidLaterIndex_rejected() {
        assertInvalidLaterIndex(parser, ProjectCommand.MESSAGE_USAGE);
    }

    @Test
    public void parse_repeatedIndices_identifiesRepeatedIndex() {
        assertRepeatedIndices(parser);
    }

}
