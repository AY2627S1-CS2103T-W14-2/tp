package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import seedu.address.commons.core.index.Index;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.person.Person;
import seedu.address.model.project.Project;

/**
 * Associates an existing contact with an additional project.
 */
public class ProjectCommand extends Command {
    public static final String COMMAND_WORD = "project";
    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Adds a project to the contact at the displayed index.\n"
            + "Parameters: INDEX (must be a positive integer) pr/PROJECT\n"
            + "Example: " + COMMAND_WORD + " 1 pr/Orbital";
    public static final String MESSAGE_SUCCESS = "Added project %1$s to contact %2$s.";
    public static final String MESSAGE_DUPLICATE_PROJECT = "This contact is already associated with that project.";

    private final Index index;
    private final Project project;

    /**
     * Creates a command to associate the contact at {@code index} with {@code project}.
     */
    public ProjectCommand(Index index, Project project) {
        this.index = requireNonNull(index);
        this.project = requireNonNull(project);
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        List<Person> displayedContacts = model.getFilteredPersonList();
        if (index.getZeroBased() >= displayedContacts.size()) {
            throw new CommandException(Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
        }
        Person original = displayedContacts.get(index.getZeroBased());
        if (original.getProjects().contains(project)) {
            throw new CommandException(MESSAGE_DUPLICATE_PROJECT);
        }

        List<Project> projects = new ArrayList<>(original.getProjects());
        projects.add(project);
        Person updated = new Person(original.getName(), original.getPhone(), original.getEmail(),
                original.getTelegramUsername(), original.getAddress(), projects, original.getTags());
        model.setPerson(original, updated);
        return new CommandResult(String.format(MESSAGE_SUCCESS, project, original.getName()));
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        return other instanceof ProjectCommand otherCommand
                && index.equals(otherCommand.index) && project.equals(otherCommand.project);
    }

    @Override
    public int hashCode() {
        return Objects.hash(index.getZeroBased(), project);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).add("index", index).add("project", project).toString();
    }
}
