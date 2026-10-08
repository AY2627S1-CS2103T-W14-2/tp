package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import java.util.Locale;

import org.junit.jupiter.api.Test;

import seedu.address.testutil.PersonBuilder;

public class ProjectContainsKeywordPredicateTest {

    @Test
    public void constructor_nullOrBlankKeyword_throwsException() {
        assertThrows(NullPointerException.class, () -> new ProjectContainsKeywordPredicate(null));
        assertThrows(IllegalArgumentException.class, () -> new ProjectContainsKeywordPredicate(""));
        assertThrows(IllegalArgumentException.class, () -> new ProjectContainsKeywordPredicate(" \t\u00a0 "));
    }

    @Test
    public void test_exactPartialAndMixedCaseMatches_returnsTrue() {
        Person person = new PersonBuilder().withProjects("CS2103T Team Alpha").build();
        assertTrue(new ProjectContainsKeywordPredicate("CS2103T Team Alpha").test(person));
        assertTrue(new ProjectContainsKeywordPredicate("2103").test(person));
        assertTrue(new ProjectContainsKeywordPredicate("tEaM aLpHa").test(person));
    }

    @Test
    public void test_multiwordPhrase_matchesContiguousPhraseOnly() {
        ProjectContainsKeywordPredicate predicate = new ProjectContainsKeywordPredicate("team alpha");
        assertFalse(predicate.test(new PersonBuilder().withProjects("Team Beta").build()));
        assertFalse(predicate.test(new PersonBuilder().withProjects("Alpha").build()));
        assertFalse(predicate.test(new PersonBuilder().withProjects("Alpha Team").build()));
        assertFalse(predicate.test(new PersonBuilder().withProjects("Team", "Alpha").build()));
        assertFalse(predicate.test(new PersonBuilder().withProjects("Team Project Alpha").build()));
    }

    @Test
    public void test_normalizedWhitespace_returnsTrue() {
        Person person = new PersonBuilder().withProjects(" CS2103T   Team\u00a0Alpha ").build();
        assertTrue(new ProjectContainsKeywordPredicate(" \tTEAM  \u00a0 ALPHA\n").test(person));
    }

    @Test
    public void test_multipleProjects_matchesAnyProjectWithoutChangingContact() {
        Person person = new PersonBuilder().withProjects("Orbital", "CS2103T", "CS2103T Team Alpha").build();
        Person original = new PersonBuilder(person).build();
        assertTrue(new ProjectContainsKeywordPredicate("cs2103").test(person));
        assertEquals(original, person);
    }

    @Test
    public void test_noMatchingProject_returnsFalse() {
        ProjectContainsKeywordPredicate predicate = new ProjectContainsKeywordPredicate("orbital");
        assertFalse(predicate.test(new PersonBuilder().withProjects("CS2103T").build()));
        assertFalse(predicate.test(new PersonBuilder().withProjects().build()));
        assertFalse(predicate.test(new PersonBuilder().withName("Orbital")
                .withEmail("orbital@example.com").withAddress("Orbital Road").withTags("orbital").build()));
    }

    @Test
    public void test_turkishDefaultLocale_matchesUsingRootLocale() {
        Locale originalLocale = Locale.getDefault();
        try {
            Locale.setDefault(Locale.forLanguageTag("tr-TR"));
            Person person = new PersonBuilder().withProjects("ORBITAL").build();
            assertTrue(new ProjectContainsKeywordPredicate("orbital").test(person));
            assertTrue(new ProjectContainsKeywordPredicate("ORBITAL")
                    .test(new PersonBuilder().withProjects("orbital").build()));
        } finally {
            Locale.setDefault(originalLocale);
        }
    }

    @Test
    public void equals() {
        ProjectContainsKeywordPredicate predicate = new ProjectContainsKeywordPredicate("Team Alpha");
        ProjectContainsKeywordPredicate equivalent = new ProjectContainsKeywordPredicate(" TEAM   alpha ");
        assertTrue(predicate.equals(predicate));
        assertTrue(predicate.equals(equivalent));
        assertEquals(predicate.hashCode(), equivalent.hashCode());
        assertFalse(predicate.equals(new ProjectContainsKeywordPredicate("Team Beta")));
        assertFalse(predicate.equals("team alpha"));
        assertFalse(predicate.equals(null));
    }

    @Test
    public void toStringMethod() {
        ProjectContainsKeywordPredicate predicate = new ProjectContainsKeywordPredicate("TEAM alpha");
        assertEquals(ProjectContainsKeywordPredicate.class.getCanonicalName() + "{keyword=team alpha}",
                predicate.toString());
    }
}
