package seedu.address.testutil;

import java.util.List;
import java.util.Locale;

import seedu.address.model.person.Email;
import seedu.address.model.person.Name;
import seedu.address.model.person.Person;
import seedu.address.model.person.Phone;
import seedu.address.model.person.TelegramUsername;
import seedu.address.model.project.Project;
import seedu.address.model.util.SampleDataUtil;

/**
 * A utility class to help with building Person objects.
 */
public class PersonBuilder {

    public static final String DEFAULT_NAME = "Amy Bee";
    public static final String DEFAULT_PHONE = "+6585355255";
    public static final String DEFAULT_EMAIL = "amy@gmail.com";
    public static final String DEFAULT_TELEGRAM_USERNAME = "amy_bee";

    private Name name;
    private Phone phone;
    private Email email;
    private TelegramUsername telegramUsername;
    private List<Project> projects;

    /**
     * Creates a {@code PersonBuilder} with the default details.
     */
    public PersonBuilder() {
        name = new Name(DEFAULT_NAME);
        phone = Phone.fromStoredValue(DEFAULT_PHONE);
        email = new Email(DEFAULT_EMAIL);
        telegramUsername = new TelegramUsername(DEFAULT_TELEGRAM_USERNAME);
        projects = List.of();
    }

    /**
     * Initializes the PersonBuilder with the data of {@code personToCopy}.
     */
    public PersonBuilder(Person personToCopy) {
        name = personToCopy.getName();
        phone = personToCopy.getPhone();
        email = personToCopy.getEmail();
        telegramUsername = personToCopy.getTelegramUsername();
        projects = List.copyOf(personToCopy.getProjects());
    }

    /**
     * Creates a {@code PersonBuilder} named {@code name}, whose phone, email and Telegram username are derived from
     * {@code name}. Persons built this way with different names do not share contact details, so they can be added
     * to the same address book.
     */
    public static PersonBuilder named(String name) {
        String key = name.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]", "");
        String phone = "+65" + (10_000_000 + Math.floorMod(key.hashCode(), 90_000_000));
        String telegramUsername = key + "_user";
        return new PersonBuilder().withName(name).withPhone(phone).withEmail(key + "@example.com")
                .withTelegramUsername(telegramUsername.substring(0, Math.min(telegramUsername.length(), 32)));
    }

    /**
     * Sets the {@code Name} of the {@code Person} that we are building.
     */
    public PersonBuilder withName(String name) {
        this.name = new Name(name);
        return this;
    }

    /**
     * Restores a saved name verbatim, including legacy spacing.
     * Use {@link #withName(String)} for newly entered names.
     */
    public PersonBuilder withStoredName(String name) {
        this.name = Name.fromStoredValue(name);
        return this;
    }

    /**
     * Sets the {@code Phone} of the {@code Person} that we are building.
     */
    public PersonBuilder withPhone(String phone) {
        this.phone = Phone.fromStoredValue(phone);
        return this;
    }

    /**
     * Sets the {@code Email} of the {@code Person} that we are building.
     */
    public PersonBuilder withEmail(String email) {
        this.email = new Email(email);
        return this;
    }

    /**
     * Restores a saved email verbatim, including legacy spelling and case.
     * Use {@link #withEmail(String)} for newly entered email values.
     */
    public PersonBuilder withStoredEmail(String email) {
        this.email = Email.fromStoredValue(email);
        return this;
    }

    /**
     * Sets the {@code TelegramUsername} of the {@code Person} that we are building.
     */
    public PersonBuilder withTelegramUsername(String telegramUsername) {
        this.telegramUsername = new TelegramUsername(telegramUsername);
        return this;
    }

    /**
     * Gives the {@code Person} that we are building no Telegram username, as for legacy contacts.
     */
    public PersonBuilder withoutTelegramUsername() {
        this.telegramUsername = TelegramUsername.EMPTY;
        return this;
    }

    /**
     * Parses the project names into an ordered project list for the person being built.
     */
    public PersonBuilder withProjects(String... projectNames) {
        projects = SampleDataUtil.getProjectList(projectNames);
        return this;
    }

    public Person build() {
        return new Person(name, phone, email, telegramUsername, projects);
    }

}
