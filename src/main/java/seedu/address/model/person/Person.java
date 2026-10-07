package seedu.address.model.person;

import static seedu.address.commons.util.AppUtil.checkArgument;
import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import seedu.address.commons.util.ToStringBuilder;
import seedu.address.model.project.Project;
import seedu.address.model.tag.Tag;

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
    private final Address address;
    private final List<Project> projects;
    private final Set<Tag> tags = new HashSet<>();

    /**
     * Every field must be present and not null.
     */
    public Person(Name name, Phone phone, Email email, TelegramUsername telegramUsername, Address address,
            Set<Tag> tags) {
        this(name, phone, email, telegramUsername, address, List.of(), tags);
    }

    /**
     * Every field must be present and not null. Projects must not contain duplicates.
     */
    public Person(Name name, Phone phone, Email email, TelegramUsername telegramUsername, Address address,
            List<Project> projects, Set<Tag> tags) {
        requireAllNonNull(name, phone, email, telegramUsername, address, tags);
        requireAllNonNull(projects);
        checkArgument(new HashSet<>(projects).size() == projects.size(), MESSAGE_DUPLICATE_PROJECTS);
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.telegramUsername = telegramUsername;
        this.address = address;
        this.projects = List.copyOf(projects);
        this.tags.addAll(tags);
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

    public Address getAddress() {
        return address;
    }

    /**
     * Returns the projects in their saved display order.
     */
    public List<Project> getProjects() {
        return projects;
    }

    /**
     * Returns an immutable tag set, which throws {@code UnsupportedOperationException}
     * if modification is attempted.
     */
    public Set<Tag> getTags() {
        return Collections.unmodifiableSet(tags);
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
                && address.equals(otherPerson.address)
                && projects.equals(otherPerson.projects)
                && tags.equals(otherPerson.tags);
    }

    @Override
    public int hashCode() {
        // use this method for custom fields hashing instead of implementing your own
        return Objects.hash(name, phone, email, telegramUsername, address, projects, tags);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("name", name)
                .add("phone", phone)
                .add("email", email)
                .add("telegramUsername", telegramUsername)
                .add("address", address)
                .add("projects", projects)
                .add("tags", tags)
                .toString();
    }

}
