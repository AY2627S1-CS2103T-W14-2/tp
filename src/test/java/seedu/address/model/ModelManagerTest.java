package seedu.address.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.model.Model.PREDICATE_SHOW_ALL_PERSONS;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.ConflictTestUtil.ALICE_NAMESAKE;
import static seedu.address.testutil.ConflictTestUtil.clashesWith;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.BENSON;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import javafx.collections.ObservableList;
import seedu.address.commons.core.GuiSettings;
import seedu.address.model.person.ContactConflict;
import seedu.address.model.person.ContactConflict.Field;
import seedu.address.model.person.NameContainsKeywordsPredicate;
import seedu.address.model.person.Person;
import seedu.address.model.person.ProjectContainsKeywordPredicate;
import seedu.address.model.person.PersonNameComparator;
import seedu.address.model.person.exceptions.DuplicatePersonException;
import seedu.address.testutil.AddressBookBuilder;
import seedu.address.testutil.ConflictTestUtil.Clash;
import seedu.address.testutil.PersonBuilder;

public class ModelManagerTest {

    private ModelManager modelManager = new ModelManager();

    @Test
    public void constructor() {
        assertEquals(new UserPrefs(), modelManager.getUserPrefs());
        assertEquals(new GuiSettings(), modelManager.getGuiSettings());
        assertEquals(new AddressBook(), new AddressBook(modelManager.getAddressBook()));
    }

    @Test
    public void constructor_validUserPrefs_copiesUserPrefs() {
        UserPrefs userPrefs = new UserPrefs();
        userPrefs.setGuiSettings(new GuiSettings(1, 2, 3, 4));
        modelManager = new ModelManager(new AddressBook(), userPrefs);
        assertEquals(userPrefs, modelManager.getUserPrefs());

        // Modifying userPrefs should not modify modelManager's userPrefs
        UserPrefs oldUserPrefs = new UserPrefs(userPrefs);
        userPrefs.setGuiSettings(new GuiSettings(5, 6, 7, 8));
        assertEquals(oldUserPrefs, modelManager.getUserPrefs());
    }

