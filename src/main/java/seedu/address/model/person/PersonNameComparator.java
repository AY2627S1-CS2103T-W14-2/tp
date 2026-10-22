package seedu.address.model.person;

import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.util.Comparator;
import java.util.Locale;

/**
 * Orders persons alphabetically by name, ignoring case.
 * Names that differ only in case are ordered by their exact text, and persons with the same name by their saved
 * phone number. Since no two persons share a phone number, the order of any list of persons is always the same.
 */
public final class PersonNameComparator implements Comparator<Person> {

    private static final Comparator<Person> ORDER = Comparator
            .comparing((Person person) -> person.getName().fullName.toLowerCase(Locale.ROOT))
            .thenComparing(person -> person.getName().fullName)
            .thenComparing(person -> person.getPhone().value);

    @Override
    public int compare(Person first, Person second) {
        requireAllNonNull(first, second);
        return ORDER.compare(first, second);
    }
}
