package seedu.address.storage;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.model.person.Email;
import seedu.address.model.person.Name;
import seedu.address.model.person.Person;
import seedu.address.model.person.Phone;
import seedu.address.model.person.TelegramUsername;
import seedu.address.model.project.Project;

/**
 * Jackson-friendly version of {@link Person}.
 */
class JsonAdaptedPerson {

    public static final String MISSING_FIELD_MESSAGE_FORMAT = "Person's %s field is missing!";

    private final String name;
    private final String phone;
    private final String email;
    private final String telegramUsername;
    private final List<JsonAdaptedProject> projects = new ArrayList<>();

    /**
     * Constructs a {@code JsonAdaptedPerson} with the given person details.
     */
    @JsonCreator
    public JsonAdaptedPerson(@JsonProperty("name") String name, @JsonProperty("phone") String phone,
            @JsonProperty("email") String email, @JsonProperty("telegramUsername") String telegramUsername,
            @JsonProperty("projects") List<JsonAdaptedProject> projects) {
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.telegramUsername = telegramUsername;
        if (projects != null) {
            this.projects.addAll(projects);
        }
    }

    /**
     * Converts a given {@code Person} into this class for Jackson use.
     */
    public JsonAdaptedPerson(Person source) {
        name = source.getName().fullName;
        phone = source.getPhone().value;
        email = source.getEmail().value;
        telegramUsername = source.getTelegramUsername().value;
        projects.addAll(source.getProjects().stream()
                .map(JsonAdaptedProject::new)
                .collect(Collectors.toList()));
    }

    /**
     * Converts this Jackson-friendly adapted person object into the model's {@code Person} object.
     *
     * @throws IllegalValueException if there were any data constraints violated in the adapted person.
     */
    public Person toModelType() throws IllegalValueException {
        final List<Project> personProjects = new ArrayList<>();
        for (JsonAdaptedProject project : projects) {
            if (project == null) {
                throw new IllegalValueException(Project.MESSAGE_CONSTRAINTS);
            }
            Project modelProject = project.toModelType();
            if (personProjects.contains(modelProject)) {
                throw new IllegalValueException(Person.MESSAGE_DUPLICATE_PROJECTS);
            }
            personProjects.add(modelProject);
        }

        if (name == null) {
            throw new IllegalValueException(String.format(MISSING_FIELD_MESSAGE_FORMAT, Name.class.getSimpleName()));
        }
        if (!Name.isValidStoredName(name)) {
            throw new IllegalValueException(Name.MESSAGE_CONSTRAINTS);
        }
        final Name modelName = Name.fromStoredValue(name);

        if (phone == null) {
            throw new IllegalValueException(String.format(MISSING_FIELD_MESSAGE_FORMAT, Phone.class.getSimpleName()));
        }
        if (!Phone.isValidStoredPhone(phone)) {
            throw new IllegalValueException(Phone.MESSAGE_CONSTRAINTS);
        }
        final Phone modelPhone = Phone.fromStoredValue(phone);

        if (email == null) {
            throw new IllegalValueException(String.format(MISSING_FIELD_MESSAGE_FORMAT, Email.class.getSimpleName()));
        }
        if (!Email.isValidStoredEmail(email)) {
            throw new IllegalValueException(Email.MESSAGE_CONSTRAINTS);
        }
        final Email modelEmail = Email.fromStoredValue(email);

        final TelegramUsername modelTelegramUsername;
        if (telegramUsername == null || telegramUsername.isEmpty()) {
            modelTelegramUsername = TelegramUsername.EMPTY;
        } else {
            if (!TelegramUsername.isValidStoredTelegramUsername(telegramUsername)) {
                throw new IllegalValueException(TelegramUsername.MESSAGE_CONSTRAINTS);
            }
            modelTelegramUsername = TelegramUsername.fromStoredValue(telegramUsername);
        }

        return new Person(modelName, modelPhone, modelEmail, modelTelegramUsername, personProjects);
    }

}
