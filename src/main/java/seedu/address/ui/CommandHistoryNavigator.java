package seedu.address.ui;

import static java.util.Objects.requireNonNull;

import java.util.List;
import java.util.function.Supplier;

/**
 * Browses a snapshot of submitted commands while preserving the unsubmitted draft.
 */
public class CommandHistoryNavigator {
    private final Supplier<List<String>> historySupplier;
    private List<String> history = List.of();
    private int position;
    private String draft = "";
    private boolean browsing;

    public CommandHistoryNavigator(Supplier<List<String>> historySupplier) {
        this.historySupplier = requireNonNull(historySupplier);
    }

    /** Returns the previous submission, stopping at the oldest entry. */
    public String previous(String currentInput) {
        if (!browsing) {
            history = List.copyOf(historySupplier.get());
            position = history.size();
            draft = currentInput;
            browsing = true;
        }
        if (position > 0) {
            position--;
        }
        return history.isEmpty() ? draft : history.get(position);
    }

    /** Returns the next submission, or restores the draft beyond the newest entry. */
    public String next(String currentInput) {
        if (!browsing) {
            return currentInput;
        }
        if (position < history.size()) {
            position++;
        }
        if (position == history.size()) {
            reset();
            return draft;
        }
        return history.get(position);
    }

    /** Starts fresh navigation after a submission, including a failed submission. */
    public void reset() {
        browsing = false;
    }
}
