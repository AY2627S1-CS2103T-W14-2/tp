package seedu.address.logic;

import javafx.beans.property.ReadOnlyStringProperty;
import javafx.collections.ObservableList;
import seedu.address.commons.core.GuiSettings;
import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.Person;

/**
 * API of the Logic component
 */
public interface Logic {
    /**
     * Executes the command and returns the result.
     * @param commandText The command as entered by the user.
     * @return the result of the command execution.
     * @throws CommandException If an error occurs during command execution.
     * @throws ParseException If an error occurs during parsing.
     */
    CommandResult execute(String commandText) throws CommandException, ParseException;

    /** Returns an unmodifiable view of the filtered list of persons */
    ObservableList<Person> getFilteredPersonList();

    /**
     * Returns an unmodifiable, observable view of persons matching the latest name or project search.
     * The list is empty before the first search and stays in sync with contact changes.
     * Showing all persons does not replace the recent search results.
     */
    ObservableList<Person> getRecentFindPersonList();

    /**
     * Returns a read-only, observable property containing the latest executed search command.
     * The value includes the command word, with surrounding whitespace trimmed, and is empty before the first search.
     * Other commands and rejected search input leave the value unchanged.
     */
    ReadOnlyStringProperty recentFindSearchProperty();

    /**
     * Returns the user prefs' GUI settings.
     */
    GuiSettings getGuiSettings();

    /**
     * Set the user prefs' GUI settings.
     */
    void setGuiSettings(GuiSettings guiSettings);
}
