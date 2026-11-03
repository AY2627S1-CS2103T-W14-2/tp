package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import seedu.address.commons.core.LogsCenter;
import seedu.address.commons.core.index.Index;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.person.Person;
import seedu.address.model.project.Project;

/**
 * Associates selected existing contacts with an additional project.
 */
public class ProjectCommand extends Command {
    public static final String COMMAND_WORD = "project";
    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Adds a project to contacts at the displayed indices.\n"
            + "Parameters: INDEX [MORE_INDICES] (positive integers) pr/PROJECT\n"
            + "Example: " + COMMAND_WORD + " 1 pr/Orbital";
    public static final String MESSAGE_SUCCESS = "Added project %1$s to contact: %2$s";

    public static final String MESSAGE_DUPLICATE_INDEX = "Contact index %1$d is repeated.";
    public static final String MESSAGE_MULTIPLE_SUCCESS = "Added project %1$s to contacts %2$s.";
    public static final String MESSAGE_CONFLICTING_CONTACTS =
            "No changes made. These contacts already have project %1$s:\n%2$s";
    private static final Logger logger = LogsCenter.getLogger(ProjectCommand.class);

    private final List<Index> indices;
    private final Project project;

    /**
     * Creates a command to associate the contact at {@code index} with {@code project}.
     */
    public ProjectCommand(Index index, Project project) {
        this(List.of(requireNonNull(index)), project);
    }

    /**
     * Creates a command for a non-empty list of distinct displayed contact indices.
     */
    public ProjectCommand(List<Index> indices, Project project) {
        this.indices = ProjectContactSelection.validateIndices(indices, MESSAGE_DUPLICATE_INDEX);
        this.project = requireNonNull(project);
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        List<Person> originals = getValidatedContacts(model);
        List<Person> replacements = ProjectContactSelection.updateContacts(model, originals,
                this::createUpdatedContact);
        logger.fine("Added project association to " + originals.size() + " contact(s)");
        return createSuccessResult(replacements);
    }

    /** Resolves all selections from one snapshot and rejects conflicts before any mutation. */
    private List<Person> getValidatedContacts(Model model) throws CommandException {
        List<Person> selectedContacts = ProjectContactSelection.selectContacts(model, indices);
        List<String> conflicts = ProjectContactSelection.describeContacts(indices, selectedContacts,
                contact -> contact.getProjects().contains(project));
        if (!conflicts.isEmpty()) {
            logger.fine("Project association rejected: duplicate project for contacts "
                    + String.join(", ", conflicts));
            Project savedProject = model.getAddressBook().getPersonList().stream().filter(selectedContacts::contains)
                    .flatMap(person -> person.getProjects().stream()).filter(project::equals).findFirst().orElseThrow();
            throw new CommandException(String.format(MESSAGE_CONFLICTING_CONTACTS, savedProject,
                    String.join("\n", conflicts)));
        }
        return selectedContacts;
    }

    /** Appends the association while preserving existing project order and contact details. */
    private Person createUpdatedContact(Person original) {
        List<Project> projects = new ArrayList<>(original.getProjects());
        projects.add(project);
        return original.withProjects(projects);
    }

    /** Formats the single-contact or batch success message for the updated contacts, using the original indices. */
    private CommandResult createSuccessResult(List<Person> contacts) {
        if (contacts.size() == 1) {
            return new CommandResult(String.format(MESSAGE_SUCCESS, project, Messages.format(contacts.get(0))));
        }
        List<String> selectedContacts = ProjectContactSelection.describeContacts(indices, contacts, contact -> true);
        return new CommandResult(String.format(MESSAGE_MULTIPLE_SUCCESS, project,
                String.join(", ", selectedContacts)));
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        return other instanceof ProjectCommand otherCommand
                && indices.equals(otherCommand.indices) && project.equals(otherCommand.project);
    }

    @Override
    public int hashCode() {
        return Objects.hash(indices.stream().map(Index::getZeroBased).collect(Collectors.toList()), project);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).add("indices", indices).add("project", project).toString();
    }

    /**
     * Returns true as this command changes the address book.
     */
    @Override
    public boolean isUndoable() {
        return true;
    }
}
