package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.Command;
import seedu.address.logic.commands.RemarkCommand;
import seedu.address.model.ModelManager;
import seedu.address.model.person.Remark;
import seedu.address.testutil.PersonBuilder;

public class RemarkCommandParserTest {

    private final RemarkCommandParser parser = new RemarkCommandParser();

    @Test
    public void parse_validArgs_returnsRemarkCommand() {
        assertParseSuccess(parser, " 1 r/Likes swimming",
                new RemarkCommand(INDEX_FIRST_PERSON, new Remark("Likes swimming")));
    }

    @Test
    public void parse_emptyOrMissingRemark_returnsEmptyRemark() {
        assertParseSuccess(parser, "1 r/", new RemarkCommand(INDEX_FIRST_PERSON, new Remark("")));
        assertParseSuccess(parser, "1", new RemarkCommand(INDEX_FIRST_PERSON, new Remark("")));
    }

    @Test
    public void parse_invalidIndex_throwsParseException() {
        String expectedMessage = String.format(MESSAGE_INVALID_COMMAND_FORMAT, RemarkCommand.MESSAGE_USAGE);
        for (String args : new String[] {"", "r/note", "0 r/note", "-1 r/note", "abc r/note"}) {
            assertParseFailure(parser, args, expectedMessage);
        }
    }

    @Test
    public void parseCommand_execute_updatesRemark() throws Exception {
        Command command = new AddressBookParser().parseCommand("remark 1 r/Likes swimming");
        ModelManager model = new ModelManager();
        model.addPerson(new PersonBuilder().build());
        command.execute(model);
        assertEquals("Likes swimming", model.getFilteredPersonList().getFirst().getRemark().value);
    }
}
