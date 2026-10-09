package seedu.address.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.model.Model.PREDICATE_SHOW_ALL_PERSONS;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.BENSON;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.GuiSettings;
import seedu.address.model.person.NameContainsKeywordsPredicate;
import seedu.address.testutil.AddressBookBuilder;

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
    public void hasPerson_nullPerson_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> modelManager.hasPerson(null));
    }

    @Test
    public void hasPerson_personNotInAddressBook_returnsFalse() {
        assertFalse(modelManager.hasPerson(ALICE));
    }

    @Test
    public void hasPerson_personInAddressBook_returnsTrue() {
        modelManager.addPerson(ALICE);
        assertTrue(modelManager.hasPerson(ALICE));
    }

    @Test
    public void getFilteredPersonList_modifyList_throwsUnsupportedOperationException() {
        assertThrows(UnsupportedOperationException.class, () -> modelManager.getFilteredPersonList().remove(0));
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
    public void saveUndoState_existingState_replacesState() {
        modelManager.saveUndoState(new AddressBookBuilder().withPerson(ALICE).build());
        modelManager.saveUndoState(new AddressBookBuilder().withPerson(BENSON).build());
        modelManager.undo();

        assertEquals(List.of(BENSON), modelManager.getAddressBook().getPersonList());
        assertFalse(modelManager.canUndo());
        assertThrows(IllegalStateException.class, () -> modelManager.undo());
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
