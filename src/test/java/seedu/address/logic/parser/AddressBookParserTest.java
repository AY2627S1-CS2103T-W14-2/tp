package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.Messages.MESSAGE_UNKNOWN_COMMAND;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;

import java.time.Duration;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.AddCommand;
import seedu.address.logic.commands.ClearCommand;
import seedu.address.logic.commands.DeleteCommand;
import seedu.address.logic.commands.EditCommand;
import seedu.address.logic.commands.EditCommand.EditPersonDescriptor;
import seedu.address.logic.commands.ExitCommand;
import seedu.address.logic.commands.FindCommand;
import seedu.address.logic.commands.FindProjectCommand;
import seedu.address.logic.commands.HelpCommand;
import seedu.address.logic.commands.ListCommand;
import seedu.address.logic.commands.ListProjectsCommand;
import seedu.address.logic.commands.SortCommand;
import seedu.address.logic.commands.UndoCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.NameContainsKeywordsPredicate;
import seedu.address.model.person.Person;
import seedu.address.model.person.ProjectContainsKeywordPredicate;
import seedu.address.testutil.EditPersonDescriptorBuilder;
import seedu.address.testutil.PersonBuilder;
import seedu.address.testutil.PersonUtil;

public class AddressBookParserTest {

    private final AddressBookParser parser = new AddressBookParser();

    @Test
    public void parseCommand_atLengthLimit_acceptsCommand() throws Exception {
        String input = "findproject a";
        input += " ".repeat(AddressBookParser.MAX_COMMAND_LENGTH - input.length());
        assertEquals(new FindProjectCommand(new ProjectContainsKeywordPredicate("a")), parser.parseCommand(input));
    }

    @Test
    public void parseCommand_overLengthLimit_rejectsBeforeTrimmingOrDispatch() {
        for (String input : List.of("help" + " ".repeat(AddressBookParser.MAX_COMMAND_LENGTH - 3),
                " ".repeat(AddressBookParser.MAX_COMMAND_LENGTH - 3) + "help",
                "x".repeat(AddressBookParser.MAX_COMMAND_LENGTH + 1),
                " ".repeat(AddressBookParser.MAX_COMMAND_LENGTH + 1))) {
            assertThrows(ParseException.class, AddressBookParser.MESSAGE_COMMAND_TOO_LONG, () -> {
                parser.parseCommand(input);
            });
        }
    }

    @Test
    public void parseCommand_supplementaryUnicode_countsUtf16CodeUnits() throws Exception {
        String input = "help " + "\uD83D\uDE00".repeat((AddressBookParser.MAX_COMMAND_LENGTH - 6) / 2) + "x";
        assertTrue(parser.parseCommand(input) instanceof HelpCommand);
        assertThrows(ParseException.class, AddressBookParser.MESSAGE_COMMAND_TOO_LONG, () -> {
            parser.parseCommand(input + "x");
        });
    }

    @Test
    public void parseCommand_largeWhitespaceSearch_rejectsWithinTwoSeconds() {
        String input = "findproject a" + " ".repeat(40_000) + "b";
        assertTimeoutPreemptively(Duration.ofSeconds(2), () -> {
            assertThrows(ParseException.class, AddressBookParser.MESSAGE_COMMAND_TOO_LONG, () -> {
                parser.parseCommand(input);
            });
        });
    }

    @Test
    public void parseCommand_add() throws Exception {
        Person person = new PersonBuilder().withProjects("CS2103T").build();
        AddCommand command = (AddCommand) parser.parseCommand(PersonUtil.getAddCommand(person));
        assertEquals(new AddCommand(person), command);
    }

