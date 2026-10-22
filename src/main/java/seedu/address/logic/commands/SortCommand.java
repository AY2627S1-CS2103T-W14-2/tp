package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.util.Comparator;
import java.util.List;
import java.util.logging.Logger;

import seedu.address.commons.core.LogsCenter;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.person.Person;
import seedu.address.model.person.PersonNameComparator;

/**
 * Sorts all saved contacts alphabetically by name. The new order is saved, and the current search results are kept.
 */
public class SortCommand extends Command {

    public static final String COMMAND_WORD = "sort";

    public static final String MESSAGE_SUCCESS = "Sorted %1$d contact(s) by name.";
    public static final String MESSAGE_ALREADY_SORTED = "Contacts are already sorted by name.";

    private static final Comparator<Person> NAME_ORDER = new PersonNameComparator();
    private static final Logger logger = LogsCenter.getLogger(SortCommand.class);

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        List<Person> contacts = model.getAddressBook().getPersonList();
        if (contacts.isEmpty()) {
            throw new CommandException(ListCommand.MESSAGE_NO_CONTACTS);
        }
        if (isSorted(contacts)) {
            throw new CommandException(MESSAGE_ALREADY_SORTED);
        }

        model.sortPersons(NAME_ORDER);
        assert isSorted(model.getAddressBook().getPersonList()) : "contacts must be in name order after sorting";
        logger.fine("Sorted " + contacts.size() + " contact(s) by name");
        return new CommandResult(String.format(MESSAGE_SUCCESS, contacts.size()));
    }

    /**
     * Returns true if every contact is ordered no later than the one after it.
     */
    private static boolean isSorted(List<Person> contacts) {
        for (int i = 1; i < contacts.size(); i++) {
            if (NAME_ORDER.compare(contacts.get(i - 1), contacts.get(i)) > 0) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns true as this command changes the order of the saved contacts.
     */
    @Override
    public boolean isUndoable() {
        return true;
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof SortCommand;
    }

    @Override
    public int hashCode() {
        return SortCommand.class.hashCode();
    }
}
