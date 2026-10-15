package seedu.address.testutil;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.AddCommand;
import seedu.address.logic.parser.AddressBookParser;
import seedu.address.model.person.Person;

public class PersonUtilTest {
    @Test
    public void getAddCommand_oneProject_roundTripsThroughParser() throws Exception {
        Person person = new PersonBuilder().withProjects("CS2103T").build();

        String command = PersonUtil.getAddCommand(person);

        assertEquals("add n/Amy Bee p/+6585355255 e/amy@gmail.com tele/amy_bee pr/CS2103T ", command);
        assertEquals(new AddCommand(person), new AddressBookParser().parseCommand(command));
    }

    @Test
    public void getAddCommand_noProject_rejectsLegacyFixture() {
        Person legacyPerson = new PersonBuilder().build();

        assertThrows(IllegalArgumentException.class, "The add command requires exactly one project", () ->
                PersonUtil.getAddCommand(legacyPerson));
    }

    @Test
    public void getAddCommand_multipleProjects_rejectsUnrepresentableFixture() {
        Person person = new PersonBuilder().withProjects("CS2103T", "Orbital").build();

        assertThrows(IllegalArgumentException.class, "The add command requires exactly one project", () ->
                PersonUtil.getAddCommand(person));
    }
}
