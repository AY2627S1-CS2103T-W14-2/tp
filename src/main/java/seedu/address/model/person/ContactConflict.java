package seedu.address.model.person;

import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

/**
 * Describes why a contact cannot be saved: one of its contact details is already used by {@code existingPerson}.
 * This is the single source of the user-facing duplicate-contact message.
 *
 * @param field The contact detail that clashes.
 * @param value The clashing value, as entered for the new or edited contact.
 * @param existingPerson The contact that already uses {@code value}.
 */
public record ContactConflict(Field field, String value, Person existingPerson) {

    public static final String MESSAGE_FORMAT = "%1$s %2$s is already used by %3$s. Please enter a different %4$s.";

    /**
     * Contact details that must be unique across contacts, in order of reporting precedence.
     */
    public enum Field {
        PHONE("Phone number", "phone number"),
        EMAIL("Email", "email"),
        TELEGRAM_USERNAME("Telegram username", "Telegram username");

        private final String label;
        private final String noun;

        Field(String label, String noun) {
            this.label = label;
            this.noun = noun;
        }
    }

    /**
     * Every field must be present and not null.
     */
    public ContactConflict {
        requireAllNonNull(field, value, existingPerson);
    }

    /**
     * Returns the message shown to the user, naming the field, the value and the existing contact.
     */
    public String getMessage() {
        return String.format(MESSAGE_FORMAT, field.label, value, existingPerson.getName(), field.noun);
    }
}
