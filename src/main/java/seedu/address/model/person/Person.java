package seedu.address.model.person;

import static seedu.address.commons.util.AppUtil.checkArgument;
import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;

import seedu.address.commons.util.ToStringBuilder;
import seedu.address.model.project.Project;

/**
 * Represents a Person in the address book.
 * Guarantees: details are present and not null, field values are validated, immutable.
 */
public class Person {

    public static final String MESSAGE_DUPLICATE_PROJECTS = "A contact cannot contain duplicate projects";

    // Identity fields
    private final Name name;
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
     * Returns true if both persons have the same name.
     * This defines a weaker notion of equality between two persons.
     */
    public boolean isSamePerson(Person otherPerson) {
        if (otherPerson == this) {
            return true;
        }

        return otherPerson != null
                && otherPerson.getName().equals(getName());
    }

    /**
     * Returns true if both persons have the same identity and data fields.
     * This defines a stronger notion of equality between two persons.
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
