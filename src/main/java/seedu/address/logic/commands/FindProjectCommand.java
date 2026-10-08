package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import seedu.address.commons.util.ToStringBuilder;
import seedu.address.model.Model;
import seedu.address.model.person.ProjectContainsKeywordPredicate;

/**
 * Lists contacts whose project names contain the whole search phrase, ignoring case.
 */
public class FindProjectCommand extends Command {

    public static final String COMMAND_WORD = "findproject";
    public static final int MAX_KEYWORD_LENGTH = 40;
    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Finds contacts whose project names contain the whole keyword phrase (case-insensitive).\n"
            + "Parameters: KEYWORD (1 to " + MAX_KEYWORD_LENGTH + " characters after trimming surrounding whitespace)\n"
            + "Example: " + COMMAND_WORD + " team alpha";
    public static final String MESSAGE_KEYWORD_TOO_LONG = "Project keyword must not exceed " + MAX_KEYWORD_LENGTH
            + " characters after trimming surrounding whitespace.";
    public static final String MESSAGE_CONTACTS_FOUND = "Found %1$d contact(s) matching the project keyword.";
    public static final String MESSAGE_NO_CONTACTS = "No contacts found for the project keyword.";

    private final ProjectContainsKeywordPredicate predicate;

    /**
     * Creates a command that filters contacts using the given project predicate.
     */
    public FindProjectCommand(ProjectContainsKeywordPredicate predicate) {
        this.predicate = requireNonNull(predicate);
    }

    @Override
    public CommandResult execute(Model model) {
        requireNonNull(model);
        model.updateFilteredPersonList(predicate);
        int resultCount = model.getFilteredPersonList().size();
        return new CommandResult(resultCount == 0 ? MESSAGE_NO_CONTACTS
                : String.format(MESSAGE_CONTACTS_FOUND, resultCount));
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        if (!(other instanceof FindProjectCommand otherCommand)) {
            return false;
        }

        return predicate.equals(otherCommand.predicate);
    }

    @Override
    public int hashCode() {
        return predicate.hashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).add("predicate", predicate).toString();
    }
}
