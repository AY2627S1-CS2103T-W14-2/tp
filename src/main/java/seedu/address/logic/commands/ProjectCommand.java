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
    public static final String MESSAGE_SUCCESS = "Added project %1$s to contact %2$s.";

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
        this.indices = List.copyOf(indices);
        if (indices.isEmpty()) {
            throw new IllegalArgumentException("At least one contact index is required.");
        }
        for (int i = 0; i < indices.size(); i++) {
            if (indices.subList(0, i).contains(indices.get(i))) {
                throw new IllegalArgumentException(String.format(MESSAGE_DUPLICATE_INDEX,
                        indices.get(i).getOneBased()));
            }
        }
        this.project = requireNonNull(project);
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        List<Person> originals = getValidatedContacts(model);
        List<Person> replacements = originals.stream().map(this::createUpdatedContact).toList();
        for (int i = 0; i < originals.size(); i++) {
            model.setPerson(originals.get(i), replacements.get(i));
        }
        logger.fine("Added project association to " + originals.size() + " contact(s)");
        return createSuccessResult(originals);
    }

    /** Resolves all selections from one snapshot and rejects conflicts before any mutation. */
    private List<Person> getValidatedContacts(Model model) throws CommandException {
        List<Person> displayedContacts = new ArrayList<>(model.getFilteredPersonList());
        List<Person> selectedContacts = new ArrayList<>();
        List<String> conflicts = new ArrayList<>();
        for (Index index : indices) {
            if (index.getZeroBased() >= displayedContacts.size()) {
                logger.fine("Project association rejected: invalid displayed index " + index.getOneBased());
                throw new CommandException(Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
            }
            Person contact = displayedContacts.get(index.getZeroBased());
            if (contact.getProjects().contains(project)) {
                logger.fine("Project association rejected: duplicate project at index " + index.getOneBased());
                conflicts.add(index.getOneBased() + ": " + contact.getName());
            }
            selectedContacts.add(contact);
        }
        if (!conflicts.isEmpty()) {
            Project savedProject = model.getAddressBook().getPersonList().stream()
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
        return new Person(original.getName(), original.getPhone(), original.getEmail(),
                original.getTelegramUsername(), projects);
    }

    /** Formats the existing single-contact or batch success message using the original indices. */
    private CommandResult createSuccessResult(List<Person> contacts) {
        if (contacts.size() == 1) {
            return new CommandResult(String.format(MESSAGE_SUCCESS, project, contacts.get(0).getName()));
        }
        List<String> selectedContacts = new ArrayList<>();
        for (int i = 0; i < contacts.size(); i++) {
            selectedContacts.add(indices.get(i).getOneBased() + ": " + contacts.get(i).getName());
        }
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
