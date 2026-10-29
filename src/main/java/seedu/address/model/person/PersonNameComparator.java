package seedu.address.model.person;

import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.util.Comparator;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Orders persons alphabetically by name, ignoring case and reading each run of digits as a number,
 * so that "Alex 2" comes before "Alex 10".
 * Names that are otherwise equal are ordered by their exact text, and persons with the same name by their saved
 * phone number. Since no two persons share a phone number, the order of any list of persons is always the same.
 */
public final class PersonNameComparator implements Comparator<Person> {

    /** Splits a name wherever it switches between digits and other characters. */
    private static final Pattern DIGIT_BOUNDARY = Pattern.compile("(?<=\\D)(?=\\d)|(?<=\\d)(?=\\D)");
    private static final Pattern NUMBER = Pattern.compile("\\d+");
    private static final Pattern LEADING_ZEROS = Pattern.compile("^0+(?=\\d)");

    private static final Comparator<Person> ORDER = Comparator
            .comparing((Person person) -> person.getName().fullName.toLowerCase(Locale.ROOT),
                    PersonNameComparator::compareNaturally)
            .thenComparing(person -> person.getName().fullName)
            .thenComparing(person -> person.getPhone().value);

    @Override
    public int compare(Person first, Person second) {
        requireAllNonNull(first, second);
        return ORDER.compare(first, second);
    }

    /**
     * Compares two names part by part, where each part is either a run of digits or a run of other characters.
     * Numbers with the same value but different leading zeros, such as "007" and "7", compare as equal.
     */
    static int compareNaturally(String first, String second) {
        String[] firstParts = DIGIT_BOUNDARY.split(first);
        String[] secondParts = DIGIT_BOUNDARY.split(second);
        int sharedLength = Math.min(firstParts.length, secondParts.length);
        for (int i = 0; i < sharedLength; i++) {
            int result = compareParts(firstParts[i], secondParts[i]);
            if (result != 0) {
                return result;
            }
        }
        return Integer.compare(firstParts.length, secondParts.length);
    }

    /**
     * Compares two numbers by value and anything else by text.
     */
    private static int compareParts(String first, String second) {
        boolean areBothNumbers = NUMBER.matcher(first).matches() && NUMBER.matcher(second).matches();
        return areBothNumbers ? compareNumbers(first, second) : first.compareTo(second);
    }

    /**
     * Compares two runs of digits by value without parsing them, since a name may hold more digits than a long.
     */
    private static int compareNumbers(String first, String second) {
        String firstValue = LEADING_ZEROS.matcher(first).replaceFirst("");
        String secondValue = LEADING_ZEROS.matcher(second).replaceFirst("");
        if (firstValue.length() != secondValue.length()) {
            return Integer.compare(firstValue.length(), secondValue.length());
        }
        return firstValue.compareTo(secondValue);
    }
}
