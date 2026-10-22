package seedu.address.logic;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import seedu.address.model.person.Person;

/**
 * Session search history. Each person returned by a name or project search earns one search;
 * ties favour the most recently searched person, then address book order.
 */
public class SearchHistory {
    private final Map<Person, SearchCount> counts = new HashMap<>();
    private final ObservableList<Person> persons;
    private long searchSequence;
    private final ReadOnlyObjectWrapper<Person> mostSearchedPerson = new ReadOnlyObjectWrapper<>();

    /**
     * Tracks contact changes so summaries never show deleted or outdated contacts.
     */
    public SearchHistory(ObservableList<Person> persons) {
        this.persons = persons;
        persons.addListener((ListChangeListener<Person>) change -> {
            while (change.next()) {
                if (change.wasReplaced() && change.getRemovedSize() == change.getAddedSize()) {
                    // Undo restores a full snapshot, while ordinary edits replace one list element.
                    List<? extends Person> removed = change.getRemoved();
                    List<? extends Person> added = change.getAddedSubList();
                    for (int i = 0; i < removed.size(); i++) {
                        if (!added.contains(removed.get(i)) && !removed.contains(added.get(i))) {
                            SearchCount count = counts.remove(removed.get(i));
                            if (count != null) {
                                counts.put(added.get(i), count);
                            }
                        }
                    }
                }
            }
            counts.keySet().retainAll(persons);
            updateMostSearchedPerson();
        });
    }

    /**
     * Records a successful name or project search, including searches with no matches.
     */
    public void record(List<Person> matches) {
        searchSequence++;
        for (Person person : matches) {
            SearchCount previous = counts.get(person);
            counts.put(person, new SearchCount(previous == null ? 1 : previous.count() + 1, searchSequence));
        }
        updateMostSearchedPerson();
    }

    private void updateMostSearchedPerson() {
        Person winner = null;
        long highestCount = 0;
        long latestSearch = 0;
        // Iterating current contact order makes ties deterministic even after edits or reordering.
        for (Person person : persons) {
            SearchCount count = counts.get(person);
            if (count != null && (count.count() > highestCount
                    || count.count() == highestCount && count.lastSearch() > latestSearch)) {
                winner = person;
                highestCount = count.count();
                latestSearch = count.lastSearch();
            }
        }
        mostSearchedPerson.set(winner);
    }

    /** Returns the single winning contact, or null when no current contact has been searched. */
    public ReadOnlyObjectProperty<Person> mostSearchedPersonProperty() {
        return mostSearchedPerson.getReadOnlyProperty();
    }

    /** A contact's frequency and actual search recency, independent of contact edits. */
    private record SearchCount(long count, long lastSearch) {}
}
