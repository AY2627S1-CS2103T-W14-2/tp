package seedu.address.model;

import java.util.Optional;
import java.util.function.Predicate;

import javafx.collections.ObservableList;
import seedu.address.commons.core.GuiSettings;
import seedu.address.model.person.ContactConflict;
import seedu.address.model.person.Person;

/**
 * The API of the Model component.
 */
public interface Model {
    /** {@code Predicate} that always evaluates to true */
    Predicate<Person> PREDICATE_SHOW_ALL_PERSONS = unused -> true;

    /**
     * Returns the user prefs.
     */
    ReadOnlyUserPrefs getUserPrefs();

    /**
     * Returns the user prefs' GUI settings.
     */
    GuiSettings getGuiSettings();

    /**
     * Sets the user prefs' GUI settings.
     */
    void setGuiSettings(GuiSettings guiSettings);

    /**
     * Replaces address book data with the data in {@code addressBook}.
     */
    void setAddressBook(ReadOnlyAddressBook addressBook);

    /** Returns the AddressBook */
    ReadOnlyAddressBook getAddressBook();

    /**
     * Returns the first conflict between {@code candidate} and a person in the address book, if any.
     * Two persons conflict if they share a phone number, email or Telegram username.
     */
    Optional<ContactConflict> findConflict(Person candidate);

    /**
     * Returns the first conflict between {@code candidate} and a person in the address book other than
     * {@code excluded}, if any. Use this when {@code candidate} is an edited version of {@code excluded}.
     */
    Optional<ContactConflict> findConflict(Person candidate, Person excluded);

    /**
     * Deletes the given person.
     * The person must exist in the address book.
     */
    void deletePerson(Person target);

    /**
     * Adds the given person.
     * {@code person} must not conflict with any person in the address book.
     */
    void addPerson(Person person);

    /**
     * Replaces the given person {@code target} with {@code editedPerson}.
     * {@code target} must exist in the address book.
     * {@code editedPerson} must not conflict with any person in the address book other than {@code target}.
     */
    void setPerson(Person target, Person editedPerson);

    /** Returns an unmodifiable view of the filtered person list */
    ObservableList<Person> getFilteredPersonList();

    /**
     * Updates the filter of the filtered person list to filter by the given {@code predicate}.
     * @throws NullPointerException if {@code predicate} is null.
     */
    void updateFilteredPersonList(Predicate<Person> predicate);

    /**
     * Saves a copy of {@code previousState} for undo, replacing any existing saved state.
     * @throws NullPointerException if {@code previousState} is null.
     */
    void saveUndoState(ReadOnlyAddressBook previousState);

    /**
     * Returns true if there is a saved address book state to restore.
     */
    boolean canUndo();

    /**
     * Restores the saved address book state and shows all persons.
     * The saved state is discarded after undoing.
     * @throws IllegalStateException if there is no saved state to restore.
     */
    void undo();
}
