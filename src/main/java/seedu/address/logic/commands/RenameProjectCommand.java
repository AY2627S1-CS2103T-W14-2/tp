package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.model.Model.PREDICATE_SHOW_ALL_PERSONS;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.person.Person;
import seedu.address.model.project.Project;

/**
 * Renames a project across the address book. LogicManager coordinates confirmation and persistence.
 */
public class RenameProjectCommand extends Command {
    public static final String COMMAND_WORD = "renameproject";
    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Renames a project across all contacts.\n"
            + "Parameters: old/OLD_NAME new/NEW_NAME\n"
            + "Example: " + COMMAND_WORD + " old/CS2103T new/Orbital\n"
            + "Quote names containing old/ or new/ delimiters: old/\"Team new/Alpha\" new/\"Team Beta\". "
            + "Inside quotes, escape a quote as \\\" and a backslash as \\\\.";
    public static final String MESSAGE_NOT_FOUND = "No contacts belong to project \"%s\".";
    public static final String MESSAGE_NO_CHANGES = "No changes made.";
    public static final String MESSAGE_CONFIRM_MERGE =
            "Project \"%2$s\" already exists. This will merge \"%1$s\" into \"%2$s\", combining memberships "
            + "and using the spelling \"%2$s\" for all destination associations. "
            + "%3$d contact(s) will change. Enter the same command again to confirm. Any other command cancels.";

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
     * Prepares and validates the complete replacement without changing contacts, filters, or undo history.
     */
    public RenamePlan prepare(ReadOnlyAddressBook addressBook) throws CommandException {
        requireNonNull(addressBook);
        List<Person> contacts = addressBook.getPersonList();
        if (contacts.stream().noneMatch(person -> person.getProjects().contains(oldProject))) {
            throw new CommandException(String.format(MESSAGE_NOT_FOUND, oldProject));
        }
        boolean merge = !oldProject.equals(newProject)
                && contacts.stream().anyMatch(person -> person.getProjects().contains(newProject));
        List<Person> updatedContacts = new ArrayList<>();
        int changedCount = 0;
        for (Person person : contacts) {
            List<Project> projects = renameProjects(person.getProjects(), merge);
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
        String feedback = String.format("%s project \"%s\" %s \"%s\" for %d contact(s).",
                merge ? "Merged" : "Renamed", oldProject, merge ? "into" : "to", newProject, changedCount);
        return new RenamePlan(replacement, merge, new CommandResult(feedback),
                String.format(MESSAGE_CONFIRM_MERGE, oldProject, newProject, changedCount));
    }

    private List<Project> renameProjects(List<Project> projects, boolean merge) {
        boolean alreadyHasDestination = merge && projects.contains(newProject);
        List<Project> updated = new ArrayList<>();
        for (Project project : projects) {
            if (project.equals(oldProject)) {
                if (!alreadyHasDestination) {
                    updated.add(newProject);
                }
            } else if (merge && project.equals(newProject)) {
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
     * Executes a non-merging rename in the model. Merges must be confirmed through LogicManager.
     */
    @Override
    public CommandResult execute(Model model) throws CommandException {
        RenamePlan plan = prepare(model.getAddressBook());
        if (plan.requiresConfirmation()) {
            throw new CommandException(plan.getConfirmationMessage());
        }
        return plan.applyTo(model);
    }

    /**
     * Matches confirmation by source identity and exact normalised destination spelling.
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

    /**
     * A validated replacement, prepared before persistence and applied only after saving succeeds.
     */
    public static class RenamePlan {
        private final AddressBook replacement;
        private final boolean merge;
        private final CommandResult result;
        private final String confirmationMessage;

        private RenamePlan(AddressBook replacement, boolean merge, CommandResult result, String confirmationMessage) {
            this.replacement = replacement;
            this.merge = merge;
            this.result = result;
            this.confirmationMessage = confirmationMessage;
        }

        public ReadOnlyAddressBook getUpdatedAddressBook() {
            return replacement;
        }

        /**
         * Returns whether two distinct existing projects would be combined.
         */
        public boolean requiresConfirmation() {
            return merge;
        }

        public String getConfirmationMessage() {
            return confirmationMessage;
        }

        /**
         * Installs the validated replacement and shows all contacts.
         */
        public CommandResult applyTo(Model model) {
            model.setAddressBook(replacement);
            model.updateFilteredPersonList(PREDICATE_SHOW_ALL_PERSONS);
            return result;
        }
    }
}
