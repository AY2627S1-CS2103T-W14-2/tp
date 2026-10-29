package seedu.address.model.person;

import static seedu.address.commons.util.AppUtil.checkArgument;

import java.util.Locale;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import seedu.address.commons.util.ToStringBuilder;
import seedu.address.model.project.Project;

/**
 * Tests whether any of a contact's project names contains the whole search phrase, ignoring case.
 */
public class ProjectContainsKeywordPredicate implements Predicate<Person> {
    private final String keyword;
    private final Pattern pattern;

    /**
     * Creates a predicate with a non-blank phrase, normalising whitespace and case for matching.
     */
    public ProjectContainsKeywordPredicate(String keyword) {
        String normalizedKeyword = Project.normalize(keyword);
        checkArgument(!normalizedKeyword.isEmpty(), "Project keyword must not be blank");
        this.keyword = normalizedKeyword.toLowerCase(Locale.ROOT);
        pattern = Pattern.compile(Pattern.quote(normalizedKeyword), Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
    }

    @Override
    public boolean test(Person person) {
        return person.getProjects().stream()
                .anyMatch(project -> pattern.matcher(project.projectName).find());
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        if (!(other instanceof ProjectContainsKeywordPredicate otherPredicate)) {
            return false;
        }

        return keyword.equals(otherPredicate.keyword);
    }

    @Override
    public int hashCode() {
        return keyword.hashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).add("keyword", keyword).toString();
    }
}
