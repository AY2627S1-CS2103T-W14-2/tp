package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.ListProjectsCommand;

public class ListProjectsCommandParserTest {

    private static final String MESSAGE_INVALID_FORMAT =
            String.format(MESSAGE_INVALID_COMMAND_FORMAT, ListProjectsCommand.MESSAGE_USAGE);

    private final ListProjectsCommandParser parser = new ListProjectsCommandParser();

    @Test
    public void messageUsage_matchesAgreedText() {
        assertEquals("projects: Lists every project saved across all contacts, with the number of contacts in each.\n"
                + "This command takes no parameters.\nExample: projects", ListProjectsCommand.MESSAGE_USAGE);
    }

    @Test
    public void parse_noArguments_success() {
        assertParseSuccess(parser, "", new ListProjectsCommand());
    }

    @Test
    public void parse_whitespaceOnly_success() {
        assertParseSuccess(parser, "   ", new ListProjectsCommand());
        assertParseSuccess(parser, " \t\n ", new ListProjectsCommand());
    }

    @Test
    public void parse_anyArgument_throwsParseException() {
        assertParseFailure(parser, " 2 pr/Hackathon", MESSAGE_INVALID_FORMAT);
        assertParseFailure(parser, " abc", MESSAGE_INVALID_FORMAT);
        assertParseFailure(parser, " 0", MESSAGE_INVALID_FORMAT);
        assertParseFailure(parser, " pr/Orbital", MESSAGE_INVALID_FORMAT);
    }
}
