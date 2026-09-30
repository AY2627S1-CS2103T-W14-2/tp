package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_REMARK;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;

import org.junit.jupiter.api.Test;

import seedu.address.logic.Messages;
import seedu.address.logic.commands.RemarkCommand;
import seedu.address.model.person.Remark;

public class RemarkCommandParserTest {

    private static final String MESSAGE_INVALID_FORMAT =
            String.format(MESSAGE_INVALID_COMMAND_FORMAT, RemarkCommand.MESSAGE_USAGE);

    private final RemarkCommandParser parser = new RemarkCommandParser();

    @Test
    public void parse_validArgs_returnsRemarkCommand() {
        assertParseSuccess(parser, "1 r/Likes to swim",
                new RemarkCommand(INDEX_FIRST_PERSON, new Remark("Likes to swim")));
    }

    @Test
    public void parse_emptyRemark_returnsRemarkCommand() {
        assertParseSuccess(parser, "1 r/", new RemarkCommand(INDEX_FIRST_PERSON, new Remark("")));
        assertParseSuccess(parser, "1", new RemarkCommand(INDEX_FIRST_PERSON, new Remark("")));
    }

    @Test
    public void parse_invalidIndex_throwsParseException() {
        assertParseFailure(parser, "0 r/Likes to swim", MESSAGE_INVALID_FORMAT);
        assertParseFailure(parser, "one r/Likes to swim", MESSAGE_INVALID_FORMAT);
    }

    @Test
    public void parse_duplicateRemarkPrefix_throwsParseException() {
        String userInput = "1 r/Likes to swim r/Likes baseball";
        assertParseFailure(parser, userInput, Messages.getErrorMessageForDuplicatePrefixes(PREFIX_REMARK));
    }
}
