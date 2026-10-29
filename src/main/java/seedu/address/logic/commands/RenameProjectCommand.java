package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.person.Person;
import seedu.address.model.project.Project;

/**
 * Renames or merges a project across the address book, preserving the current search filter.
 */
public class RenameProjectCommand extends Command {
    public static final String COMMAND_WORD = "renameproject";
    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Renames a project across all contacts.\n"
            + "Parameters: old/OLD_NAME new/NEW_NAME\n"
            + "Example: " + COMMAND_WORD + " old/CS2103T new/Orbital";
    public static final String MESSAGE_NOT_FOUND = "No contacts belong to project \"%s\".";
    public static final String MESSAGE_NO_CHANGES = "No changes made.";
    public static final String MESSAGE_SUCCESS = "%s project \"%s\" %s \"%s\" for %d contact(s). "
            + "Use undo to reverse this change.";

    private final Project oldProject;
    private final Project newProject;

    /**
     * Creates a rename from a project identity to the requested destination spelling.
     */
    public RenameProjectCommand(Project oldProject, Project newProject) {
        this.oldProject = requireNonNull(oldProject);
        this.newProject = requireNonNull(newProject);
    }

    /**
     * Validates the complete replacement before applying it, including contacts hidden by the current search.
     */
    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        List<Person> contacts = model.getAddressBook().getPersonList();
        if (contacts.stream().noneMatch(person -> person.getProjects().contains(oldProject))) {
            throw new CommandException(String.format(MESSAGE_NOT_FOUND, oldProject));
        }
        Project savedOldProject = contacts.stream().flatMap(person -> person.getProjects().stream())
                .filter(oldProject::equals).findFirst().orElseThrow();
        boolean isMerge = !oldProject.equals(newProject)
                && contacts.stream().anyMatch(person -> person.getProjects().contains(newProject));
        List<Person> updatedContacts = new ArrayList<>();
        int changedCount = 0;
        for (Person person : contacts) {
            List<Project> projects = renameProjects(person.getProjects(), isMerge);
            // Project.equals ignores case, so compare the actual display strings.
            if (displayNames(person.getProjects()).equals(displayNames(projects))) {
                updatedContacts.add(person);
            } else {
                updatedContacts.add(new Person(person.getName(), person.getPhone(), person.getEmail(),
                        person.getTelegramUsername(), projects));
                changedCount++;
            }
        }
        if (changedCount == 0) {
            throw new CommandException(MESSAGE_NO_CHANGES);
        }
        AddressBook replacement = new AddressBook();
        replacement.setPersons(updatedContacts);
        model.setAddressBook(replacement);
        return new CommandResult(String.format(Locale.ROOT, MESSAGE_SUCCESS,
                isMerge ? "Merged" : "Renamed", escapedName(savedOldProject), isMerge ? "into" : "to",
                escapedName(newProject), changedCount));
    }

    /** Escapes literal quotes and backslashes inside the message's quoted project names. */
    private static String escapedName(Project project) {
        return project.projectName.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private List<Project> renameProjects(List<Project> projects, boolean isMerge) {
        boolean alreadyHasDestination = isMerge && projects.contains(newProject);
        List<Project> updated = new ArrayList<>();
        for (Project project : projects) {
            if (project.equals(oldProject)) {
                if (!alreadyHasDestination) {
                    updated.add(newProject);
                }
            } else if (isMerge && project.equals(newProject)) {
                updated.add(newProject);
            } else {
                updated.add(project);
            }
        }
        return updated;
    }

    private static List<String> displayNames(List<Project> projects) {
        return projects.stream().map(project -> project.projectName).toList();
    }

    /**
     * Compares source identity and exact normalised destination spelling.
     */
    @Override
    public boolean equals(Object other) {
        return other instanceof RenameProjectCommand otherCommand
                && oldProject.equals(otherCommand.oldProject)
                && newProject.projectName.equals(otherCommand.newProject.projectName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(oldProject, newProject.projectName);
    }

    @Override
    public boolean isUndoable() {
        return true;
    }
}
