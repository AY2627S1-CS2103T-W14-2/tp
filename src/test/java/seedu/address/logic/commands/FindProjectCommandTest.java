package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.person.Person;
import seedu.address.model.person.ProjectContainsKeywordPredicate;
import seedu.address.testutil.PersonBuilder;

public class FindProjectCommandTest {

    @Test
    public void constructor_nullPredicate_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new FindProjectCommand(null));
    }

    @Test
    public void execute_nullModel_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> command("cs2103").execute(null));
    }

    @Test
    public void execute_matchingProjects_listsEachContactOnceInOriginalOrder() {
        Person alice = new PersonBuilder().withName("Alice").withProjects("CS2103T", "CS2103T Team Alpha").build();
        Person bob = new PersonBuilder().withName("Bob").withProjects("Orbital", "CS2103T Team Beta").build();
        Person carl = new PersonBuilder().withName("Carl").withProjects("Orbital").build();
        Model model = new ModelManager();
        model.addPerson(alice);
        model.addPerson(bob);
        model.addPerson(carl);
        AddressBook original = new AddressBook(model.getAddressBook());

        CommandResult result = command("cs2103").execute(model);

        assertEquals(new CommandResult("Found 2 contact(s) matching the project keyword."), result);
        assertEquals(List.of(alice, bob), model.getFilteredPersonList());
        assertEquals(original, model.getAddressBook());
    }

    @Test
    public void execute_noMatches_clearsDisplayedListWithoutChangingContacts() {
        Person person = new PersonBuilder().withProjects("Orbital").build();
        Model model = new ModelManager();
        model.addPerson(person);

        CommandResult result = command("cs2103").execute(model);

        assertEquals(new CommandResult("No contacts found for the project keyword."), result);
        assertTrue(model.getFilteredPersonList().isEmpty());
        assertEquals(List.of(person), model.getAddressBook().getPersonList());
    }

    @Test
    public void execute_emptyAddressBook_showsNoContactsMessage() {
        Model model = new ModelManager();
        assertEquals(new CommandResult("No contacts found for the project keyword."), command("cs2103").execute(model));
        assertTrue(model.getFilteredPersonList().isEmpty());
    }

    @Test
    public void equals() {
        FindProjectCommand command = command("team alpha");
        assertTrue(command.equals(command));
        assertEquals(command, command("TEAM  ALPHA"));
        assertEquals(command.hashCode(), command("TEAM  ALPHA").hashCode());
        assertFalse(command.equals(command("orbital")));
        assertFalse(command.equals(new ListCommand()));
        assertFalse(command.equals(null));
    }

    @Test
    public void toStringMethod() {
        ProjectContainsKeywordPredicate predicate = new ProjectContainsKeywordPredicate("cs2103");
        assertEquals(FindProjectCommand.class.getCanonicalName() + "{predicate=" + predicate + "}",
                new FindProjectCommand(predicate).toString());
    }

    private FindProjectCommand command(String keyword) {
        return new FindProjectCommand(new ProjectContainsKeywordPredicate(keyword));
    }
}
