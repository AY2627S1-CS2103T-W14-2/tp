package seedu.address.model.person;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

import com.google.i18n.phonenumbers.PhoneNumberUtil;

/**
 * Represents a Person's phone number in the address book.
 * Guarantees: immutable; is a canonical international number or an unchanged legacy local number.
 */
public class Phone {

    public static final String MESSAGE_CONSTRAINTS =
            "Phone number must start with +, include a valid country calling code, and contain 3-15 digits.";
    private static final String INTERNATIONAL_REGEX = "\\+[0-9]{3,15}";
    private static final String LEGACY_REGEX = "[0-9]{3,}";
    private static final PhoneNumberUtil PHONE_NUMBER_UTIL = PhoneNumberUtil.getInstance();

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
        String normalizedPhone = normalize(test);
        if (!normalizedPhone.matches(INTERNATIONAL_REGEX)) {
            return false;
        }
        String digits = normalizedPhone.substring(1);
        for (int codeLength = 1; codeLength <= 3 && codeLength < digits.length(); codeLength++) {
            int callingCode = Integer.parseInt(digits.substring(0, codeLength));
            if (!PHONE_NUMBER_UTIL.getRegionCodesForCountryCode(callingCode).isEmpty()) {
                return true;
            }
        }
        return false;
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
