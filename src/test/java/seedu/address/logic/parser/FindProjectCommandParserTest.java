package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.FindProjectCommand;
import seedu.address.model.person.ProjectContainsKeywordPredicate;

public class FindProjectCommandParserTest {
    private final FindProjectCommandParser parser = new FindProjectCommandParser();

    @Test
    public void parse_nullArgs_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> parser.parse(null));
    }

    @Test
    public void parse_blankArgs_throwsParseException() {
        String expectedMessage = String.format(MESSAGE_INVALID_COMMAND_FORMAT, FindProjectCommand.MESSAGE_USAGE);
        assertParseFailure(parser, "", expectedMessage);
        assertParseFailure(parser, " \t\n\u00a0\u2003 ", expectedMessage);
    }

    @Test
    public void parse_javaWhitespaceOnly_throwsParseException() {
        String expectedMessage = String.format(MESSAGE_INVALID_COMMAND_FORMAT, FindProjectCommand.MESSAGE_USAGE);
        for (int codePoint = 0x1c; codePoint <= 0x1f; codePoint++) {
            assertParseFailure(parser, "\u00a0" + Character.toString(codePoint) + "\u00a0", expectedMessage);
        }
    }

    @Test
    public void parse_javaWhitespaceAroundLimitLengthKeyword_ignoresSurroundingWhitespace() {
        String keyword = "a".repeat(40);
        String separator = Character.toString(0x1c);
        assertParseSuccess(parser, "\u00a0" + separator + keyword + separator + "\u00a0",
                new FindProjectCommand(new ProjectContainsKeywordPredicate(keyword)));
    }

    @Test
    public void parse_phrase_preservesWholePhrase() {
        FindProjectCommand expected = new FindProjectCommand(new ProjectContainsKeywordPredicate("team alpha"));
        assertParseSuccess(parser, "team alpha", expected);
        assertParseSuccess(parser, " \u00a0 TEAM \t  Alpha \u2003 ", expected);
    }

    @Test
    public void parse_lengthBoundary_checksTrimmedLength() {
        String keyword = "a".repeat(40);
        FindProjectCommand expected = new FindProjectCommand(new ProjectContainsKeywordPredicate(keyword));
        assertParseSuccess(parser, " \u00a0" + keyword + "\u00a0 ", expected);
        assertParseSuccess(parser, "a", new FindProjectCommand(new ProjectContainsKeywordPredicate("a")));
        assertParseFailure(parser, keyword + "a", FindProjectCommand.MESSAGE_KEYWORD_TOO_LONG);
    }

    @Test
    public void parse_repeatedInternalWhitespace_countsBeforeCollapsing() {
        String keyword = "a" + " ".repeat(39) + "b";
        assertParseFailure(parser, keyword, FindProjectCommand.MESSAGE_KEYWORD_TOO_LONG);
    }

    @Test
    public void parse_supplementaryCharacters_countsCodePoints() {
        String keyword = "\uD83D\uDE80".repeat(40);
        assertParseSuccess(parser, keyword,
                new FindProjectCommand(new ProjectContainsKeywordPredicate(keyword)));
        assertParseFailure(parser, keyword + "\uD83D\uDE80", FindProjectCommand.MESSAGE_KEYWORD_TOO_LONG);
    }

    @Test
    public void parse_punctuation_treatsInputLiterally() {
        String keyword = "C++ [Team]/Alpha";
        assertParseSuccess(parser, keyword,
                new FindProjectCommand(new ProjectContainsKeywordPredicate(keyword)));
    }
}