    @Test
    public void parseCommand_clear() throws Exception {
        assertTrue(parser.parseCommand(ClearCommand.COMMAND_WORD) instanceof ClearCommand);
        assertTrue(parser.parseCommand("  " + ClearCommand.COMMAND_WORD + "  ") instanceof ClearCommand);
        assertThrows(ParseException.class,
                String.format(MESSAGE_INVALID_COMMAND_FORMAT, ClearCommand.MESSAGE_USAGE), () ->
                        parser.parseCommand(ClearCommand.COMMAND_WORD + " 5"));
        assertThrows(ParseException.class,
                String.format(MESSAGE_INVALID_COMMAND_FORMAT, ClearCommand.MESSAGE_USAGE), () ->
                        parser.parseCommand("CLEAR 5"));
    }

    @Test
    public void parseCommand_delete() throws Exception {
        DeleteCommand command = (DeleteCommand) parser.parseCommand(
                DeleteCommand.COMMAND_WORD + " " + INDEX_FIRST_PERSON.getOneBased());
        assertEquals(new DeleteCommand(INDEX_FIRST_PERSON), command);
    }

    @Test
    public void parseCommand_edit() throws Exception {
        Person person = new PersonBuilder().build();
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder(person).build();
        assertTrue(descriptor.getTelegramUsername().isPresent()); // all four editable fields are routed
        EditCommand command = (EditCommand) parser.parseCommand(EditCommand.COMMAND_WORD + " "
                + INDEX_FIRST_PERSON.getOneBased() + " " + PersonUtil.getEditPersonDescriptorDetails(descriptor));
        assertEquals(new EditCommand(INDEX_FIRST_PERSON, descriptor), command);
    }

    @Test
    public void parseCommand_editWithProject_throwsParseException() {
        for (String userInput : List.of("edit 1 n/Alice pr/Orbital", "edit pr/Orbital")) {
            assertThrows(ParseException.class, EditCommand.MESSAGE_PROJECT_NOT_EDITABLE, () ->
                    parser.parseCommand(userInput));
        }
    }

    @Test
    public void parseCommand_exit() throws Exception {
        assertTrue(parser.parseCommand(ExitCommand.COMMAND_WORD) instanceof ExitCommand);
        assertTrue(parser.parseCommand(ExitCommand.COMMAND_WORD + " 3") instanceof ExitCommand);
    }

    @Test
    public void parseCommand_find() throws Exception {
        List<String> keywords = List.of("foo", "bar", "baz");
        FindCommand command = (FindCommand) parser.parseCommand(
                FindCommand.COMMAND_WORD + " " + keywords.stream().collect(Collectors.joining(" ")));
        assertEquals(new FindCommand(new NameContainsKeywordsPredicate(keywords)), command);
    }

    @Test
    public void parseCommand_help() throws Exception {
        assertTrue(parser.parseCommand(HelpCommand.COMMAND_WORD) instanceof HelpCommand);
        assertTrue(parser.parseCommand(HelpCommand.COMMAND_WORD + " 3") instanceof HelpCommand);
    }

    @Test
    public void parseCommand_findProject() throws Exception {
        assertEquals(new FindProjectCommand(new ProjectContainsKeywordPredicate("team alpha")),
                parser.parseCommand("findproject team alpha"));
    }

    @Test
    public void parseCommand_list() throws Exception {
        assertTrue(parser.parseCommand(ListCommand.COMMAND_WORD) instanceof ListCommand);
        assertTrue(parser.parseCommand(ListCommand.COMMAND_WORD + " 3") instanceof ListCommand);
    }

    @Test
    public void parseCommand_sort() throws Exception {
        assertEquals(new SortCommand(), parser.parseCommand(SortCommand.COMMAND_WORD));
        assertEquals(new SortCommand(), parser.parseCommand(SortCommand.COMMAND_WORD + " 3"));
    }

