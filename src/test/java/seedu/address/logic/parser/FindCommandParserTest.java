package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.FindCommand;
import seedu.address.model.person.NameContainsKeywordsPredicate;

public class FindCommandParserTest {

    private FindCommandParser parser = new FindCommandParser();

    @Test
    public void parse_emptyArg_throwsParseException() {
        assertParseFailure(parser, "     ", String.format(MESSAGE_INVALID_COMMAND_FORMAT, FindCommand.MESSAGE_USAGE));
        assertParseFailure(parser, "\u00a0\u2003\t", String.format(MESSAGE_INVALID_COMMAND_FORMAT,
                FindCommand.MESSAGE_USAGE));
    }

    @Test
    public void parse_validArgs_returnsFindCommand() {
        // no leading and trailing whitespaces
        FindCommand expectedFindCommand =
                new FindCommand(new NameContainsKeywordsPredicate(List.of("Alice", "Bob")));
        assertParseSuccess(parser, "Alice Bob", expectedFindCommand);

        // multiple whitespaces between keywords
        assertParseSuccess(parser, " \n Alice \n \t Bob  \t", expectedFindCommand);
        assertParseSuccess(parser, "\u00a0Alice\u2003Bob\u00a0", expectedFindCommand);
    }

    @Test
    public void parse_maxLengthInput_returnsFindCommand() {
        String keyword = "a".repeat(60);
        FindCommand expected = new FindCommand(new NameContainsKeywordsPredicate(List.of(keyword)));
        assertParseSuccess(parser, keyword, expected);
        assertParseSuccess(parser, "\u00a0  " + keyword + " \t\u2003", expected);
    }

    @Test
    public void parse_overlongInput_throwsParseException() {
        assertParseFailure(parser, "a".repeat(61), FindCommand.MESSAGE_KEYWORD_TOO_LONG);
        assertParseFailure(parser, "  " + "a".repeat(61) + "  ", FindCommand.MESSAGE_KEYWORD_TOO_LONG);
    }

    @Test
    public void parse_multipleKeywords_countsInternalWhitespaceTowardsLimit() {
        String first = "a".repeat(29);
        String second = "b".repeat(30);
        FindCommand expected = new FindCommand(new NameContainsKeywordsPredicate(List.of(first, second)));
        assertParseSuccess(parser, first + " " + second, expected);
        assertParseFailure(parser, first + "  " + second, FindCommand.MESSAGE_KEYWORD_TOO_LONG);
    }

    @Test
    public void parse_supplementaryCharacters_countsUnicodeCodePoints() {
        String character = new String(Character.toChars(0x10400));
        String keyword = character.repeat(60);
        FindCommand expected = new FindCommand(new NameContainsKeywordsPredicate(List.of(keyword)));
        assertParseSuccess(parser, keyword, expected);
        assertParseFailure(parser, keyword + character, FindCommand.MESSAGE_KEYWORD_TOO_LONG);
    }

}
