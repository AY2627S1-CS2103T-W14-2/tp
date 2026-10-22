package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.RenameProjectCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.project.Project;

public class RenameProjectCommandParserTest {
    private final RenameProjectCommandParser parser = new RenameProjectCommandParser();

    @Test
    public void parse_validNames_normalisesWhitespaceAndAcceptsEitherOrder() throws Exception {
        RenameProjectCommand expected = command("Team Alpha", "Orbital 2026");
        assertEquals(expected, parser.parse(" old/ Team   Alpha new/ Orbital  2026 "));
        assertEquals(expected, parser.parse(" new/Orbital 2026 old/team alpha"));
        assertEquals(expected, parser.parse(" old/Team\u00a0Alpha new/Orbital 2026"));
        assertEquals(expected, new AddressBookParser().parseCommand(
                "renameproject old/Team Alpha new/Orbital 2026"));
    }

    @Test
    public void parse_quotesAndBackslashes_areLiteralCharacters() throws Exception {
        for (String name : List.of("\"CS2103T\"", "\"Quoted\"", "\"", "\"\"", "\"  \"", "\"Unclosed", "Trailing\"",
                "\"A\"extra", "Team \"Blue\"", "Team\\Alpha", "\"A\\q\"", "old/", "new/", "😀 Team")) {
            assertEquals(command(name, "Destination"), parser.parse(" old/" + name + " new/Destination"), name);
            assertEquals(command("Source", name), parser.parse(" new/" + name + " old/Source"), name);
        }
    }

    @Test
    public void parse_invalidSyntax_rejectsWithoutGuessing() {
        for (String input : List.of("", " old/A", " new/B", " preamble old/A new/B",
                " old/A old/B new/C", " old/A new/B new/C", " old/ new/B", " old/A new/",
                " old/\"A\"new/B", " OLD/A new/B",
                " old/\"Team new/Alpha\" new/B", " old/A new/\"Team old/Beta\"")) {
            assertThrows(ParseException.class, () -> parser.parse(input), input);
        }
    }

    @Test
    public void parse_lengthBoundary_countsLiteralQuotesAndUnicodeCodePoints() throws Exception {
        String limit = "😀".repeat(40);
        String quotedLimit = "\"" + "😀".repeat(38) + "\"";
        assertEquals(command(limit, "B"), parser.parse(" old/" + limit + " new/B"));
        assertEquals(command("A", limit), parser.parse(" old/A new/" + limit));
        assertEquals(command(quotedLimit, "B"), parser.parse(" old/" + quotedLimit + " new/B"));
        assertEquals(command("A", quotedLimit), parser.parse(" old/A new/" + quotedLimit));
        assertThrows(ParseException.class, () -> parser.parse(" old/" + limit + "x new/B"));
        assertThrows(ParseException.class, () -> parser.parse(" old/A new/" + limit + "x"));
        assertThrows(ParseException.class, () -> parser.parse(" old/\"" + limit + "\" new/B"));
        assertThrows(ParseException.class, () -> parser.parse(" old/A new/\"" + limit + "\""));
        assertThrows(ParseException.class, () -> parser.parse(" old/A new/B\u0000C"));
    }

    private RenameProjectCommand command(String oldName, String newName) {
        return new RenameProjectCommand(new Project(oldName), new Project(newName));
    }
}
