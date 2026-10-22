package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static seedu.address.testutil.ConflictTestUtil.ALICE_NAMESAKE;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.BENSON;

import java.util.Comparator;
import java.util.List;

import org.junit.jupiter.api.Test;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import seedu.address.model.person.Person;
import seedu.address.testutil.PersonBuilder;

public class SearchHistoryTest {
    private final ObservableList<Person> persons = FXCollections.observableArrayList(ALICE, BENSON);
    private final SearchHistory history = new SearchHistory(persons);

    @Test
    public void record_repeatedSearches_choosesFrequencyThenRecency() {
        assertNull(history.mostSearchedPersonProperty().get());
        assertEquals("", history.recentSearchProperty().get());
        history.record("Alice", List.of(ALICE));
        history.record("Alice", List.of(ALICE));
        history.record("Benson", List.of(BENSON));
        assertEquals(ALICE, history.mostSearchedPersonProperty().get());
        history.record("Benson", List.of(BENSON));
        assertEquals(BENSON, history.mostSearchedPersonProperty().get());
        assertEquals("Benson", history.recentSearchProperty().get());
    }

    @Test
    public void record_multipleMatches_countsEachPersonOnceAndBreaksTieByResultOrder() {
        history.record("a", List.of(ALICE, BENSON));
        assertEquals(ALICE, history.mostSearchedPersonProperty().get());
        history.record("Benson", List.of(BENSON));
        assertEquals(BENSON, history.mostSearchedPersonProperty().get());
        history.record("missing", List.of());
        assertEquals(BENSON, history.mostSearchedPersonProperty().get());
        assertEquals("missing", history.recentSearchProperty().get());
    }

    @Test
    public void contactChanges_updatesWinnerAndRemovesDeletedContacts() {
        history.record("Alice", List.of(ALICE));
        history.record("Alice", List.of(ALICE));
        history.record("Benson", List.of(BENSON));
        Person renamed = new PersonBuilder(ALICE).withName("Alicia").build();
        persons.set(0, renamed);
        assertEquals(renamed, history.mostSearchedPersonProperty().get());
        persons.remove(renamed);
        assertEquals(BENSON, history.mostSearchedPersonProperty().get());
        persons.clear();
        assertNull(history.mostSearchedPersonProperty().get());
    }

    @Test
    public void contactChanges_editDoesNotMakeOlderSearchWinTie() {
        history.record("Alice", List.of(ALICE));
        history.record("Benson", List.of(BENSON));
        Person renamed = new PersonBuilder(ALICE).withName("Alicia").build();
        persons.set(0, renamed);
        assertEquals(BENSON, history.mostSearchedPersonProperty().get());
        history.record("Alicia", List.of(renamed));
        assertEquals(renamed, history.mostSearchedPersonProperty().get());
    }

    @Test
    public void contactChanges_editPreservesOrderForSameSearchTie() {
        history.record("a", List.of(ALICE, BENSON));
        persons.set(1, new PersonBuilder(BENSON).withName("Ben").build());
        assertEquals(ALICE, history.mostSearchedPersonProperty().get());
    }

    @Test
    public void contactChanges_reorderingUsesCurrentContactOrderForSameSearchTie() {
        history.record("a", List.of(ALICE, BENSON));
        FXCollections.sort(persons, Comparator.comparing(person -> person.getName().fullName,
                Comparator.reverseOrder()));
        assertEquals(BENSON, history.mostSearchedPersonProperty().get());
    }

    @Test
    public void contactChanges_snapshotRestorationPreservesEditedContactCountAndRecency() {
        history.record("Alice", List.of(ALICE));
        history.record("Alice", List.of(ALICE));
        history.record("Benson", List.of(BENSON));
        Person renamed = new PersonBuilder(ALICE).withName("Alicia").build();
        persons.set(0, renamed);
        persons.setAll(ALICE, BENSON);
        assertEquals(ALICE, history.mostSearchedPersonProperty().get());
        history.record("Benson", List.of(BENSON));
        assertEquals(BENSON, history.mostSearchedPersonProperty().get());
    }

    @Test
    public void record_namesakesHaveIndependentCounts() {
        persons.add(ALICE_NAMESAKE);
        history.record("Alice", List.of(ALICE, ALICE_NAMESAKE));
        assertEquals(ALICE, history.mostSearchedPersonProperty().get());
        persons.remove(ALICE);
        assertEquals(ALICE_NAMESAKE, history.mostSearchedPersonProperty().get());
        history.record("Benson", List.of(BENSON));
        assertEquals(BENSON, history.mostSearchedPersonProperty().get());
    }

    @Test
    public void record_noMatchesDoesNotCreateWinnerOrChangeTieRecency() {
        history.record("missing", List.of());
        assertNull(history.mostSearchedPersonProperty().get());
        assertEquals("missing", history.recentSearchProperty().get());
        history.record("Alice", List.of(ALICE));
        history.record("Benson", List.of(BENSON));
        history.record("missing again", List.of());
        assertEquals(BENSON, history.mostSearchedPersonProperty().get());
    }

    @Test
    public void contactChanges_deletedThenReaddedStartsWithNoSearchCount() {
        history.record("Alice", List.of(ALICE));
        persons.remove(ALICE);
        persons.add(ALICE);
        assertNull(history.mostSearchedPersonProperty().get());
        history.record("Benson", List.of(BENSON));
        assertEquals(BENSON, history.mostSearchedPersonProperty().get());
    }

    @Test
    public void newHistory_resetsSessionStatistics() {
        history.record("Alice", List.of(ALICE));
        SearchHistory freshHistory = new SearchHistory(persons);
        assertNull(freshHistory.mostSearchedPersonProperty().get());
        assertEquals("", freshHistory.recentSearchProperty().get());
    }
}
