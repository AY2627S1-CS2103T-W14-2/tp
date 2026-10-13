package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.storage.JsonAdaptedPerson.MISSING_FIELD_MESSAGE_FORMAT;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPersons.BENSON;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.model.person.Email;
import seedu.address.model.person.Name;
import seedu.address.model.person.Person;
import seedu.address.model.person.Phone;
import seedu.address.model.person.TelegramUsername;
import seedu.address.model.project.Project;

public class JsonAdaptedPersonTest {
    private static final String INVALID_NAME = "R@chel";
    private static final String INVALID_PHONE = "+9991234";
    private static final String INVALID_EMAIL = "example.com";
    private static final String INVALID_TELEGRAM_USERNAME = "invalid!";
    private static final String INVALID_PROJECT = " ";

    private static final String VALID_NAME = BENSON.getName().toString();
    private static final String VALID_PHONE = BENSON.getPhone().toString();
    private static final String VALID_EMAIL = BENSON.getEmail().toString();
    private static final String VALID_TELEGRAM_USERNAME = BENSON.getTelegramUsername().value;
    private static final List<JsonAdaptedProject> VALID_PROJECTS = BENSON.getProjects().stream()
            .map(JsonAdaptedProject::new)
            .collect(Collectors.toList());

    @Test
    public void toModelType_validPersonDetails_returnsPerson() throws Exception {
        JsonAdaptedPerson person = new JsonAdaptedPerson(BENSON);
        assertEquals(BENSON, person.toModelType());
    }

    @Test
    public void toModelType_legacyLocalPhone_preservesSavedValue() throws Exception {
        JsonAdaptedPerson person = new JsonAdaptedPerson(VALID_NAME, "91234567", VALID_EMAIL,
                VALID_TELEGRAM_USERNAME, VALID_PROJECTS);

        assertEquals("91234567", person.toModelType().getPhone().value);
    }

    @Test
    public void toModelType_invalidName_throwsIllegalValueException() {
        JsonAdaptedPerson person =
                new JsonAdaptedPerson(INVALID_NAME, VALID_PHONE, VALID_EMAIL, VALID_TELEGRAM_USERNAME,
                        VALID_PROJECTS);
        String expectedMessage = Name.MESSAGE_CONSTRAINTS;
        assertThrows(IllegalValueException.class, expectedMessage, person::toModelType);
    }

    @Test
    public void toModelType_nullName_throwsIllegalValueException() {
        JsonAdaptedPerson person = new JsonAdaptedPerson(null, VALID_PHONE, VALID_EMAIL, VALID_TELEGRAM_USERNAME,
                VALID_PROJECTS);
        String expectedMessage = String.format(MISSING_FIELD_MESSAGE_FORMAT, Name.class.getSimpleName());
        assertThrows(IllegalValueException.class, expectedMessage, person::toModelType);
    }

    @Test
    public void toModelType_invalidPhone_throwsIllegalValueException() {
        JsonAdaptedPerson person =
                new JsonAdaptedPerson(VALID_NAME, INVALID_PHONE, VALID_EMAIL, VALID_TELEGRAM_USERNAME,
                        VALID_PROJECTS);
        String expectedMessage = Phone.MESSAGE_CONSTRAINTS;
        assertThrows(IllegalValueException.class, expectedMessage, person::toModelType);
    }

    @Test
    public void toModelType_nullPhone_throwsIllegalValueException() {
        JsonAdaptedPerson person = new JsonAdaptedPerson(VALID_NAME, null, VALID_EMAIL, VALID_TELEGRAM_USERNAME,
                VALID_PROJECTS);
        String expectedMessage = String.format(MISSING_FIELD_MESSAGE_FORMAT, Phone.class.getSimpleName());
        assertThrows(IllegalValueException.class, expectedMessage, person::toModelType);
    }

    @Test
    public void toModelType_invalidEmail_throwsIllegalValueException() {
        JsonAdaptedPerson person =
                new JsonAdaptedPerson(VALID_NAME, VALID_PHONE, INVALID_EMAIL, VALID_TELEGRAM_USERNAME,
                        VALID_PROJECTS);
        String expectedMessage = Email.MESSAGE_CONSTRAINTS;
        assertThrows(IllegalValueException.class, expectedMessage, person::toModelType);
    }

    @Test
    public void toModelType_nullEmail_throwsIllegalValueException() {
        JsonAdaptedPerson person = new JsonAdaptedPerson(VALID_NAME, VALID_PHONE, null, VALID_TELEGRAM_USERNAME,
                VALID_PROJECTS);
        String expectedMessage = String.format(MISSING_FIELD_MESSAGE_FORMAT, Email.class.getSimpleName());
        assertThrows(IllegalValueException.class, expectedMessage, person::toModelType);
    }

    @Test
    public void toModelType_invalidTelegramUsername_throwsIllegalValueException() {
        JsonAdaptedPerson person = new JsonAdaptedPerson(VALID_NAME, VALID_PHONE, VALID_EMAIL,
                INVALID_TELEGRAM_USERNAME, VALID_PROJECTS);
        assertThrows(IllegalValueException.class, TelegramUsername.MESSAGE_CONSTRAINTS, person::toModelType);
    }

    @Test
    public void toModelType_missingTelegramUsername_returnsLegacyPerson() throws Exception {
        JsonAdaptedPerson person = new JsonAdaptedPerson(VALID_NAME, VALID_PHONE, VALID_EMAIL, null,
                VALID_PROJECTS);

        assertEquals(TelegramUsername.EMPTY, person.toModelType().getTelegramUsername());
    }

    @Test
    public void toModelType_invalidProject_throwsIllegalValueException() {
        List<JsonAdaptedProject> invalidProjects = new ArrayList<>(VALID_PROJECTS);
        invalidProjects.add(new JsonAdaptedProject(INVALID_PROJECT));
        JsonAdaptedPerson person = new JsonAdaptedPerson(VALID_NAME, VALID_PHONE, VALID_EMAIL,
                VALID_TELEGRAM_USERNAME, invalidProjects);

        assertThrows(IllegalValueException.class, Project.MESSAGE_CONSTRAINTS, person::toModelType);
    }

    @Test
    public void toModelType_duplicateProjectsIgnoringCase_throwsIllegalValueException() {
        List<JsonAdaptedProject> duplicateProjects = List.of(
                new JsonAdaptedProject("CS2103T"), new JsonAdaptedProject("cs2103t"));
        JsonAdaptedPerson person = new JsonAdaptedPerson(VALID_NAME, VALID_PHONE, VALID_EMAIL,
                VALID_TELEGRAM_USERNAME, duplicateProjects);

        assertThrows(IllegalValueException.class, Person.MESSAGE_DUPLICATE_PROJECTS, person::toModelType);
    }

    @Test
    public void toModelType_missingProjects_returnsPersonWithEmptyProjectList() throws Exception {
        JsonAdaptedPerson person = new JsonAdaptedPerson(VALID_NAME, VALID_PHONE, VALID_EMAIL,
                VALID_TELEGRAM_USERNAME, null);

        assertEquals(List.of(), person.toModelType().getProjects());
    }

    @Test
    public void toModelType_projects_preservesSavedOrder() throws Exception {
        JsonAdaptedPerson person = new JsonAdaptedPerson(VALID_NAME, VALID_PHONE, VALID_EMAIL,
                VALID_TELEGRAM_USERNAME,
                List.of(new JsonAdaptedProject("Orbital"), new JsonAdaptedProject("CS2103T")));

        assertEquals(List.of(new Project("Orbital"), new Project("CS2103T")),
                person.toModelType().getProjects());
    }

}
