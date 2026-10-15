package seedu.address.model.person.exceptions;

import static java.util.Objects.requireNonNull;

import seedu.address.model.person.ContactConflict;

/**
 * Signals that the operation would result in two persons that conflict, i.e. share a phone number, email or
 * Telegram username.
 */
public class DuplicatePersonException extends RuntimeException {

    /**
     * Creates an exception whose message describes {@code conflict}.
     */
    public DuplicatePersonException(ContactConflict conflict) {
        super(requireNonNull(conflict).getMessage());
    }
}
