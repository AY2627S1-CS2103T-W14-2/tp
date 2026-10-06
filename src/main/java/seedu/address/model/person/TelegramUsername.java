package seedu.address.model.person;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

import java.util.Locale;

/**
 * Represents a Person's Telegram username in the address book.
 * Guarantees: immutable; is either empty for a legacy contact or valid as declared in
 * {@link #isValidTelegramUsername(String)}.
 */
public class TelegramUsername {

    public static final String MESSAGE_CONSTRAINTS = "Telegram usernames should be 5 to 32 characters long and "
            + "contain only Latin letters, digits, or underscores";
    public static final String VALIDATION_REGEX = "[a-z0-9_]{5,32}";
    public static final TelegramUsername EMPTY = new TelegramUsername();

    public final String value;

    /** Creates an empty Telegram username for contacts saved before Telegram support was added. */
    private TelegramUsername() {
        value = "";
    }

    /**
     * Constructs a {@code TelegramUsername}.
     * An optional leading {@code @} is removed and the username is normalized to lowercase.
     *
     * @param username A valid Telegram username.
     */
    public TelegramUsername(String username) {
        requireNonNull(username);
        String normalizedUsername = normalize(username);
        checkArgument(isValidTelegramUsername(normalizedUsername), MESSAGE_CONSTRAINTS);
        value = normalizedUsername;
    }

    /**
     * Returns the normalized form of the given Telegram username.
     */
    public static String normalize(String username) {
        requireNonNull(username);
        String trimmedUsername = username.trim();
        String usernameWithoutAt = trimmedUsername.startsWith("@")
                ? trimmedUsername.substring(1)
                : trimmedUsername;
        return usernameWithoutAt.toLowerCase(Locale.ROOT);
    }

    /**
     * Returns true if the given string is a valid Telegram username.
     */
    public static boolean isValidTelegramUsername(String test) {
        requireNonNull(test);
        return normalize(test).matches(VALIDATION_REGEX);
    }

    /**
     * Returns true if this represents a legacy contact without a Telegram username.
     */
    public boolean isEmpty() {
        return value.isEmpty();
    }

    @Override
    public String toString() {
        return isEmpty() ? "" : "@" + value;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        if (!(other instanceof TelegramUsername otherTelegramUsername)) {
            return false;
        }

        return value.equals(otherTelegramUsername.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }
}
