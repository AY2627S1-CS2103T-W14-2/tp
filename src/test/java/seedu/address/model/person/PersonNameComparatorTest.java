package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPersons.ALICE;

import org.junit.jupiter.api.Test;

import seedu.address.testutil.PersonBuilder;

public class PersonNameComparatorTest {

    private final PersonNameComparator comparator = new PersonNameComparator();

    @Test
    public void compare_differentNames_alphabeticalIgnoringCase() {
        assertOrdered("alice", "Bob");
        assertOrdered("Alex Tan", "alex yeoh");
    }

    @Test
    public void compare_namesWithSpacesAndPunctuation_characterByCharacter() {
        assertOrdered("Alex  Tan", "Alex Tan"); // a second space comes before a letter
        assertOrdered("O'Connor", "Oliver"); // an apostrophe comes before a letter
    }

    @Test
    public void compare_namesWithNumbers_numbersOrderedByValue() {
        assertOrdered("Alex 2", "Alex 10"); // as documented in the user guide
        assertOrdered("Alex 10", "Alex Yeoh");
        assertOrdered("Group 9", "Group 10");
        assertOrdered("Group 10", "Group 100");
        assertOrdered("Team 2 Alpha", "Team 2 Beta");
        assertOrdered("Team 2 Beta", "Team 10 Alpha");
        assertOrdered("2 Alex", "10 Alex");
    }

    @Test
    public void compare_numbersLongerThanLong_noOverflow() {
        assertOrdered("Alex " + "9".repeat(19), "Alex 1" + "0".repeat(19));
        assertOrdered("A" + "9".repeat(58), "A1" + "0".repeat(58)); // longest valid name
    }

    @Test
    public void compare_sameNumberWithLeadingZeros_orderedByExactName() {
        assertEquals(0, PersonNameComparator.compareNaturally("bond 007", "bond 7"));
        assertOrdered("Bond 007", "Bond 7");
        assertOrdered("Bond 7", "Bond 08");
    }

    @Test
    public void compare_namesDifferingOnlyInCase_orderedByExactName() {
        assertOrdered("Alex Tan", "alex tan");
    }

    @Test
    public void compare_sameName_orderedBySavedPhone() {
        Person lowerPhone = new PersonBuilder(ALICE).withPhone("+6581112222").withEmail("other@example.com")
                .withTelegramUsername("other_alice").build();
        Person legacyLocalPhone = new PersonBuilder(ALICE).withPhone("91234567").withEmail("legacy@example.com")
                .withTelegramUsername("legacy_alice").build();

        assertOrdered(lowerPhone, ALICE);
        // saved text is compared, so an international number comes before a legacy local one
        assertOrdered(ALICE, legacyLocalPhone);
    }

    @Test
    public void compare_samePerson_returnsZero() {
        assertEquals(0, comparator.compare(ALICE, new PersonBuilder(ALICE).build()));
    }

    @Test
    public void compare_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> comparator.compare(null, ALICE));
        assertThrows(NullPointerException.class, () -> comparator.compare(ALICE, null));
    }

    /**
     * Asserts that a person named {@code firstName} comes before one named {@code secondName}.
     */
    private void assertOrdered(String firstName, String secondName) {
        assertOrdered(PersonBuilder.named(firstName).build(), PersonBuilder.named(secondName).build());
    }

    /**
     * Asserts that {@code first} comes before {@code second}, in both directions of comparison.
     */
    private void assertOrdered(Person first, Person second) {
        assertTrue(comparator.compare(first, second) < 0);
        assertTrue(comparator.compare(second, first) > 0);
    }
}
