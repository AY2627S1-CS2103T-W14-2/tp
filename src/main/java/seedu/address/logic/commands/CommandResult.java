package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.util.Objects;

import seedu.address.commons.util.ToStringBuilder;

/**
 * Represents the result of a command execution.
 */
public class CommandResult {

    private final String feedbackToUser;

    /** Help information should be shown to the user. */
    private final boolean showHelp;

    /** The application should exit. */
    private final boolean exit;

    /**
     * False only for an undoable command that succeeded without changing contact data, so that no undo entry is
     * recorded for it. {@code LogicManager} checks it only for undoable commands; other results keep the default.
     */
    private final boolean isDataChanged;

    /**
     * Constructs a {@code CommandResult} with the specified fields, for a command that may have changed contact data.
     */
    public CommandResult(String feedbackToUser, boolean showHelp, boolean exit) {
        this(feedbackToUser, showHelp, exit, true);
    }

    private CommandResult(String feedbackToUser, boolean showHelp, boolean exit, boolean isDataChanged) {
        this.feedbackToUser = requireNonNull(feedbackToUser);
        this.showHelp = showHelp;
        this.exit = exit;
        this.isDataChanged = isDataChanged;
    }

    /**
     * Constructs a {@code CommandResult} with the specified {@code feedbackToUser},
     * and other fields set to their default value.
     */
    public CommandResult(String feedbackToUser) {
        this(feedbackToUser, false, false);
    }

    /**
     * Returns a result for a command that succeeded but left the contact data unchanged,
     * so that no undo entry is recorded for it.
     */
    public static CommandResult withoutDataChange(String feedbackToUser) {
        return new CommandResult(feedbackToUser, false, false, false);
    }

    public String getFeedbackToUser() {
        return feedbackToUser;
    }

    public boolean isShowHelp() {
        return showHelp;
    }

    public boolean isExit() {
        return exit;
    }

    /**
     * Returns false if an undoable command left contact data unchanged and should not be recorded for undo.
     */
    public boolean isDataChanged() {
        return isDataChanged;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof CommandResult otherCommandResult)) {
            return false;
        }

        return feedbackToUser.equals(otherCommandResult.feedbackToUser)
                && showHelp == otherCommandResult.showHelp
                && exit == otherCommandResult.exit
                && isDataChanged == otherCommandResult.isDataChanged;
    }

    @Override
    public int hashCode() {
        return Objects.hash(feedbackToUser, showHelp, exit, isDataChanged);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("feedbackToUser", feedbackToUser)
                .add("showHelp", showHelp)
                .add("exit", exit)
                .add("isDataChanged", isDataChanged)
                .toString();
    }

}
