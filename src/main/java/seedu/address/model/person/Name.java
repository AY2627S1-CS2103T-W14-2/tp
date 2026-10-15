package seedu.address.model.person;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

/**
 * Represents a Person's name in the address book.
 * Guarantees: immutable; is valid for new input or was valid in an older saved contact.
 */
public class Name {

    public static final String MESSAGE_CONSTRAINTS =
            "Name must be 1-60 characters and contain at least one English letter. "
                    + "Only English letters, digits, spaces, apostrophes, hyphens, and periods are allowed.";
    private static final String VALIDATION_REGEX = "[A-Za-z0-9 .'-]{1,60}";
    private static final String LEGACY_VALIDATION_REGEX = "[\\p{Alnum}][\\p{Alnum} ]*";

    public final String fullName;

    /**
     * Constructs a {@code Name}.
     *
     * @param name A valid name.
     */
    public Name(String name) {
        this(name, false);
    }

    private Name(String name, boolean fromStorage) {
        requireNonNull(name);
        checkArgument(fromStorage ? isValidStoredName(name) : isValidName(name), MESSAGE_CONSTRAINTS);
        fullName = fromStorage ? name : name.trim();
    }

    /**
     * Restores a saved name without changing its spelling or spacing. Older contacts may have names that are no
     * longer accepted as new input.
     */
    public static Name fromStoredValue(String name) {
        return new Name(name, true);
    }

    /**
     * Returns true if the value can be restored from a current or older data file.
     */
    public static boolean isValidStoredName(String test) {
        requireNonNull(test);
        return isValidName(test) || test.matches(LEGACY_VALIDATION_REGEX);
    }

    /**
     * Returns true if a given string is a valid name.
     */
    public static boolean isValidName(String test) {
        requireNonNull(test);
        String trimmedName = test.trim();
        return trimmedName.matches(VALIDATION_REGEX) && trimmedName.matches(".*[A-Za-z].*");
    }


    @Override
    public String toString() {
        return fullName;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof Name otherName)) {
            return false;
        }

        return fullName.equals(otherName.fullName);
    }

    @Override
    public int hashCode() {
        return fullName.hashCode();
    }

}
