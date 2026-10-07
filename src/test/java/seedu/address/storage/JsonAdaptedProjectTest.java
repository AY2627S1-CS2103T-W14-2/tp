package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.model.project.Project;

public class JsonAdaptedProjectTest {

    @Test
    public void toModelType_validProjectName_returnsProject() throws Exception {
        assertEquals(new Project("CS2103T Team Project"),
                new JsonAdaptedProject("  CS2103T   Team Project  ").toModelType());
    }

    @Test
    public void toModelType_invalidProjectName_throwsIllegalValueException() {
        assertThrows(IllegalValueException.class, Project.MESSAGE_CONSTRAINTS, () ->
                new JsonAdaptedProject(" ").toModelType());
    }

    @Test
    public void toModelType_nullProjectName_throwsIllegalValueException() {
        assertThrows(IllegalValueException.class, Project.MESSAGE_CONSTRAINTS, () ->
                new JsonAdaptedProject((String) null).toModelType());
    }
}
