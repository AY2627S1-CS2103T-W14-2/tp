package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.person.Person;
import seedu.address.model.project.Project;
import seedu.address.testutil.PersonBuilder;

public class ReplaceProjectCommandTest {
    private final Model model = new ModelManager();

    private ReplaceProjectCommand command(int index, String oldName, String newName) {
        return new ReplaceProjectCommand(Index.fromOneBased(index), new Project(oldName), new Project(newName));
    }

    @Test
    public void execute_filteredContact_preservesFieldsOrderAndOtherContacts() throws Exception {
        Person alice = PersonBuilder.named("Alice").withProjects("Team Alpha").build();
        Person bob = PersonBuilder.named("Bob").withProjects("First", "Team Alpha", "Last").build();
        model.addPerson(alice);
        model.addPerson(bob);
        model.updateFilteredPersonList(person -> person.getName().equals(bob.getName()));
        CommandResult result = command(1, " team   ALPHA ", "Orbital").execute(model);
        assertEquals(new PersonBuilder(bob).withProjects("First", "Orbital", "Last").build(),
                model.getFilteredPersonList().get(0));
        assertEquals(1, model.getFilteredPersonList().size());
        assertEquals(alice, model.getAddressBook().getPersonList().get(0));
        assertEquals(String.format(ReplaceProjectCommand.MESSAGE_SUCCESS, "Team Alpha", "Orbital",
                Messages.format(model.getFilteredPersonList().get(0))), result.getFeedbackToUser());
    }

    @Test
    public void execute_invalidReplacements_failureWithoutMutation() {
        model.addPerson(new PersonBuilder().withProjects("Team Alpha", "Orbital").build());
        assertCommandFailure(command(1, "Missing", "New"), model, ReplaceProjectCommand.MESSAGE_PROJECT_NOT_FOUND);
        assertCommandFailure(command(1, "team alpha", " Team   Alpha "), model,
                ReplaceProjectCommand.MESSAGE_UNCHANGED_PROJECT);
        assertCommandFailure(command(1, "Team Alpha", "ORBITAL"), model,
                ReplaceProjectCommand.MESSAGE_DUPLICATE_PROJECT);
        assertCommandFailure(command(2, "Team Alpha", "New"), model, Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
        model.updateFilteredPersonList(person -> false);
        assertCommandFailure(command(1, "Team Alpha", "New"), model, Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    @Test
    public void execute_contactWithoutProjects_failure() {
        model.addPerson(new PersonBuilder().withProjects().build());
        assertCommandFailure(command(1, "Missing", "New"), model, ReplaceProjectCommand.MESSAGE_PROJECT_NOT_FOUND);
    }

    @Test
    public void constructor_nullArguments_throws() {
        Index index = Index.fromOneBased(1);
        Project project = new Project("A");
        assertThrows(NullPointerException.class, () -> new ReplaceProjectCommand(null, project, project));
        assertThrows(NullPointerException.class, () -> new ReplaceProjectCommand(index, null, project));
        assertThrows(NullPointerException.class, () -> new ReplaceProjectCommand(index, project, null));
    }

    @Test
    public void toString_includesIndexAndBothProjects() {
        ReplaceProjectCommand value = command(2, "Team Alpha", "Orbital");
        String expected = ReplaceProjectCommand.class.getCanonicalName()
                + "{index=" + Index.fromOneBased(2) + ", oldProject=Team Alpha, newProject=Orbital}";
        assertEquals(expected, value.toString());
    }

    @Test
    public void equals_comparesAllArguments() {
        ReplaceProjectCommand value = command(1, "A", "B");
        assertEquals(value, value);
        assertEquals(value, command(1, "a", "B"));
        assertNotEquals(value, command(1, "A", "b"));
        assertEquals(value.hashCode(), command(1, "a", "B").hashCode());
        assertNotEquals(value, command(2, "A", "B"));
        assertNotEquals(value, command(1, "C", "B"));
        assertNotEquals(value, command(1, "A", "C"));
        assertNotEquals(value, null);
        assertNotEquals(value, "replaceproject");
        assertTrue(value.isUndoable());
    }
    @Test
    public void execute_caseOnlyCorrection_usesStoredSpellingToDetectChanges() throws Exception {
        model.addPerson(new PersonBuilder().withProjects("First", "orbital", "Last").build());
        command(1, "ORBITAL", "Orbital").execute(model);
        assertEquals(java.util.List.of("First", "Orbital", "Last"), model.getFilteredPersonList().get(0)
                .getProjects().stream().map(value -> value.projectName).toList());
        assertCommandFailure(command(1, "orbital", "Orbital"), model,
                ReplaceProjectCommand.MESSAGE_UNCHANGED_PROJECT);
        command(1, "orbital", "orbital").execute(model);
        assertEquals("orbital", model.getFilteredPersonList().get(0).getProjects().get(1).projectName);
    }

}
