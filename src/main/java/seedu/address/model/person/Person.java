package seedu.address.model.person;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;
import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import seedu.address.commons.util.ToStringBuilder;
import seedu.address.model.project.Project;

/**
 * Represents a Person in the address book.
 * Guarantees: details are present and not null, field values are validated, immutable.
 */
public class Person {

    public static final String MESSAGE_DUPLICATE_PROJECTS = "A contact cannot contain duplicate projects";

    private final Name name;

    // Contact details that must be unique across contacts
    private final Phone phone;
    private final Email email;
    private final TelegramUsername telegramUsername;

    // Data fields
    private final List<Project> projects;

    /**
     * Every field must be present and not null.
     */
    public Person(Name name, Phone phone, Email email, TelegramUsername telegramUsername) {
        this(name, phone, email, telegramUsername, List.of());
    }

    /**
     * Every field must be present and not null. Projects must not contain duplicates.
     */
    public Person(Name name, Phone phone, Email email, TelegramUsername telegramUsername,
            List<Project> projects) {
        requireAllNonNull(name, phone, email, telegramUsername);
        requireAllNonNull(projects);
        checkArgument(new HashSet<>(projects).size() == projects.size(), MESSAGE_DUPLICATE_PROJECTS);
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.telegramUsername = telegramUsername;
        this.projects = List.copyOf(projects);
    }

    public Name getName() {
        return name;
    }

    public Phone getPhone() {
        return phone;
    }

    public Email getEmail() {
        return email;
    }

    public TelegramUsername getTelegramUsername() {
        return telegramUsername;
    }

    /**
     * Returns the projects in their saved display order.
     */
    public List<Project> getProjects() {
        return projects;
    }

    /**
     * Returns true if this person and {@code otherPerson} cannot both be saved, because they share a phone number,
     * an email (ignoring case) or a Telegram username (ignoring case). Names and projects are never compared.
     * This relation is symmetric but not transitive, so it is not an equivalence.
     */
    public boolean conflictsWith(Person otherPerson) {
        return findConflictWith(otherPerson).isPresent();
    }

    /**
     * Returns the first contact detail this person shares with {@code existingPerson}, checking phone, email and
     * Telegram username in that order, or an empty {@code Optional} if they do not conflict.
     * The reported value is taken from this person.
     */
    public Optional<ContactConflict> findConflictWith(Person existingPerson) {
        requireNonNull(existingPerson);
        if (phone.equals(existingPerson.phone)) {
            return conflictOn(ContactConflict.Field.PHONE, phone, existingPerson);
        }
        if (email.equals(existingPerson.email)) {
            return conflictOn(ContactConflict.Field.EMAIL, email, existingPerson);
        }
        if (telegramUsername.equals(existingPerson.telegramUsername)) {
            return conflictOn(ContactConflict.Field.TELEGRAM_USERNAME, telegramUsername, existingPerson);
        }
        return Optional.empty();
    }

    private static Optional<ContactConflict> conflictOn(ContactConflict.Field field, Object value,
            Person existingPerson) {
        return Optional.of(new ContactConflict(field, value.toString(), existingPerson));
    }

    /**
     * Returns true if both persons have identical fields.
     * Two contacts that are not equal may still conflict; see {@link #conflictsWith(Person)}.
     */
    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof Person otherPerson)) {
            return false;
        }

        return name.equals(otherPerson.name)
                && phone.equals(otherPerson.phone)
                && email.equals(otherPerson.email)
                && telegramUsername.equals(otherPerson.telegramUsername)
                && projects.equals(otherPerson.projects);
    }

    @Override
    public int hashCode() {
        // use this method for custom fields hashing instead of implementing your own
        return Objects.hash(name, phone, email, telegramUsername, projects);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("name", name)
                .add("phone", phone)
                .add("email", email)
                .add("telegramUsername", telegramUsername)
                .add("projects", projects)
                .toString();
    }

}
