package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.util.List;
import java.util.logging.Logger;

import seedu.address.commons.core.LogsCenter;
import seedu.address.model.Model;

/**
 * Displays earlier submissions from the current session, newest first.
 */
public class HistoryCommand extends Command {
    public static final String COMMAND_WORD = "history";
    public static final String MESSAGE_USAGE = "history: Lists earlier commands, newest first.\nExample: history";
    public static final String MESSAGE_EMPTY = "No commands entered yet.";
    public static final String MESSAGE_HEADER = "Command history (newest first):\n";
    private static final Logger logger = LogsCenter.getLogger(HistoryCommand.class);
    private final List<String> history;

    public HistoryCommand(List<String> history) {
        this.history = List.copyOf(history);
    }

    @Override
    public CommandResult execute(Model model) {
        requireNonNull(model);
        logger.fine("Displaying " + history.size() + " earlier command(s)");
        if (history.isEmpty()) {
            return new CommandResult(MESSAGE_EMPTY);
        }
        return new CommandResult(MESSAGE_HEADER + String.join("\n", history.reversed()));
    }
}
