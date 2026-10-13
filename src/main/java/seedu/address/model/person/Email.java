package seedu.address.model.person;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

import java.util.Arrays;
import java.util.Locale;

/**
 * Represents a Person's email in the address book.
 * Guarantees: immutable; is valid for new input or was valid in an older saved contact.
 */
public class Email {

    public static final String MESSAGE_CONSTRAINTS = "Email must be in the format local@domain, with exactly one @, "
            + "no whitespace, and at least two non-empty domain labels separated by dots.";
    private static final String SPECIAL_CHARACTERS = "+_.-";
    // Previous AB3 rule, retained only to read contacts saved before the new input rule.
    private static final String ALPHANUMERIC_NO_UNDERSCORE = "[^\\W_]+"; // alphanumeric characters except underscore
    private static final String LOCAL_PART_REGEX = "^" + ALPHANUMERIC_NO_UNDERSCORE + "([" + SPECIAL_CHARACTERS + "]"
            + ALPHANUMERIC_NO_UNDERSCORE + ")*";
    private static final String DOMAIN_PART_REGEX = ALPHANUMERIC_NO_UNDERSCORE
            + "(-" + ALPHANUMERIC_NO_UNDERSCORE + ")*";
    private static final String DOMAIN_LAST_PART_REGEX = "(" + DOMAIN_PART_REGEX + "){2,}$"; // At least two chars
    private static final String DOMAIN_REGEX = "(" + DOMAIN_PART_REGEX + "\\.)*" + DOMAIN_LAST_PART_REGEX;
    private static final String LEGACY_VALIDATION_REGEX = LOCAL_PART_REGEX + "@" + DOMAIN_REGEX;

    public final String value;

    /**
     * Constructs an {@code Email}.
     *
     * @param email A valid email address.
     */
    public Email(String email) {
        this(email, false);
    }

    private Email(String email, boolean fromStorage) {
        requireNonNull(email);
        checkArgument(fromStorage ? isValidStoredEmail(email) : isValidEmail(email), MESSAGE_CONSTRAINTS);
        value = fromStorage ? email : email.toLowerCase(Locale.ROOT);
    }

    /**
     * Restores a saved email without changing its case. Older contacts may have emails that are no longer accepted
     * as new input.
     */
    public static Email fromStoredValue(String email) {
        return new Email(email, true);
    }

    /**
     * Returns true if the value can be restored from a current or older data file.
     */
    public static boolean isValidStoredEmail(String test) {
        requireNonNull(test);
        return isValidEmail(test) || test.matches(LEGACY_VALIDATION_REGEX);
    }

    /**
     * Returns true if a given string is a valid email.
     */
    public static boolean isValidEmail(String test) {
        requireNonNull(test);
        int atIndex = test.indexOf('@');
        if (atIndex < 1 || atIndex != test.lastIndexOf('@')
                || test.codePoints().anyMatch(Email::isWhitespaceOrControl)) {
            return false;
        }
        String domain = test.substring(atIndex + 1);
        String[] labels = domain.split("\\.", -1);
        return labels.length >= 2 && Arrays.stream(labels).allMatch(label -> !label.isEmpty());
    }

    private static boolean isWhitespaceOrControl(int codePoint) {
        return Character.isWhitespace(codePoint) || Character.isSpaceChar(codePoint)
                || Character.isISOControl(codePoint);
    }

    @Override
    public String toString() {
        return value;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof Email otherEmail)) {
            return false;
        }

        return value.equals(otherEmail.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

}
