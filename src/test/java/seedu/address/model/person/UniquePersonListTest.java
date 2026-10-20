package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.logic.commands.CommandTestUtil.VALID_PHONE_BOB;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.ConflictTestUtil.ALICE_NAMESAKE;
import static seedu.address.testutil.ConflictTestUtil.clashesWith;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.BOB;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import seedu.address.model.person.exceptions.DuplicatePersonException;
import seedu.address.model.person.exceptions.PersonNotFoundException;
import seedu.address.testutil.ConflictTestUtil.Clash;
import seedu.address.testutil.PersonBuilder;

public class UniquePersonListTest {

    private final UniquePersonList uniquePersonList = new UniquePersonList();

    @Test
    public void findConflict_nullPerson_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> uniquePersonList.findConflict(null));
        assertThrows(NullPointerException.class, () -> uniquePersonList.findConflict(ALICE, null));
        assertThrows(NullPointerException.class, () -> uniquePersonList.findConflict(null, ALICE));
    }

    @Test
    public void findConflict_emptyList_returnsEmpty() {
        assertEquals(Optional.empty(), uniquePersonList.findConflict(ALICE));
    }

    @Test
    public void findConflict_clashingPerson_returnsConflict() {
        uniquePersonList.add(ALICE);
        for (Clash clash : clashesWith(ALICE, BOB)) {
            assertEquals(Optional.of(clash.expectedConflict()), uniquePersonList.findConflict(clash.person()));
        }
    }

    @Test
    public void findConflict_excludedPersonOnlyClash_returnsEmpty() {
        uniquePersonList.add(ALICE);
        assertEquals(Optional.empty(), uniquePersonList.findConflict(ALICE, ALICE));
    }

    @Test
    public void add_nullPerson_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> uniquePersonList.add(null));
    }

    @Test
    public void add_samePersonTwice_throwsDuplicatePersonException() {
        uniquePersonList.add(ALICE);
        assertThrows(DuplicatePersonException.class, () -> uniquePersonList.add(ALICE));
    }

    @Test
    public void add_sameNameDistinctDetails_success() {
        uniquePersonList.add(ALICE);
        uniquePersonList.add(ALICE_NAMESAKE);
        assertEquals(List.of(ALICE, ALICE_NAMESAKE), uniquePersonList.asUnmodifiableObservableList());
    }

    @Test
    public void add_differentNameSharedDetail_throwsDuplicatePersonException() {
        uniquePersonList.add(ALICE);
        for (Clash clash : clashesWith(ALICE, BOB)) {
            assertThrows(DuplicatePersonException.class, clash.expectedMessage(), () ->
                    uniquePersonList.add(clash.person()));
        }
    }

    @Test
    public void setPerson_nullTargetPerson_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> uniquePersonList.setPerson(null, ALICE));
    }

    @Test
    public void setPerson_nullEditedPerson_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> uniquePersonList.setPerson(ALICE, null));
    }

    @Test
    public void setPerson_targetPersonNotInList_throwsPersonNotFoundException() {
        assertThrows(PersonNotFoundException.class, () -> uniquePersonList.setPerson(ALICE, ALICE));
    }

    @Test
    public void setPerson_editedPersonIsSamePerson_success() {
        uniquePersonList.add(ALICE);
        uniquePersonList.setPerson(ALICE, ALICE);
        UniquePersonList expectedUniquePersonList = new UniquePersonList();
        expectedUniquePersonList.add(ALICE);
        assertEquals(expectedUniquePersonList, uniquePersonList);
    }

    @Test
    public void setPerson_editedPersonKeepsOwnEmailAndTelegram_success() {
        uniquePersonList.add(ALICE);
        Person editedAlice = new PersonBuilder(ALICE).withPhone(VALID_PHONE_BOB)
                .build();
        uniquePersonList.setPerson(ALICE, editedAlice);
        UniquePersonList expectedUniquePersonList = new UniquePersonList();
        expectedUniquePersonList.add(editedAlice);
        assertEquals(expectedUniquePersonList, uniquePersonList);
    }

    @Test
    public void setPerson_editedPersonHasDifferentIdentity_success() {
        uniquePersonList.add(ALICE);
        uniquePersonList.setPerson(ALICE, BOB);
        UniquePersonList expectedUniquePersonList = new UniquePersonList();
        expectedUniquePersonList.add(BOB);
        assertEquals(expectedUniquePersonList, uniquePersonList);
    }

    @Test
    public void setPerson_editedPersonIsAnotherPerson_throwsDuplicatePersonException() {
        uniquePersonList.add(ALICE);
        uniquePersonList.add(BOB);
        assertThrows(DuplicatePersonException.class, () -> uniquePersonList.setPerson(ALICE, BOB));
    }

    @Test
    public void setPerson_editedPersonTakesAnotherPersonsDetail_throwsDuplicatePersonException() {
        // Regression: the edited person still shares its other details with the target, which must not hide the clash
        uniquePersonList.add(ALICE);
        uniquePersonList.add(BOB);
        for (Clash clash : clashesWith(BOB, ALICE)) {
            assertThrows(DuplicatePersonException.class, clash.expectedMessage(), () ->
                    uniquePersonList.setPerson(ALICE, clash.person()));
        }
        assertEquals(List.of(ALICE, BOB), uniquePersonList.asUnmodifiableObservableList());
    }

    @Test
    public void setPerson_editedPersonTakesNamesakesName_success() {
        uniquePersonList.add(ALICE);
        uniquePersonList.add(BOB);
        Person bobRenamedToAlice = new PersonBuilder(BOB).withName(ALICE.getName().fullName).build();
        uniquePersonList.setPerson(BOB, bobRenamedToAlice);
        assertEquals(List.of(ALICE, bobRenamedToAlice), uniquePersonList.asUnmodifiableObservableList());
    }

    @Test
    public void remove_nullPerson_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> uniquePersonList.remove(null));
    }

    @Test
    public void remove_personDoesNotExist_throwsPersonNotFoundException() {
        assertThrows(PersonNotFoundException.class, () -> uniquePersonList.remove(ALICE));
    }

    @Test
    public void remove_existingPerson_removesPerson() {
        uniquePersonList.add(ALICE);
        uniquePersonList.remove(ALICE);
        UniquePersonList expectedUniquePersonList = new UniquePersonList();
        assertEquals(expectedUniquePersonList, uniquePersonList);
    }

    @Test
    public void setPersons_nullUniquePersonList_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> uniquePersonList.setPersons((UniquePersonList) null));
    }

    @Test
    public void setPersons_uniquePersonList_replacesOwnListWithProvidedUniquePersonList() {
        uniquePersonList.add(ALICE);
        UniquePersonList expectedUniquePersonList = new UniquePersonList();
        expectedUniquePersonList.add(BOB);
        uniquePersonList.setPersons(expectedUniquePersonList);
        assertEquals(expectedUniquePersonList, uniquePersonList);
    }

    @Test
    public void setPersons_nullList_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> uniquePersonList.setPersons((List<Person>) null));
    }

    @Test
    public void setPersons_list_replacesOwnListWithProvidedList() {
        uniquePersonList.add(ALICE);
        List<Person> personList = List.of(BOB);
        uniquePersonList.setPersons(personList);
        UniquePersonList expectedUniquePersonList = new UniquePersonList();
        expectedUniquePersonList.add(BOB);
        assertEquals(expectedUniquePersonList, uniquePersonList);
    }

    @Test
    public void setPersons_listWithDuplicatePersons_throwsDuplicatePersonException() {
        List<Person> listWithDuplicatePersons = List.of(ALICE, ALICE);
        assertThrows(DuplicatePersonException.class, () -> uniquePersonList.setPersons(listWithDuplicatePersons));
    }

    @Test
    public void setPersons_listWithClashingPersons_throwsDuplicatePersonException() {
        for (Clash clash : clashesWith(ALICE, BOB)) {
            List<Person> clashingPersons = List.of(ALICE, clash.person());
            assertThrows(DuplicatePersonException.class, clash.expectedMessage(), () ->
                    uniquePersonList.setPersons(clashingPersons));
        }
    }

    @Test
    public void setPersons_listWithSameNameDistinctDetails_success() {
        uniquePersonList.setPersons(List.of(ALICE, ALICE_NAMESAKE));
        assertEquals(List.of(ALICE, ALICE_NAMESAKE), uniquePersonList.asUnmodifiableObservableList());
    }

    @Test
    public void remove_sameNamePair_removesOnlyEqualPerson() {
        uniquePersonList.setPersons(List.of(ALICE, ALICE_NAMESAKE));
        uniquePersonList.remove(ALICE_NAMESAKE);
        assertEquals(List.of(ALICE), uniquePersonList.asUnmodifiableObservableList());
    }

    @Test
    public void asUnmodifiableObservableList_modifyList_throwsUnsupportedOperationException() {
        assertThrows(UnsupportedOperationException.class, ()
            -> uniquePersonList.asUnmodifiableObservableList().remove(0));
    }

    @Test
    public void sortPersons_nullComparator_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> uniquePersonList.sortPersons(null));
    }

    @Test
    public void sortPersons_unsortedListWithNamesake_ordersByNameThenPhone() {
        // ALICE_NAMESAKE shares ALICE's name but has the smaller phone number
        uniquePersonList.setPersons(List.of(BOB, ALICE, ALICE_NAMESAKE));

        uniquePersonList.sortPersons(new PersonNameComparator());

        assertEquals(List.of(ALICE_NAMESAKE, ALICE, BOB), uniquePersonList.asUnmodifiableObservableList());
    }

    @Test
    public void toStringMethod() {
        assertEquals(uniquePersonList.asUnmodifiableObservableList().toString(), uniquePersonList.toString());
    }
}
