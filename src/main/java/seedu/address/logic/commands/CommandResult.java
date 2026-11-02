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
    private final boolean shouldShowHelp;

    /** The application should exit. */
    private final boolean shouldExit;

    /**
     * False only for an undoable command that succeeded without changing contact data, so that no undo entry is
     * recorded for it. {@code LogicManager} checks it only for undoable commands; other results keep the default.
     */
    private final boolean isDataChanged;

    /**
     * Constructs a {@code CommandResult} with the specified fields, for a command that may have changed contact data.
     */
    public CommandResult(String feedbackToUser, boolean shouldShowHelp, boolean shouldExit) {
        this(feedbackToUser, shouldShowHelp, shouldExit, true);
    }

    private CommandResult(String feedbackToUser, boolean shouldShowHelp, boolean shouldExit, boolean isDataChanged) {
        this.feedbackToUser = requireNonNull(feedbackToUser);
        this.shouldShowHelp = shouldShowHelp;
        this.shouldExit = shouldExit;
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
        return shouldShowHelp;
    }

    public boolean isExit() {
        return shouldExit;
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
                && shouldShowHelp == otherCommandResult.shouldShowHelp
                && shouldExit == otherCommandResult.shouldExit
                && isDataChanged == otherCommandResult.isDataChanged;
    }

    @Override
    public int hashCode() {
        return Objects.hash(feedbackToUser, shouldShowHelp, shouldExit, isDataChanged);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("feedbackToUser", feedbackToUser)
                .add("showHelp", shouldShowHelp)
                .add("exit", shouldExit)
                .add("isDataChanged", isDataChanged)
                .toString();
    }

}
