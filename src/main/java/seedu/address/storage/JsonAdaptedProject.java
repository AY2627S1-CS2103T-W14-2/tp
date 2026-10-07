package seedu.address.storage;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.model.project.Project;

/**
 * Jackson-friendly version of {@link Project}.
 */
class JsonAdaptedProject {

    private final String projectName;

    /**
     * Constructs a {@code JsonAdaptedProject} with the given {@code projectName}.
     */
    @JsonCreator
    public JsonAdaptedProject(String projectName) {
        this.projectName = projectName;
    }

    /**
     * Converts a given {@code Project} into this class for Jackson use.
     */
    public JsonAdaptedProject(Project source) {
        projectName = source.projectName;
    }

    @JsonValue
    public String getProjectName() {
        return projectName;
    }

    /**
     * Converts this Jackson-friendly adapted project object into the model's {@code Project} object.
     *
     * @throws IllegalValueException if the project name violates the model constraints.
     */
    public Project toModelType() throws IllegalValueException {
        if (projectName == null || !Project.isValidProjectName(projectName)) {
            throw new IllegalValueException(Project.MESSAGE_CONSTRAINTS);
        }
        return new Project(projectName);
    }
}
