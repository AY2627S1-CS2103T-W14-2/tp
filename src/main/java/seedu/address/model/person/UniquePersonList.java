package seedu.address.model.person;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import seedu.address.model.person.exceptions.DuplicatePersonException;
import seedu.address.model.person.exceptions.PersonNotFoundException;

/**
 * A list of persons in which no two persons conflict, and which does not allow nulls.
 * Two persons conflict if they share a phone number, email or Telegram username, as defined by
 * {@link Person#conflictsWith(Person)}. Persons with the same name are allowed.
 * Adding and updating persons checks for conflicts, while finding and removing a person uses
 * {@link Person#equals(Object)}, so that the person with exactly the same fields is affected.
 *
 * Supports a minimal set of list operations.
 *
 * @see Person#conflictsWith(Person)
 */
public class UniquePersonList implements Iterable<Person> {

    private final ObservableList<Person> internalList = FXCollections.observableArrayList();
    private final ObservableList<Person> internalUnmodifiableList =
            FXCollections.unmodifiableObservableList(internalList);

    /**
     * Returns the first conflict between {@code candidate} and a person in the list, if any.
     */
    public Optional<ContactConflict> findConflict(Person candidate) {
        requireNonNull(candidate);
        return findFirstConflict(candidate, internalList.stream());
    }

    /**
     * Returns the first conflict between {@code candidate} and a person in the list other than {@code excluded},
     * if any. Use this when {@code candidate} is an edited version of {@code excluded}.
     */
    public Optional<ContactConflict> findConflict(Person candidate, Person excluded) {
        requireAllNonNull(candidate, excluded);
        return findFirstConflict(candidate, internalList.stream().filter(person -> !person.equals(excluded)));
    }

    /**
     * Adds a person to the list.
     * The person must not conflict with any person in the list.
     *
     * @throws DuplicatePersonException if {@code toAdd} conflicts with a person in the list.
     */
    public void add(Person toAdd) {
        requireNonNull(toAdd);
        throwIfPresent(findConflict(toAdd));
        internalList.add(toAdd);
    }

    /**
     * Replaces the person {@code target} in the list with {@code editedPerson}.
     * {@code target} must exist in the list.
     * {@code editedPerson} must not conflict with any person in the list other than {@code target}.
     *
     * @throws DuplicatePersonException if {@code editedPerson} conflicts with another person in the list.
     */
    public void setPerson(Person target, Person editedPerson) {
        requireAllNonNull(target, editedPerson);

        int index = internalList.indexOf(target);
        if (index == -1) {
            throw new PersonNotFoundException();
        }

        throwIfPresent(findConflict(editedPerson, target));
        internalList.set(index, editedPerson);
    }

    /**
     * Removes the equivalent person from the list.
     * The person must exist in the list.
     */
    public void remove(Person toRemove) {
        requireNonNull(toRemove);
        if (!internalList.remove(toRemove)) {
            throw new PersonNotFoundException();
        }
    }

    public void setPersons(UniquePersonList replacement) {
        requireNonNull(replacement);
        internalList.setAll(replacement.internalList);
    }

    /**
     * Replaces the contents of this list with {@code persons}.
     * No two persons in {@code persons} may conflict.
     *
     * @throws DuplicatePersonException if two persons in {@code persons} conflict.
     */
    public void setPersons(List<Person> persons) {
        requireAllNonNull(persons);
        throwIfPresent(findConflictWithin(persons));
        internalList.setAll(persons);
    }

    /**
     * Returns the backing list as an unmodifiable {@code ObservableList}.
     */
    public ObservableList<Person> asUnmodifiableObservableList() {
        return internalUnmodifiableList;
    }

    @Override
    public Iterator<Person> iterator() {
        return internalList.iterator();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof UniquePersonList otherUniquePersonList)) {
            return false;
        }

        return internalList.equals(otherUniquePersonList.internalList);
    }

    @Override
    public int hashCode() {
        return internalList.hashCode();
    }

    @Override
    public String toString() {
        return internalList.toString();
    }

    private static Optional<ContactConflict> findFirstConflict(Person candidate, Stream<Person> existingPersons) {
        return existingPersons.map(candidate::findConflictWith).flatMap(Optional::stream).findFirst();
    }

    /**
     * Returns the first conflict between two persons in {@code persons}, reported against the earlier person.
     */
    private static Optional<ContactConflict> findConflictWithin(List<Person> persons) {
        for (int i = 1; i < persons.size(); i++) {
            Optional<ContactConflict> conflict = findFirstConflict(persons.get(i), persons.subList(0, i).stream());
            if (conflict.isPresent()) {
                return conflict;
            }
        }
        return Optional.empty();
    }

    private static void throwIfPresent(Optional<ContactConflict> conflict) {
        conflict.ifPresent(presentConflict -> {
            throw new DuplicatePersonException(presentConflict);
        });
    }
}
