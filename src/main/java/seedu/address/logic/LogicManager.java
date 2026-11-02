package seedu.address.logic;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.util.function.Predicate;
import java.util.logging.Logger;

import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.ObservableList;
import seedu.address.commons.core.GuiSettings;
import seedu.address.commons.core.LogsCenter;
import seedu.address.logic.commands.Command;
import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.commands.FindCommand;
import seedu.address.logic.commands.FindProjectCommand;
import seedu.address.logic.commands.UndoCommand;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.AddressBookParser;
import seedu.address.logic.parser.FindCommandParser;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.person.Person;
import seedu.address.storage.Storage;

/**
 * The main LogicManager of the app.
 */
public class LogicManager implements Logic {
    public static final String FILE_OPS_ERROR_FORMAT = "Could not save data due to the following error: %s"
            + "\nThe change was not saved. Check available storage, file and folder permissions,"
            + " and whether another program is using the data file, then retry the command.";

    public static final String FILE_OPS_PERMISSION_ERROR_FORMAT =
            "Could not save data to file %s. Access was denied."
            + "\nThe change was not saved. Check file and folder permissions and close other programs"
            + " using the data file, then retry the command.";

    public static final String FILE_OPS_ATOMIC_MOVE_ERROR =
            "Could not save data because this location does not support atomic file replacement."
            + "\nThe change was not saved. Use a data location that supports atomic replacement.";

    private final Logger logger = LogsCenter.getLogger(LogicManager.class);

    private final Model model;
    private final Storage storage;
    private final AddressBookParser addressBookParser;
    private final ReadOnlyStringWrapper recentSearch = new ReadOnlyStringWrapper("");
    private final SearchHistory searchHistory;

    /**
     * Constructs a {@code LogicManager} with the given {@code Model} and {@code Storage}.
     */
    public LogicManager(Model model, Storage storage) {
        this.model = model;
        this.storage = storage;
        addressBookParser = new AddressBookParser();
        searchHistory = new SearchHistory(model.getAddressBook().getPersonList());
    }

    @Override
    public CommandResult execute(String commandText) throws CommandException, ParseException {
        Command command = addressBookParser.parseCommand(commandText);
        logger.info("----------------[USER COMMAND][" + command.getClass().getSimpleName() + "]");
        if (command instanceof UndoCommand && model.canUndo()) {
            // Persist the restored state before changing contacts, the filter or undo history.
            saveAddressBook(model.getUndoState());
            return command.execute(model);
        }
        CommandResult commandResult = command.isUndoable() ? executeUndoableCommand(command) : command.execute(model);
        if (command instanceof FindCommand || command instanceof FindProjectCommand) {
            updateSearchHistory(command, commandText);
        }
        return commandResult;
    }

    private CommandResult executeUndoableCommand(Command command) throws CommandException {
        AddressBook before = new AddressBook(model.getAddressBook());
        Predicate<Person> filteredPersonPredicate = model.getFilteredPersonPredicate();
        SearchHistory.Snapshot searchHistoryBefore = searchHistory.snapshot();
        CommandResult commandResult = command.execute(model);
        if (commandResult.isDataChanged()) {
            try {
                saveAddressBook(model.getAddressBook());
            } catch (CommandException e) {
                // Restore all observable state changed by the command before reporting the failed save.
                model.setAddressBook(before);
                model.updateFilteredPersonList(filteredPersonPredicate);
                searchHistory.restore(searchHistoryBefore);
                throw e;
            }
            model.saveUndoState(before);
        }
        return commandResult;
    }

    private void updateSearchHistory(Command command, String commandText) {
        String commandWord = command instanceof FindCommand
                ? FindCommand.COMMAND_WORD : FindProjectCommand.COMMAND_WORD;
        String searchType = command instanceof FindCommand ? "Name: " : "Project: ";
        recentSearch.set(searchType + FindCommandParser.trimSearchInput(
                commandText.trim().substring(commandWord.length())));
        searchHistory.record(model.getFilteredPersonList());
    }

    private void saveAddressBook(ReadOnlyAddressBook addressBook) throws CommandException {
        try {
            storage.saveAddressBook(addressBook);
        } catch (AtomicMoveNotSupportedException e) {
            throw new CommandException(FILE_OPS_ATOMIC_MOVE_ERROR, e);
        } catch (AccessDeniedException e) {
            throw new CommandException(String.format(FILE_OPS_PERMISSION_ERROR_FORMAT, e.getMessage()), e);
        } catch (IOException ioe) {
            throw new CommandException(String.format(FILE_OPS_ERROR_FORMAT, ioe.getMessage()), ioe);
        }
    }

    @Override
    public ObservableList<Person> getFilteredPersonList() {
        return model.getFilteredPersonList();
    }

    /**
     * Returns the model's live view of persons matching the latest search.
     */
    @Override
    public ObservableList<Person> getRecentFindPersonList() {
        return model.getRecentFindList();
    }

    /**
     * Returns a read-only view of the search text so callers can observe changes without modifying it.
     */
    @Override
    public ReadOnlyStringProperty recentSearchProperty() {
        return recentSearch.getReadOnlyProperty();
    }

    @Override
    public ReadOnlyObjectProperty<Person> mostSearchedPersonProperty() {
        return searchHistory.mostSearchedPersonProperty();
    }

    @Override
    public GuiSettings getGuiSettings() {
        return model.getGuiSettings();
    }

    @Override
    public void setGuiSettings(GuiSettings guiSettings) {
        model.setGuiSettings(guiSettings);
    }
}
