package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.person.Person;
import seedu.address.model.project.Project;
import seedu.address.testutil.PersonBuilder;

public class ProjectCommandTest {
    private final Model model = new ModelManager();
    private final Project project = new Project("Orbital");

    @Test
    public void execute_filteredContact_preservesFieldsOrderAndFilter() throws Exception {
        Person first = new PersonBuilder().withName("Alice").build();
        Person target = new PersonBuilder().withName("Bob").withProjects("CS2103T", "CS2101").build();
        model.addPerson(first);
        model.addPerson(target);
        model.updateFilteredPersonList(person -> person.getName().equals(target.getName()));

        CommandResult result = new ProjectCommand(Index.fromOneBased(1), project).execute(model);

        Person expected = new PersonBuilder(target).withProjects("CS2103T", "CS2101", "Orbital").build();
        assertEquals(expected, model.getFilteredPersonList().get(0));
        assertEquals(1, model.getFilteredPersonList().size());
        assertEquals(first, model.getAddressBook().getPersonList().get(0));
        assertEquals(String.format(ProjectCommand.MESSAGE_SUCCESS, project, target.getName()),
                result.getFeedbackToUser());
    }

    @Test
    public void execute_noExistingProjects_success() throws Exception {
        Person original = new PersonBuilder().withProjects().build();
        model.addPerson(original);
        new ProjectCommand(Index.fromOneBased(1), project).execute(model);
        assertEquals(new PersonBuilder(original).withProjects("Orbital").build(),
                model.getFilteredPersonList().get(0));
    }

    @Test
    public void execute_duplicateNormalizedProject_failureWithoutMutation() {
        model.addPerson(new PersonBuilder().withProjects("Team Alpha").build());
        assertCommandFailure(new ProjectCommand(Index.fromOneBased(1), new Project(" team   ALPHA ")),
                model, ProjectCommand.MESSAGE_DUPLICATE_PROJECT);
    }

    @Test
    public void execute_invalidDisplayedIndex_failureWithoutMutation() {
        assertCommandFailure(new ProjectCommand(Index.fromOneBased(1), project), model,
                Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
        model.addPerson(new PersonBuilder().withName("Alice").build());
        model.addPerson(new PersonBuilder().withName("Bob").build());
        model.updateFilteredPersonList(person -> person.getName().fullName.equals("Bob"));
        assertCommandFailure(new ProjectCommand(Index.fromOneBased(2), project), model,
                Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    @Test
    public void constructor_nullArguments_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new ProjectCommand(null, project));
        assertThrows(NullPointerException.class, () -> new ProjectCommand(Index.fromOneBased(1), null));
    }

    @Test
    public void equals_comparesIndexAndProject() {
        ProjectCommand command = new ProjectCommand(Index.fromOneBased(1), project);
        assertEquals(command, command);
        assertEquals(command, new ProjectCommand(Index.fromOneBased(1), new Project("ORBITAL")));
        assertEquals(command.hashCode(), new ProjectCommand(Index.fromOneBased(1), project).hashCode());
        assertNotEquals(command, new ProjectCommand(Index.fromOneBased(2), project));
        assertNotEquals(command, new ProjectCommand(Index.fromOneBased(1), new Project("CS2103T")));
        assertNotEquals(command, null);
        assertNotEquals(command, "project");
        assertEquals(ProjectCommand.class.getCanonicalName() + "{index=" + Index.fromOneBased(1)
                + ", project=Orbital}", command.toString());
    }
}
