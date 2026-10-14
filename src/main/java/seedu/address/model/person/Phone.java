package seedu.address.model.person;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

/**
 * Represents a Person's phone number in the address book.
 * Guarantees: immutable; is a canonical international number or an unchanged legacy local number.
 */
public class Phone {

    public static final String MESSAGE_CONSTRAINTS =
            "Phone number must start with + and contain 3-15 digits, with a non-zero first digit. "
                    + "The + must be immediately followed by a digit; spaces and hyphens are allowed "
                    + "only between digits, e.g. +65 9123-4567.";
    private static final String INPUT_REGEX = "\\+[1-9][0-9 -]*[0-9]";
    private static final String INTERNATIONAL_REGEX = "\\+[1-9][0-9]{2,14}";
    private static final String LEGACY_REGEX = "[0-9]{3,}";

    public final String value;

    /**
     * Constructs a {@code Phone}.
     *
     * @param phone A valid phone number.
     */
    public Phone(String phone) {
        this(phone, false);
    }

    private Phone(String phone, boolean fromStorage) {
        requireNonNull(phone);
        checkArgument(fromStorage ? isValidStoredPhone(phone) : isValidPhone(phone), MESSAGE_CONSTRAINTS);
        value = fromStorage ? phone : normalize(phone);
    }

    /**
     * Restores a saved phone without assigning a country code or changing a legacy local number.
     */
    public static Phone fromStoredValue(String phone) {
        return new Phone(phone, true);
    }

    /**
     * Returns true if the value can be restored from a current or older data file.
     */
    public static boolean isValidStoredPhone(String test) {
        requireNonNull(test);
        return test.matches(LEGACY_REGEX) || (test.equals(normalize(test)) && isValidPhone(test));
    }

    /**
     * Removes the permitted formatting characters from an international phone number.
     */
    public static String normalize(String phone) {
        requireNonNull(phone);
        return phone.trim().replace(" ", "").replace("-", "");
    }

    /**
     * Returns true if a given string is a valid phone number.
     */
    public static boolean isValidPhone(String test) {
        requireNonNull(test);
        if (!test.trim().matches(INPUT_REGEX)) {
            return false;
        }
        String normalizedPhone = normalize(test);
        return normalizedPhone.matches(INTERNATIONAL_REGEX);
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
        if (!(other instanceof Phone otherPhone)) {
            return false;
        }

        return value.equals(otherPhone.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

}
