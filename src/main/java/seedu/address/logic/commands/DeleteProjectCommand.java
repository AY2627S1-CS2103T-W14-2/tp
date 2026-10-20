package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;

import seedu.address.commons.core.LogsCenter;
import seedu.address.commons.core.index.Index;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.person.Person;
import seedu.address.model.project.Project;

/**
 * Removes one project association from an existing contact.
 */
public class DeleteProjectCommand extends Command {
    public static final String COMMAND_WORD = "deleteproject";
    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Removes a project from the contact at the displayed index.\n"
            + "Parameters: INDEX (must be a positive integer) pr/PROJECT\n"
            + "Example: " + COMMAND_WORD + " 1 pr/Orbital";
    public static final String MESSAGE_SUCCESS = "Removed project %1$s from contact %2$s.";
    public static final String MESSAGE_PROJECT_NOT_FOUND = "This contact is not associated with that project.";

    private static final Logger logger = LogsCenter.getLogger(DeleteProjectCommand.class);

    private final Index index;
    private final Project project;

    /**
     * Creates a command to remove {@code project} from the contact at {@code index}.
     */
    public DeleteProjectCommand(Index index, Project project) {
        this.index = requireNonNull(index);
        this.project = requireNonNull(project);
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        List<Person> displayedContacts = model.getFilteredPersonList();
        if (index.getZeroBased() >= displayedContacts.size()) {
            logger.fine("Project removal rejected: index " + index.getOneBased()
                    + " exceeds displayed list size " + displayedContacts.size());
            throw new CommandException(Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
        }
        Person original = displayedContacts.get(index.getZeroBased());
        int projectIndex = original.getProjects().indexOf(project);
        if (projectIndex < 0) {
            logger.fine("Project removal rejected: association not found at contact index " + index.getOneBased());
            throw new CommandException(MESSAGE_PROJECT_NOT_FOUND);
        }

        List<Project> projects = new ArrayList<>(original.getProjects());
        Project removedProject = projects.remove(projectIndex);
        Person updated = new Person(original.getName(), original.getPhone(), original.getEmail(),
                original.getTelegramUsername(), projects);
        model.setPerson(original, updated);
        logger.fine("Removed project association from contact at displayed index " + index.getOneBased());
        return new CommandResult(String.format(MESSAGE_SUCCESS, removedProject, original.getName()));
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        return other instanceof DeleteProjectCommand otherCommand
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

    /**
     * Returns true as this command changes the address book.
     */
    @Override
    public boolean isUndoable() {
        return true;
    }
}
