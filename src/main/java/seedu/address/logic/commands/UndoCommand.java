package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;

/**
 * Undoes the most recent successful undoable command.
 */
public class UndoCommand extends Command {
    public static final String COMMAND_WORD = "undo";
    public static final String MESSAGE_SUCCESS = "Last command undone.";
    public static final String MESSAGE_FAILURE = "No command to undo.";

    /**
     * Restores the saved address book state.
     * @throws CommandException if there is no command to undo.
     */
    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);

        if (!model.canUndo()) {
            throw new CommandException(MESSAGE_FAILURE);
        }

        model.undo();
        return new CommandResult(MESSAGE_SUCCESS);
    }
}
