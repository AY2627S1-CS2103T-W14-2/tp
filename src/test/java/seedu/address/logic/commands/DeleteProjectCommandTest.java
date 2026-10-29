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

public class DeleteProjectCommandTest {
    private final Model model = new ModelManager();
    private final Project project = new Project("Orbital");

    @Test
    public void execute_batch_usesFirstSelectedSavedSpellingBeforeRemovingAssociations() throws Exception {
        Model target = new ModelManager();
        Person hidden = PersonBuilder.named("Hidden").withProjects("ORBITAL").build();
        target.addPerson(hidden);
        target.addPerson(PersonBuilder.named("Alice").withProjects("orbital").build());
        target.addPerson(PersonBuilder.named("Bob").withProjects("Orbital").build());
        target.updateFilteredPersonList(person -> !person.equals(hidden));
        CommandResult result = new DeleteProjectCommand(List.of(Index.fromOneBased(2), Index.fromOneBased(1)),
                new Project("ORBITAL")).execute(target);
        assertEquals("Removed project orbital from contacts 2: Bob, 1: Alice.", result.getFeedbackToUser());
        assertEquals(List.of(), target.getAddressBook().getPersonList().get(1).getProjects());
        assertEquals(List.of(), target.getAddressBook().getPersonList().getLast().getProjects());
        assertEquals(List.of(new Project("ORBITAL")), target.getAddressBook().getPersonList().getFirst().getProjects());
    }

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
        assertEquals(String.format(DeleteProjectCommand.MESSAGE_SUCCESS, project, Messages.format(expected)),
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
                model, String.format(DeleteProjectCommand.MESSAGE_CONFLICTING_CONTACTS, "Missing",
                        "1: " + model.getFilteredPersonList().get(0).getName()));
    }

    @Test
    public void execute_normalizedProject_removesStoredAssociation() throws Exception {
        Person original = new PersonBuilder().withProjects("Team Alpha", "Other").build();
        model.addPerson(original);
        CommandResult result = new DeleteProjectCommand(Index.fromOneBased(1),
                new Project(" team   ALPHA ")).execute(model);
        Person expected = new PersonBuilder(original).withProjects("Other").build();
        assertEquals(expected, model.getFilteredPersonList().get(0));
        assertEquals(String.format(DeleteProjectCommand.MESSAGE_SUCCESS, "Team Alpha", Messages.format(expected)),
                result.getFeedbackToUser());
    }

    @Test
    public void execute_noProjects_failureWithoutMutation() {
        model.addPerson(new PersonBuilder().withProjects().build());
        assertCommandFailure(new DeleteProjectCommand(Index.fromOneBased(1), project), model,
                String.format(DeleteProjectCommand.MESSAGE_CONFLICTING_CONTACTS, "Orbital",
                        "1: " + model.getFilteredPersonList().get(0).getName()));
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
        assertThrows(NullPointerException.class, () -> new DeleteProjectCommand((Index) null, project));
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
        assertEquals(DeleteProjectCommand.class.getCanonicalName() + "{indices=" + List.of(Index.fromOneBased(1))
                + ", project=Orbital}", command.toString());
    }
    @Test
    public void execute_multipleFilteredContacts_removesAllDespiteChangingResults() throws Exception {
        Person hidden = PersonBuilder.named("Hidden").withProjects("Other").build();
        Person alice = PersonBuilder.named("Alice").withProjects("First", "Orbital", "Last").build();
        Person bob = PersonBuilder.named("Bob").withProjects("ORBITAL").build();
        Person untouched = PersonBuilder.named("Untouched").withProjects("Orbital").build();
        model.addPerson(hidden);
        model.addPerson(alice);
        model.addPerson(bob);
        model.addPerson(untouched);
        model.updateFilteredPersonList(person -> person.getProjects().contains(project));
        CommandResult result = new DeleteProjectCommand(List.of(Index.fromOneBased(2), Index.fromOneBased(1)),
                project).execute(model);
        assertEquals(List.of(hidden, new PersonBuilder(alice).withProjects("First", "Last").build(),
                new PersonBuilder(bob).withProjects().build(), untouched), model.getAddressBook().getPersonList());
        assertEquals(List.of(untouched), model.getFilteredPersonList());
        assertEquals(String.format(DeleteProjectCommand.MESSAGE_MULTIPLE_SUCCESS, project, "2: Bob, 1: Alice"),
                result.getFeedbackToUser());
    }

    @Test
    public void execute_missingAssociationsOrInvalidIndex_rejectsEntireBatch() {
        model.addPerson(PersonBuilder.named("Alice").withProjects("Orbital").build());
        model.addPerson(PersonBuilder.named("Bob").withProjects().build());
        model.addPerson(PersonBuilder.named("Charlie").withProjects("Other").build());
        assertCommandFailure(new DeleteProjectCommand(List.of(Index.fromOneBased(1), Index.fromOneBased(3),
                Index.fromOneBased(2)), project), model,
                "No changes made. These contacts do not have project Orbital:\n3: Charlie\n2: Bob");
        assertCommandFailure(new DeleteProjectCommand(List.of(Index.fromOneBased(1), Index.fromOneBased(4)),
                project), model, Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    @Test
    public void constructor_invalidLists_rejected() {
        assertThrows(NullPointerException.class, () -> new DeleteProjectCommand((List<Index>) null, project));
        assertThrows(IllegalArgumentException.class, () -> new DeleteProjectCommand(List.of(), project));
        assertThrows(IllegalArgumentException.class, () -> new DeleteProjectCommand(
                List.of(Index.fromOneBased(1), Index.fromOneBased(1)), project));
    }

}
