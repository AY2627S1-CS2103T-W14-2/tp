package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Locale;

import org.junit.jupiter.api.Test;

import seedu.address.testutil.PersonBuilder;

public class NameContainsKeywordsPredicateTest {

    @Test
    public void equals() {
        List<String> firstPredicateKeywordList = List.of("first");
        List<String> secondPredicateKeywordList = List.of("first", "second");

        NameContainsKeywordsPredicate firstPredicate = new NameContainsKeywordsPredicate(firstPredicateKeywordList);
        NameContainsKeywordsPredicate secondPredicate = new NameContainsKeywordsPredicate(secondPredicateKeywordList);

        // same object -> returns true
        assertTrue(firstPredicate.equals(firstPredicate));

        // same values -> returns true
        NameContainsKeywordsPredicate firstPredicateCopy = new NameContainsKeywordsPredicate(firstPredicateKeywordList);
        assertTrue(firstPredicate.equals(firstPredicateCopy));

        // different types -> returns false
        assertFalse(firstPredicate.equals(1));

        // null -> returns false
        assertFalse(firstPredicate.equals(null));

        // different person -> returns false
        assertFalse(firstPredicate.equals(secondPredicate));
    }

    @Test
    public void test_nameContainsKeywords_returnsTrue() {
        // One keyword
        NameContainsKeywordsPredicate predicate = new NameContainsKeywordsPredicate(List.of("Alice"));
        assertTrue(predicate.test(new PersonBuilder().withName("Alice Bob").build()));

        // Multiple keywords
        predicate = new NameContainsKeywordsPredicate(List.of("Alice", "Bob"));
        assertTrue(predicate.test(new PersonBuilder().withName("Alice Bob").build()));

        // Only one matching keyword
        predicate = new NameContainsKeywordsPredicate(List.of("Bob", "Carol"));
        assertTrue(predicate.test(new PersonBuilder().withName("Alice Carol").build()));

        // Mixed-case keywords
        predicate = new NameContainsKeywordsPredicate(List.of("aLIce", "bOB"));
        assertTrue(predicate.test(new PersonBuilder().withName("Alice Bob").build()));
    }

    @Test
    public void test_nameDoesNotContainKeywords_returnsFalse() {
        // Zero keywords
        NameContainsKeywordsPredicate predicate = new NameContainsKeywordsPredicate(List.of());
        assertFalse(predicate.test(new PersonBuilder().withName("Alice").build()));

        // Non-matching keyword
        predicate = new NameContainsKeywordsPredicate(List.of("Carol"));
        assertFalse(predicate.test(new PersonBuilder().withName("Alice Bob").build()));

        // Keywords match phone, email and address, but do not match name
        predicate = new NameContainsKeywordsPredicate(List.of("12345", "alice@email.com", "Main", "Street"));
        assertFalse(predicate.test(new PersonBuilder().withName("Alice").withPhone("12345")
                .withEmail("alice@email.com").withAddress("Main Street").build()));
    }

    @Test
    public void test_partialKeywords_matchesAnywhereInName() {
        for (String keyword : List.of("Al", "le", "ex", "LEX", "ta", "AN")) {
            NameContainsKeywordsPredicate predicate = new NameContainsKeywordsPredicate(List.of(keyword));
            assertTrue(predicate.test(new PersonBuilder().withName("Alex Tan").build()));
        }
    }

    @Test
    public void test_multiplePartialKeywords_matchesAnyKeyword() {
        NameContainsKeywordsPredicate predicate = new NameContainsKeywordsPredicate(List.of("missing", "lex", "lex"));
        assertTrue(predicate.test(new PersonBuilder().withName("Alex Tan").build()));
        assertFalse(predicate.test(new PersonBuilder().withName("Bob Lee").build()));
    }

    @Test
    public void test_nonNamePartialMatch_returnsFalse() {
        NameContainsKeywordsPredicate predicate =
                new NameContainsKeywordsPredicate(List.of("123", "mail", "reet", "user", "orb", "fri"));
        assertFalse(predicate.test(new PersonBuilder().withName("Alex Tan").withPhone("12345678")
                .withEmail("other@mail.com").withAddress("Main Street").withTelegramUsername("other_user")
                .withProjects("Orbital").withTags("friend").build()));
    }

    @Test
    public void test_turkishDefaultLocale_matchesIgnoringCase() {
        Locale originalLocale = Locale.getDefault();
        try {
            Locale.setDefault(Locale.forLanguageTag("tr-TR"));
            NameContainsKeywordsPredicate predicate = new NameContainsKeywordsPredicate(List.of("i"));
            assertTrue(predicate.test(new PersonBuilder().withName("ALICE").build()));
        } finally {
            Locale.setDefault(originalLocale);
        }
    }

    @Test
    public void toStringMethod() {
        List<String> keywords = List.of("keyword1", "keyword2");
        NameContainsKeywordsPredicate predicate = new NameContainsKeywordsPredicate(keywords);

        String expected = NameContainsKeywordsPredicate.class.getCanonicalName() + "{keywords=" + keywords + "}";
        assertEquals(expected, predicate.toString());
    }
}
