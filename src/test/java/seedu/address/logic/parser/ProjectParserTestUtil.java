package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;

import java.util.List;
import java.util.function.BiFunction;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.Command;
import seedu.address.model.project.Project;

/**
 * Shared parser cases for commands selecting contacts and a project.
 * Command-specific prefix handling is tested separately in each parser test class.
 */
final class ProjectParserTestUtil {
    private ProjectParserTestUtil() {}

    /** Checks normalization and the maximum project length. */
    static void assertNormalizedProject(Parser<? extends Command> parser,
            BiFunction<List<Index>, Project, Command> commandFactory) {
        // EP: surrounding and repeated internal whitespace is normalised.
        assertParseSuccess(parser, " 2 pr/ Team   Alpha ",
                commandFactory.apply(List.of(Index.fromOneBased(2)), new Project("Team Alpha")));
        // BVA: a project name at the maximum length is accepted.
        String longestName = "x".repeat(Project.MAX_NAME_LENGTH);
        assertParseSuccess(parser, " 1 pr/" + longestName,
                commandFactory.apply(List.of(Index.fromOneBased(1)), new Project(longestName)));
    }

    /** Checks missing arguments and invalid first indices. */
    static void assertInvalidSelection(Parser<? extends Command> parser, String usage) {
        String expected = String.format(MESSAGE_INVALID_COMMAND_FORMAT, usage);
        // EPs: missing or misplaced arguments, non-positive indices, non-integers and integer overflow.
        for (String input : new String[] {"", "1", "pr/Orbital", "0 pr/Orbital", "-1 pr/Orbital",
            "one pr/Orbital", "1.5 pr/Orbital", "2147483648 pr/Orbital", "pr/Orbital 1"}) {
            assertParseFailure(parser, input, expected);
        }
    }

    /** Checks blank, overlong and control-character-containing project names. */
    static void assertInvalidProject(Parser<? extends Command> parser) {
        // EPs: blank and control-containing names; BVA: one character beyond the maximum length.
        for (String name : new String[] {"   ", "x".repeat(Project.MAX_NAME_LENGTH + 1), "Team\u0000Alpha"}) {
            assertParseFailure(parser, "1 pr/" + name, Project.MESSAGE_CONSTRAINTS);
        }
    }

    /** Checks whitespace-separated indices retain their supplied order. */
    static void assertSelectionOrder(Parser<? extends Command> parser,
            BiFunction<List<Index>, Project, Command> commandFactory) {
        // EP: distinct indices separated by spaces or tabs retain input order rather than numeric order.
        assertParseSuccess(parser, " 3  1\t2 pr/ Team   Alpha ",
                commandFactory.apply(List.of(Index.fromOneBased(3), Index.fromOneBased(1), Index.fromOneBased(2)),
                        new Project("Team Alpha")));
    }

    /** Checks invalid indices after the first selected contact. */
    static void assertInvalidLaterIndex(Parser<? extends Command> parser, String usage) {
        String expected = String.format(MESSAGE_INVALID_COMMAND_FORMAT, usage);
        // EP: a valid first index does not hide an invalid later index, including integer overflow.
        for (String input : new String[] {"1 0", "1 -2", "1 word", "1 2.5", "1 2147483648"}) {
            assertParseFailure(parser, input + " pr/Orbital", expected);
        }
    }

    /** Checks duplicate indices report the repeated value. */
    static void assertRepeatedIndices(Parser<? extends Command> parser) {
        // EPs: adjacent and non-adjacent repetitions identify the repeated index.
        assertParseFailure(parser, "1 1 pr/Orbital", "Contact index 1 is repeated.");
        assertParseFailure(parser, "3 2 1 2 pr/Orbital", "Contact index 2 is repeated.");
    }
}
