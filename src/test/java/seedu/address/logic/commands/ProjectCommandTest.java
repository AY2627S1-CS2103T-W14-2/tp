package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;

import java.util.List;

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
        Person first = PersonBuilder.named("Alice").build();
        Person target = PersonBuilder.named("Bob").withProjects("CS2103T", "CS2101").build();
        model.addPerson(first);
        model.addPerson(target);
        model.updateFilteredPersonList(person -> person.getName().equals(target.getName()));

        CommandResult result = new ProjectCommand(Index.fromOneBased(1), project).execute(model);

        Person expected = new PersonBuilder(target).withProjects("CS2103T", "CS2101", "Orbital").build();
        assertEquals(expected, model.getFilteredPersonList().get(0));
        assertEquals(1, model.getFilteredPersonList().size());
        assertEquals(first, model.getAddressBook().getPersonList().get(0));
        assertEquals(String.format(ProjectCommand.MESSAGE_SUCCESS, project, Messages.format(expected)),
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
                model, String.format(ProjectCommand.MESSAGE_CONFLICTING_CONTACTS, "team ALPHA",
                        "1: " + model.getFilteredPersonList().get(0).getName()));
    }

    @Test
    public void execute_invalidDisplayedIndex_failureWithoutMutation() {
        assertCommandFailure(new ProjectCommand(Index.fromOneBased(1), project), model,
                Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
        model.addPerson(PersonBuilder.named("Alice").build());
        model.addPerson(PersonBuilder.named("Bob").build());
        model.updateFilteredPersonList(person -> person.getName().fullName.equals("Bob"));
        assertCommandFailure(new ProjectCommand(Index.fromOneBased(2), project), model,
                Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    @Test
    public void constructor_nullArguments_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new ProjectCommand((Index) null, project));
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
        assertEquals(ProjectCommand.class.getCanonicalName() + "{indices=" + List.of(Index.fromOneBased(1))
                + ", project=Orbital}", command.toString());
    }
    @Test
    public void execute_multipleContacts_snapshotsIndicesBeforeFilterChanges() throws Exception {
        Person hidden = PersonBuilder.named("Hidden").withProjects("Orbital").build();
        Person alice = PersonBuilder.named("Alice").withProjects("First", "Second").build();
        Person bob = PersonBuilder.named("Bob").withProjects().build();
        Person untouched = PersonBuilder.named("Untouched").withProjects().build();
        model.addPerson(hidden);
        model.addPerson(alice);
        model.addPerson(bob);
        model.addPerson(untouched);
        model.updateFilteredPersonList(person -> !person.getProjects().contains(project));
        CommandResult result = new ProjectCommand(List.of(Index.fromOneBased(2), Index.fromOneBased(1)),
                project).execute(model);
        assertEquals(List.of(hidden, new PersonBuilder(alice).withProjects("First", "Second", "Orbital").build(),
                new PersonBuilder(bob).withProjects("Orbital").build(), untouched),
                model.getAddressBook().getPersonList());
        assertEquals(List.of(untouched), model.getFilteredPersonList());
        assertEquals(String.format(ProjectCommand.MESSAGE_MULTIPLE_SUCCESS, project, "2: Bob, 1: Alice"),
                result.getFeedbackToUser());
    }

    @Test
    public void execute_laterSelectionInvalid_rejectsWithoutPartialChanges() {
        model.addPerson(PersonBuilder.named("Alice").withProjects().build());
        model.addPerson(PersonBuilder.named("Bob").withProjects("ORBITAL").build());
        assertCommandFailure(new ProjectCommand(List.of(Index.fromOneBased(1), Index.fromOneBased(3)), project),
                model, Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
        assertCommandFailure(new ProjectCommand(List.of(Index.fromOneBased(1), Index.fromOneBased(2)), project),
                model, String.format(ProjectCommand.MESSAGE_CONFLICTING_CONTACTS, "Orbital", "2: Bob"));
    }

    @Test
    public void constructor_invalidIndexLists_rejected() {
        assertThrows(NullPointerException.class, () -> new ProjectCommand((List<Index>) null, project));
        assertThrows(IllegalArgumentException.class, () -> new ProjectCommand(List.of(), project));
        assertThrows(IllegalArgumentException.class, () -> new ProjectCommand(
                List.of(Index.fromOneBased(1), Index.fromOneBased(1)), project));
    }

    @Test
    public void execute_multipleConflicts_listsEveryConflictWithoutChangingValidSelections() {
        model.addPerson(PersonBuilder.named("Hidden").withProjects("Orbital").build());
        model.addPerson(PersonBuilder.named("Alice").withProjects("ORBITAL").build());
        model.addPerson(PersonBuilder.named("Bob").withProjects().build());
        model.addPerson(PersonBuilder.named("Charlie").withProjects("orbital").build());
        model.updateFilteredPersonList(person -> !person.getName().fullName.equals("Hidden"));
        assertCommandFailure(new ProjectCommand(List.of(Index.fromOneBased(3), Index.fromOneBased(2),
                Index.fromOneBased(1)), project), model,
                "No changes made. These contacts already have project Orbital:\n3: Charlie\n1: Alice");
        assertCommandFailure(new ProjectCommand(List.of(Index.fromOneBased(1), Index.fromOneBased(4)), project),
                model, Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

}