    @Test
    public void parseCommand_projects() throws Exception {
        assertEquals(new ListProjectsCommand(), parser.parseCommand(ListProjectsCommand.COMMAND_WORD));
        assertEquals(new ListProjectsCommand(), parser.parseCommand("  " + ListProjectsCommand.COMMAND_WORD + "  "));
        assertEquals(new ListProjectsCommand(), parser.parseCommand(ListProjectsCommand.COMMAND_WORD + "\t"));
        assertThrows(ParseException.class, "Invalid command format!\n" + ListProjectsCommand.MESSAGE_USAGE, ()
            -> parser.parseCommand(ListProjectsCommand.COMMAND_WORD + " 0"));
        assertThrows(ParseException.class,
                String.format(MESSAGE_INVALID_COMMAND_FORMAT, ListProjectsCommand.MESSAGE_USAGE), ()
                    -> parser.parseCommand(ListProjectsCommand.COMMAND_WORD + " 2 pr/Hackathon"));
        assertEquals(new ListProjectsCommand(), parser.parseCommand("PROJECTS"));
    }

    @Test
    public void parseCommand_commandWordInAnyCase_sameAsLowercase() throws Exception {
        // issue #171
        assertEquals(new FindCommand(new NameContainsKeywordsPredicate(List.of("alex"))),
                parser.parseCommand("FIND alex"));
        assertEquals(new FindCommand(new NameContainsKeywordsPredicate(List.of("alex"))),
                parser.parseCommand("Find alex"));
        assertEquals(new FindProjectCommand(new ProjectContainsKeywordPredicate("team alpha")),
                parser.parseCommand("FindProject team alpha"));
        assertEquals(new DeleteCommand(INDEX_FIRST_PERSON), parser.parseCommand("DELETE 1"));
        assertEquals(new SortCommand(), parser.parseCommand("SORT"));
        assertTrue(parser.parseCommand("LIST") instanceof ListCommand);
        assertTrue(parser.parseCommand("Undo") instanceof UndoCommand);
        assertTrue(parser.parseCommand("hElP") instanceof HelpCommand);
        assertTrue(parser.parseCommand("EXIT") instanceof ExitCommand);
    }

    @Test
    public void parseCommand_commandWordInAnyCase_onlyCommandWordIgnoresCase() throws Exception {
        // the keyword keeps its case
        assertEquals(new FindCommand(new NameContainsKeywordsPredicate(List.of("ALEX"))),
                parser.parseCommand("FIND ALEX"));
        // prefixes stay case-sensitive
        assertThrows(ParseException.class, String.format(MESSAGE_INVALID_COMMAND_FORMAT, EditCommand.MESSAGE_USAGE), ()
            -> parser.parseCommand("EDIT 1 N/Alex"));
        // command words still cannot be shortened
        assertThrows(ParseException.class, MESSAGE_UNKNOWN_COMMAND, () -> parser.parseCommand("FINDP alex"));
        assertThrows(ParseException.class, MESSAGE_UNKNOWN_COMMAND, () -> parser.parseCommand("UNKNOWNCOMMAND"));
    }

    @Test
    public void parseCommand_commandWordInTurkishLocale_sameAsLowercase() throws Exception {
        // In Turkish, "I".toLowerCase() is a dotless "ı", so "LIST" would not match without Locale.ROOT
        Locale originalLocale = Locale.getDefault();
        try {
            Locale.setDefault(Locale.forLanguageTag("tr"));
            assertTrue(parser.parseCommand("LIST") instanceof ListCommand);
            assertTrue(parser.parseCommand("FIND alex") instanceof FindCommand);
        } finally {
            Locale.setDefault(originalLocale);
        }
    }

    @Test
    public void parseCommand_undo() throws Exception {
        assertTrue(parser.parseCommand(UndoCommand.COMMAND_WORD) instanceof UndoCommand);
    }

    @Test
    public void parseCommand_unrecognisedInput_throwsParseException() {
        assertThrows(ParseException.class, String.format(MESSAGE_INVALID_COMMAND_FORMAT, HelpCommand.MESSAGE_USAGE), ()
            -> parser.parseCommand(""));
    }

    @Test
    public void parseCommand_unknownCommand_throwsParseException() {
        assertThrows(ParseException.class, MESSAGE_UNKNOWN_COMMAND, () -> parser.parseCommand("unknownCommand"));
    }
}
