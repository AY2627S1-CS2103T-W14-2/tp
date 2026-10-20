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

public class DeleteProjectCommandTest {
    private final Model model = new ModelManager();
    private final Project project = new Project("Orbital");

    @Test
    public void execute_filteredContact_preservesFieldsOrderAndFilter() throws Exception {
        Person first = PersonBuilder.named("Alice").withProjects("Orbital").build();
        Person target = PersonBuilder.named("Bob").withProjects("CS2103T", "Orbital", "CS2101").build();
        model.addPerson(first);
        model.addPerson(target);
        model.updateFilteredPersonList(person -> person.getName().equals(target.getName()));

        CommandResult result = new DeleteProjectCommand(Index.fromOneBased(1), project).execute(model);

        Person expected = new PersonBuilder(target).withProjects("CS2103T", "CS2101").build();
        assertEquals(expected, model.getFilteredPersonList().get(0));
        assertEquals(1, model.getFilteredPersonList().size());
        assertEquals(first, model.getAddressBook().getPersonList().get(0));
        assertEquals(String.format(DeleteProjectCommand.MESSAGE_SUCCESS, project, target.getName()),
                result.getFeedbackToUser());
    }

    @Test
    public void execute_lastProjectRemoval_success() throws Exception {
        Person original = new PersonBuilder().withProjects("Orbital").build();
        model.addPerson(original);
        new DeleteProjectCommand(Index.fromOneBased(1), project).execute(model);
        assertEquals(new PersonBuilder(original).withProjects().build(),
                model.getFilteredPersonList().get(0));
    }

    @Test
    public void execute_missingProject_failureWithoutMutation() {
        model.addPerson(new PersonBuilder().withProjects("Team Alpha").build());
        assertCommandFailure(new DeleteProjectCommand(Index.fromOneBased(1), new Project("Missing")),
                model, DeleteProjectCommand.MESSAGE_PROJECT_NOT_FOUND);
    }

    @Test
    public void execute_normalizedProject_removesStoredAssociation() throws Exception {
        Person original = new PersonBuilder().withProjects("Team Alpha", "Other").build();
        model.addPerson(original);
        CommandResult result = new DeleteProjectCommand(Index.fromOneBased(1),
                new Project(" team   ALPHA ")).execute(model);
        assertEquals(new PersonBuilder(original).withProjects("Other").build(),
                model.getFilteredPersonList().get(0));
        assertEquals(String.format(DeleteProjectCommand.MESSAGE_SUCCESS, "Team Alpha", original.getName()),
                result.getFeedbackToUser());
    }

    @Test
    public void execute_noProjects_failureWithoutMutation() {
        model.addPerson(new PersonBuilder().withProjects().build());
        assertCommandFailure(new DeleteProjectCommand(Index.fromOneBased(1), project), model,
                DeleteProjectCommand.MESSAGE_PROJECT_NOT_FOUND);
    }

    @Test
    public void execute_invalidDisplayedIndex_failureWithoutMutation() {
        assertCommandFailure(new DeleteProjectCommand(Index.fromOneBased(1), project), model,
                Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
        model.addPerson(PersonBuilder.named("Alice").build());
        model.addPerson(PersonBuilder.named("Bob").build());
        model.updateFilteredPersonList(person -> person.getName().fullName.equals("Bob"));
        assertCommandFailure(new DeleteProjectCommand(Index.fromOneBased(2), project), model,
                Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    @Test
    public void constructor_nullArguments_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new DeleteProjectCommand(null, project));
        assertThrows(NullPointerException.class, () -> new DeleteProjectCommand(Index.fromOneBased(1), null));
    }

    @Test
    public void equals_comparesIndexAndProject() {
        DeleteProjectCommand command = new DeleteProjectCommand(Index.fromOneBased(1), project);
        assertEquals(command, command);
        assertEquals(command, new DeleteProjectCommand(Index.fromOneBased(1), new Project("ORBITAL")));
        assertEquals(command.hashCode(), new DeleteProjectCommand(Index.fromOneBased(1), project).hashCode());
        assertNotEquals(command, new DeleteProjectCommand(Index.fromOneBased(2), project));
        assertNotEquals(command, new DeleteProjectCommand(Index.fromOneBased(1), new Project("CS2103T")));
        assertNotEquals(command, null);
        assertNotEquals(command, "project");
        assertEquals(DeleteProjectCommand.class.getCanonicalName() + "{index=" + Index.fromOneBased(1)
                + ", project=Orbital}", command.toString());
    }
}
