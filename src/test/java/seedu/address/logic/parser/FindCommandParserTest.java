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
        // EP: whitespace-only keywords.
        assertParseFailure(parser, "     ", String.format(MESSAGE_INVALID_COMMAND_FORMAT, FindCommand.MESSAGE_USAGE));
        assertParseFailure(parser, "\u00a0\u2003\t", String.format(MESSAGE_INVALID_COMMAND_FORMAT,
                FindCommand.MESSAGE_USAGE));
    }

    @Test
    public void parse_validArgs_returnsFindCommand() {
        // EP: no leading and trailing whitespaces
        FindCommand expectedFindCommand =
                new FindCommand(new NameContainsKeywordsPredicate(List.of("Alice", "Bob")));
        assertParseSuccess(parser, "Alice Bob", expectedFindCommand);

        // EP: multiple whitespaces between keywords
        assertParseSuccess(parser, " \n Alice \n \t Bob  \t", expectedFindCommand);
        assertParseSuccess(parser, "\u00a0Alice\u2003Bob\u00a0", expectedFindCommand);
    }

    @Test
    public void parse_slashInKeyword_throwsHelpfulParseException() {
        // EP: keyword contains a slash.
        for (String input : List.of("n/alex", "n/ alex", "alex n/bob", "alex/bob", "alex/", "/",
                "\u00a0n/alex\u2003")) {
            assertParseFailure(parser, input, FindCommand.MESSAGE_SLASH_IN_KEYWORD);
        }
    }

    @Test
    public void parse_validNamePunctuation_returnsFindCommand() {
        FindCommand expected = new FindCommand(
                new NameContainsKeywordsPredicate(List.of("O'Neil", "Anne-Marie", "Jr.", "123")));
        assertParseSuccess(parser, "O'Neil Anne-Marie Jr. 123", expected);
    }

    @Test
    public void parse_maxLengthInput_returnsFindCommand() {
        // Boundary value: maximum trimmed length of 60 code points.
        String keyword = "a".repeat(60);
        FindCommand expected = new FindCommand(new NameContainsKeywordsPredicate(List.of(keyword)));
        assertParseSuccess(parser, keyword, expected);
        assertParseSuccess(parser, "\u00a0  " + keyword + " \t\u2003", expected);
    }

    @Test
    public void parse_overlongInput_throwsParseException() {
        // Boundary value: 61 code points, one above the maximum.
        assertParseFailure(parser, "a".repeat(61), FindCommand.MESSAGE_KEYWORD_TOO_LONG);
        assertParseFailure(parser, "  " + "a".repeat(61) + "  ", FindCommand.MESSAGE_KEYWORD_TOO_LONG);
    }

    @Test
    public void parse_multipleKeywords_countsInternalWhitespaceTowardsLimit() {
        // Boundary values: 60 and 61 code points, including internal spaces.
        String first = "a".repeat(29);
        String second = "b".repeat(30);
        FindCommand expected = new FindCommand(new NameContainsKeywordsPredicate(List.of(first, second)));
        assertParseSuccess(parser, first + " " + second, expected);
        assertParseFailure(parser, first + "  " + second, FindCommand.MESSAGE_KEYWORD_TOO_LONG);
    }

    @Test
    public void parse_supplementaryCharacters_countsUnicodeCodePoints() {
        // Boundary values: 60 and 61 supplementary code points.
        String character = new String(Character.toChars(0x10400));
        String keyword = character.repeat(60);
        FindCommand expected = new FindCommand(new NameContainsKeywordsPredicate(List.of(keyword)));
        assertParseSuccess(parser, keyword, expected);
        assertParseFailure(parser, keyword + character, FindCommand.MESSAGE_KEYWORD_TOO_LONG);
    }

}
