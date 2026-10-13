package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.util.Optional;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.person.ContactConflict;

/**
 * Represents a command with hidden internal logic and the ability to be executed.
 */
public abstract class Command {

    /**
     * Executes the command and returns the result message.
     *
     * @param model {@code Model} which the command should operate on.
     * @return feedback message of the operation result for display
     * @throws CommandException If an error occurs during command execution.
     */
    public abstract CommandResult execute(Model model) throws CommandException;

    /**
     * Returns true if this command supports undoing its changes to the address book.
     */
    public boolean isUndoable() {
        return false;
    }

    /**
     * Rejects the command if {@code conflict} is present, so that the user can re-enter the clashing detail.
     *
     * @throws CommandException with the conflict's message if {@code conflict} is present.
     */
    protected static void rejectIfConflicting(Optional<ContactConflict> conflict) throws CommandException {
        requireNonNull(conflict);
        if (conflict.isPresent()) {
            throw new CommandException(conflict.get().getMessage());
        }
    }

}
