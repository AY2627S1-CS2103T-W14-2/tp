package seedu.address.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.logic.commands.CommandTestUtil.VALID_PHONE_BOB;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.ConflictTestUtil.ALICE_NAMESAKE;
import static seedu.address.testutil.ConflictTestUtil.clashesWith;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.BOB;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import seedu.address.model.person.Person;
import seedu.address.model.person.exceptions.DuplicatePersonException;
import seedu.address.testutil.ConflictTestUtil.Clash;
import seedu.address.testutil.PersonBuilder;

public class AddressBookTest {

    private final AddressBook addressBook = new AddressBook();

    @Test
    public void constructor() {
        assertEquals(List.of(), addressBook.getPersonList());
    }

    @Test
    public void resetData_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> addressBook.resetData(null));
    }

    @Test
    public void resetData_withValidReadOnlyAddressBook_replacesData() {
        AddressBook newData = getTypicalAddressBook();
        addressBook.resetData(newData);
        assertEquals(newData, addressBook);
    }

    @Test
    public void resetData_withClashingPersons_throwsDuplicatePersonException() {
        for (Clash clash : clashesWith(ALICE, BOB)) {
            AddressBookStub newData = new AddressBookStub(List.of(ALICE, clash.person()));
            assertThrows(DuplicatePersonException.class, clash.expectedMessage(), () ->
                    addressBook.resetData(newData));
        }
    }

    @Test
    public void resetData_withSameNameDistinctDetails_replacesData() {
        AddressBookStub newData = new AddressBookStub(List.of(ALICE, ALICE_NAMESAKE));
        addressBook.resetData(newData);
        assertEquals(List.of(ALICE, ALICE_NAMESAKE), addressBook.getPersonList());
    }

    @Test
    public void findConflict_nullPerson_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> addressBook.findConflict(null));
    }

    @Test
    public void findConflict_personNotInAddressBook_returnsEmpty() {
        assertEquals(Optional.empty(), addressBook.findConflict(ALICE));
    }

    @Test
    public void findConflict_sameNameDistinctDetails_returnsEmpty() {
        addressBook.addPerson(ALICE);
        assertEquals(Optional.empty(), addressBook.findConflict(ALICE_NAMESAKE));
    }

    @Test
    public void findConflict_clashingPerson_returnsConflict() {
        addressBook.addPerson(ALICE);
        for (Clash clash : clashesWith(ALICE, BOB)) {
            assertEquals(Optional.of(clash.expectedConflict()), addressBook.findConflict(clash.person()));
        }
    }

    @Test
    public void findConflict_excludingClashingPerson_returnsEmpty() {
        addressBook.addPerson(ALICE);
        Person editedAlice = new PersonBuilder(ALICE).withPhone(VALID_PHONE_BOB).build();
        assertEquals(Optional.empty(), addressBook.findConflict(editedAlice, ALICE));
    }

    @Test
    public void setPerson_editedPersonTakesAnotherPersonsDetail_throwsDuplicatePersonException() {
        addressBook.addPerson(ALICE);
        addressBook.addPerson(BOB);
        for (Clash clash : clashesWith(BOB, ALICE)) {
            assertThrows(DuplicatePersonException.class, clash.expectedMessage(), () ->
                    addressBook.setPerson(ALICE, clash.person()));
        }
    }

    @Test
    public void getPersonList_modifyList_throwsUnsupportedOperationException() {
        assertThrows(UnsupportedOperationException.class, () -> addressBook.getPersonList().remove(0));
    }

    @Test
    public void toStringMethod() {
        String expected = AddressBook.class.getCanonicalName() + "{persons=" + addressBook.getPersonList() + "}";
        assertEquals(expected, addressBook.toString());
    }

    /**
     * A stub ReadOnlyAddressBook whose persons list can violate interface constraints.
     */
    private static class AddressBookStub implements ReadOnlyAddressBook {
        private final ObservableList<Person> persons = FXCollections.observableArrayList();

        AddressBookStub(Collection<Person> persons) {
            this.persons.setAll(persons);
        }

        @Override
        public ObservableList<Person> getPersonList() {
            return persons;
        }
    }

}
