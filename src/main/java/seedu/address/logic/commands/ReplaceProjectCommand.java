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
 * Replaces one project association on an existing contact.
 */
public class ReplaceProjectCommand extends Command {
    public static final String COMMAND_WORD = "replaceproject";
    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Replaces a project on the contact at the displayed index.\n"
            + "Parameters: INDEX (must be a positive integer) old/OLD_PROJECT new/NEW_PROJECT\n"
            + "Example: " + COMMAND_WORD + " 1 old/CS2103T new/Orbital";
    public static final String MESSAGE_SUCCESS = "Replaced project %1$s with %2$s for contact %3$s.";
    public static final String MESSAGE_DUPLICATE_PROJECT = "This contact is already associated with that project.";

    public static final String MESSAGE_PROJECT_NOT_FOUND = "This contact is not associated with the old project.";
    public static final String MESSAGE_UNCHANGED_PROJECT = "The replacement project must differ from the old project.";

    private static final Logger logger = LogsCenter.getLogger(ReplaceProjectCommand.class);

    private final Index index;
    private final Project oldProject;
    private final Project newProject;

    /**
     * Creates a command to replace {@code oldProject} with {@code newProject} on the selected contact.
     */
    public ReplaceProjectCommand(Index index, Project oldProject, Project newProject) {
        this.index = requireNonNull(index);
        this.oldProject = requireNonNull(oldProject);
        this.newProject = requireNonNull(newProject);
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        List<Person> displayedContacts = model.getFilteredPersonList();
        if (index.getZeroBased() >= displayedContacts.size()) {
            logger.fine("Project replacement rejected: index " + index.getOneBased()
                    + " exceeds displayed list size " + displayedContacts.size());
            throw new CommandException(Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
        }
        Person original = displayedContacts.get(index.getZeroBased());
        int projectIndex = original.getProjects().indexOf(oldProject);
        if (projectIndex < 0) {
            logger.fine("Project replacement rejected: old association not found at contact index "
                    + index.getOneBased());
            throw new CommandException(MESSAGE_PROJECT_NOT_FOUND);
        }
        if (oldProject.equals(newProject)) {
            logger.fine("Project replacement rejected: old and new projects are equivalent");
            throw new CommandException(MESSAGE_UNCHANGED_PROJECT);
        }
        if (original.getProjects().contains(newProject)) {
            logger.fine("Project replacement rejected: new association already exists at contact index "
                    + index.getOneBased());
            throw new CommandException(MESSAGE_DUPLICATE_PROJECT);
        }

        List<Project> projects = new ArrayList<>(original.getProjects());
        projects.set(projectIndex, newProject);
        Person updated = new Person(original.getName(), original.getPhone(), original.getEmail(),
                original.getTelegramUsername(), projects);
        model.setPerson(original, updated);
        logger.fine("Replaced project association at position " + (projectIndex + 1)
                + " for contact at displayed index " + index.getOneBased());
        return new CommandResult(String.format(MESSAGE_SUCCESS, original.getProjects().get(projectIndex),
                newProject, original.getName()));
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        return other instanceof ReplaceProjectCommand otherCommand
                && index.equals(otherCommand.index) && oldProject.equals(otherCommand.oldProject)
                && newProject.equals(otherCommand.newProject);
    }

    @Override
    public int hashCode() {
        return Objects.hash(index.getZeroBased(), oldProject, newProject);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).add("index", index).add("oldProject", oldProject)
                .add("newProject", newProject).toString();
    }

    /**
     * Returns true as this command changes the address book.
     */
    @Override
    public boolean isUndoable() {
        return true;
    }
}
