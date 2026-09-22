package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.RemarkCommand;
import seedu.address.model.person.Remark;

public class RemarkCommandParserTest {
    private final RemarkCommandParser parser = new RemarkCommandParser();

    @Test
    public void parse_validArguments_returnsRemarkCommand() {
        assertParseSuccess(parser, "1 r/Likes swimming",
                new RemarkCommand(INDEX_FIRST_PERSON, new Remark("Likes swimming")));
        // The tutorial's tokenizer keeps the last value when a prefix is repeated.
        assertParseSuccess(parser, "1 r/old r/new",
                new RemarkCommand(INDEX_FIRST_PERSON, new Remark("new")));
    }

    @Test
    public void parse_emptyOrMissingRemark_clearsRemark() {
        RemarkCommand expected = new RemarkCommand(INDEX_FIRST_PERSON, new Remark(""));
        assertParseSuccess(parser, "1 r/", expected);
        assertParseSuccess(parser, "1", expected);
    }

    @Test
    public void parse_invalidIndex_throwsParseException() {
        String message = String.format(MESSAGE_INVALID_COMMAND_FORMAT, RemarkCommand.MESSAGE_USAGE);
        for (String input : new String[]{"", "r/note", "0 r/note", "-1 r/note", "abc r/note",
            "2147483648 r/note"}) {
            assertParseFailure(parser, input, message);
        }
    }
}