    @Test
    public void setGuiSettings_nullGuiSettings_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> modelManager.setGuiSettings(null));
    }

    @Test
    public void setGuiSettings_validGuiSettings_setsGuiSettings() {
        GuiSettings guiSettings = new GuiSettings(1, 2, 3, 4);
        modelManager.setGuiSettings(guiSettings);
        assertEquals(guiSettings, modelManager.getGuiSettings());
    }

    @Test
    public void findConflict_nullPerson_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> modelManager.findConflict(null));
        assertThrows(NullPointerException.class, () -> modelManager.findConflict(ALICE, null));
    }

    @Test
    public void findConflict_personNotInAddressBook_returnsEmpty() {
        assertEquals(Optional.empty(), modelManager.findConflict(ALICE));
    }

    @Test
    public void findConflict_personInAddressBook_returnsConflict() {
        modelManager.addPerson(ALICE);
        assertEquals(Optional.of(new ContactConflict(Field.PHONE, ALICE.getPhone().value, ALICE)),
                modelManager.findConflict(ALICE));
    }

    @Test
    public void findConflict_excludingOnlyClashingPerson_returnsEmpty() {
        modelManager.addPerson(ALICE);
        assertEquals(Optional.empty(), modelManager.findConflict(ALICE, ALICE));
    }

    @Test
    public void addPerson_sameNameDistinctDetails_addsBoth() {
        modelManager.addPerson(ALICE);
        modelManager.addPerson(ALICE_NAMESAKE);
        assertEquals(List.of(ALICE, ALICE_NAMESAKE), modelManager.getFilteredPersonList());
    }

    @Test
    public void addPerson_clashingPerson_throwsDuplicatePersonException() {
        modelManager.addPerson(ALICE);
        for (Clash clash : clashesWith(ALICE, BENSON)) {
            assertThrows(DuplicatePersonException.class, clash.expectedMessage(), () ->
                    modelManager.addPerson(clash.person()));
        }
    }

    @Test
    public void setPerson_editedPersonTakesAnotherPersonsDetail_throwsDuplicatePersonException() {
        modelManager.addPerson(ALICE);
        modelManager.addPerson(BENSON);
        for (Clash clash : clashesWith(BENSON, ALICE)) {
            assertThrows(DuplicatePersonException.class, clash.expectedMessage(), () ->
                    modelManager.setPerson(ALICE, clash.person()));
        }
        assertEquals(List.of(ALICE, BENSON), modelManager.getFilteredPersonList());
    }

    @Test
    public void sortPersons_nullComparator_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> modelManager.sortPersons(null));
    }

    @Test
    public void sortPersons_activeFilter_keepsFilterInNewOrder() {
        modelManager.addPerson(BENSON);
        modelManager.addPerson(ALICE);
        modelManager.addPerson(ALICE_NAMESAKE);
        modelManager.updateFilteredPersonList(new NameContainsKeywordsPredicate(List.of("Alice")));

        modelManager.sortPersons(new PersonNameComparator());

        assertEquals(List.of(ALICE_NAMESAKE, ALICE, BENSON), modelManager.getAddressBook().getPersonList());
        assertEquals(List.of(ALICE_NAMESAKE, ALICE), modelManager.getFilteredPersonList());
    }

    @Test
    public void getFilteredPersonList_modifyList_throwsUnsupportedOperationException() {
        assertThrows(UnsupportedOperationException.class, () -> modelManager.getFilteredPersonList().remove(0));
    }

    @Test
    public void getRecentFindList_beforeSearch_isEmptyEvenWithSavedContacts() {
        modelManager.setAddressBook(new AddressBookBuilder().withPerson(ALICE).withPerson(BENSON).build());
        assertTrue(modelManager.getRecentFindList().isEmpty());
        modelManager.updateFilteredPersonList(PREDICATE_SHOW_ALL_PERSONS);
        assertTrue(modelManager.getRecentFindList().isEmpty());
    }

    @Test
    public void getRecentFindList_newSearchAndShowAll_updatesExistingObservableList() {
        Person alice = new PersonBuilder(ALICE).withProjects("Orbital").build();
        Person benson = new PersonBuilder(BENSON).withProjects("Team Alpha").build();
        modelManager.addPerson(alice);
        modelManager.addPerson(benson);
        ObservableList<Person> recent = modelManager.getRecentFindList();

        modelManager.updateFilteredPersonList(new NameContainsKeywordsPredicate(List.of("Alice")));
        assertEquals(List.of(alice), recent);
        modelManager.updateFilteredPersonList(new ProjectContainsKeywordPredicate("Team Alpha"));
        assertSame(recent, modelManager.getRecentFindList());
        assertEquals(List.of(benson), recent);
        modelManager.updateFilteredPersonList(PREDICATE_SHOW_ALL_PERSONS);
        assertEquals(List.of(alice, benson), modelManager.getFilteredPersonList());
        assertEquals(List.of(benson), recent);
        modelManager.updateFilteredPersonList(new NameContainsKeywordsPredicate(List.of("Nobody")));
        assertTrue(recent.isEmpty());
    }

    @Test
    public void getRecentFindList_contactChanges_updateMembershipAndDetails() {
        modelManager.addPerson(ALICE);
        modelManager.addPerson(BENSON);
        modelManager.updateFilteredPersonList(new NameContainsKeywordsPredicate(List.of("Alice")));
        modelManager.updateFilteredPersonList(PREDICATE_SHOW_ALL_PERSONS);
        ObservableList<Person> recent = modelManager.getRecentFindList();

        Person editedAlice = new PersonBuilder(ALICE).withEmail("newalice@example.com").build();
        modelManager.setPerson(ALICE, editedAlice);
        assertEquals(List.of(editedAlice), recent);
        Person renamedBenson = new PersonBuilder(BENSON).withName("Alice Benson").build();
        modelManager.setPerson(BENSON, renamedBenson);
        assertEquals(List.of(editedAlice, renamedBenson), recent);
        modelManager.setPerson(editedAlice, new PersonBuilder(editedAlice).withName("Carol Tan").build());
        assertEquals(List.of(renamedBenson), recent);
        modelManager.addPerson(ALICE_NAMESAKE);
        assertEquals(List.of(renamedBenson, ALICE_NAMESAKE), recent);
        modelManager.deletePerson(renamedBenson);
        assertEquals(List.of(ALICE_NAMESAKE), recent);
    }

    @Test
    public void getRecentFindList_clearUndoAndReload_retainsSearchOnlyWithinSession() {
        modelManager.addPerson(ALICE);
        modelManager.addPerson(BENSON);
        modelManager.updateFilteredPersonList(new NameContainsKeywordsPredicate(List.of("Alice")));
        ObservableList<Person> recent = modelManager.getRecentFindList();
        modelManager.saveUndoState(modelManager.getAddressBook());
        modelManager.setAddressBook(new AddressBook());
        assertTrue(recent.isEmpty());

        modelManager.undo();
        assertEquals(List.of(ALICE), recent);
        assertEquals(List.of(ALICE, BENSON), modelManager.getFilteredPersonList());
        Model reloaded = new ModelManager(modelManager.getAddressBook(), modelManager.getUserPrefs());
        assertTrue(reloaded.getRecentFindList().isEmpty());
    }

    @Test
    public void getRecentFindList_modifyList_throwsUnsupportedOperationException() {
        modelManager.addPerson(ALICE);
        modelManager.updateFilteredPersonList(new NameContainsKeywordsPredicate(List.of("Alice")));
        assertThrows(UnsupportedOperationException.class, () -> modelManager.getRecentFindList().remove(0));
        assertEquals(List.of(ALICE), modelManager.getRecentFindList());
    }

    @Test
    public void canUndo_noSavedState_returnsFalse() {
        assertFalse(modelManager.canUndo());
    }

    @Test
    public void saveUndoState_nullState_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> modelManager.saveUndoState(null));
    }

    @Test
    public void saveUndoState_validState_copiesState() {
        AddressBook previousState = new AddressBookBuilder().withPerson(ALICE).withPerson(BENSON).build();
        modelManager.saveUndoState(previousState);
        assertTrue(modelManager.canUndo());

        previousState.removePerson(ALICE);
        modelManager.undo();

        assertEquals(List.of(ALICE, BENSON), modelManager.getAddressBook().getPersonList());
    }

    @Test
    public void saveUndoState_existingState_preservesEarlierState() {
        modelManager.saveUndoState(new AddressBookBuilder().withPerson(ALICE).build());
        modelManager.saveUndoState(new AddressBookBuilder().withPerson(BENSON).build());
        modelManager.undo();

        assertEquals(List.of(BENSON), modelManager.getAddressBook().getPersonList());
        assertTrue(modelManager.canUndo());
        modelManager.undo();
        assertEquals(List.of(ALICE), modelManager.getAddressBook().getPersonList());
        assertFalse(modelManager.canUndo());
        assertThrows(IllegalStateException.class, () -> modelManager.undo());
    }

    @Test
    public void getUndoState_noSavedState_throwsIllegalStateException() {
        assertThrows(IllegalStateException.class, () -> modelManager.getUndoState());
    }

    @Test
    public void getUndoState_returnsCopyAndPreservesHistory() {
        AddressBook previousState = new AddressBookBuilder().withPerson(ALICE).build();
        modelManager.saveUndoState(previousState);
        AddressBook undoState = (AddressBook) modelManager.getUndoState();
        assertEquals(previousState, undoState);
        undoState.removePerson(ALICE);

        assertTrue(modelManager.canUndo());
        assertEquals(previousState, modelManager.getUndoState());
        modelManager.undo();
        assertEquals(previousState, modelManager.getAddressBook());
        assertFalse(modelManager.canUndo());
    }

    @Test
    public void undo_noSavedState_throwsIllegalStateException() {
        modelManager.addPerson(ALICE);
        assertThrows(IllegalStateException.class, () -> modelManager.undo());
        assertEquals(List.of(ALICE), modelManager.getAddressBook().getPersonList());
    }

    @Test
    public void undo_filteredList_restoresStateAndShowsAllPersons() {
        AddressBook previousState = new AddressBookBuilder().withPerson(ALICE).withPerson(BENSON).build();
        modelManager.setAddressBook(previousState);
        modelManager.saveUndoState(modelManager.getAddressBook());
        modelManager.deletePerson(ALICE);
        modelManager.updateFilteredPersonList(person -> false);

        modelManager.undo();

        assertEquals(previousState, modelManager.getAddressBook());
        assertEquals(List.of(ALICE, BENSON), modelManager.getFilteredPersonList());
        assertFalse(modelManager.canUndo());
    }

    @Test
    public void constructor_savedAddressBook_doesNotRestoreUndoState() {
        modelManager.saveUndoState(new AddressBook());
        ModelManager reloadedModel = new ModelManager(modelManager.getAddressBook(), modelManager.getUserPrefs());
        assertFalse(reloadedModel.canUndo());
    }

    @Test
    public void equals() {
        AddressBook addressBook = new AddressBookBuilder().withPerson(ALICE).withPerson(BENSON).build();
        AddressBook differentAddressBook = new AddressBook();
        UserPrefs userPrefs = new UserPrefs();

        // same values -> returns true
        modelManager = new ModelManager(addressBook, userPrefs);
        ModelManager modelManagerCopy = new ModelManager(addressBook, userPrefs);
        assertTrue(modelManager.equals(modelManagerCopy));

        // same object -> returns true
        assertTrue(modelManager.equals(modelManager));

        // null -> returns false
        assertFalse(modelManager.equals(null));

        // different types -> returns false
        assertFalse(modelManager.equals(5));

        // different addressBook -> returns false
        assertFalse(modelManager.equals(new ModelManager(differentAddressBook, userPrefs)));

        // different filteredList -> returns false
        String[] keywords = ALICE.getName().fullName.split("\\s+");
        modelManager.updateFilteredPersonList(new NameContainsKeywordsPredicate(List.of(keywords)));
        assertFalse(modelManager.equals(new ModelManager(addressBook, userPrefs)));

        // resets modelManager to initial state for upcoming tests
        modelManager.updateFilteredPersonList(PREDICATE_SHOW_ALL_PERSONS);

        // different userPrefs -> returns false
        UserPrefs differentUserPrefs = new UserPrefs();
        differentUserPrefs.setGuiSettings(new GuiSettings(1, 2, 3, 4));
        assertFalse(modelManager.equals(new ModelManager(addressBook, differentUserPrefs)));
    }
}
