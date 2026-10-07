package seedu.address.model.project;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

public class ProjectTest {

    @Test
    public void constructor_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new Project(null));
    }

    @Test
    public void constructor_invalidProjectName_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new Project("   "));
        assertThrows(IllegalArgumentException.class, () -> new Project("\u00A0"));
        assertThrows(IllegalArgumentException.class, () -> new Project("CS2103T\u0085Project"));
        assertThrows(IllegalArgumentException.class, () -> new Project("a".repeat(51)));
    }

    @Test
    public void constructor_validProjectName_normalizesWhitespaceAndPreservesCase() {
        Project project = new Project("\u00A0CS2103T\u2003\u2003Team\tProject\u00A0");

        assertEquals("CS2103T Team Project", project.projectName);
        assertEquals("CS2103T Team Project", project.toString());
    }

    @Test
    public void isValidProjectName() {
        assertFalse(Project.isValidProjectName(""));
        assertFalse(Project.isValidProjectName("   "));
        assertFalse(Project.isValidProjectName("\u00A0"));
        assertFalse(Project.isValidProjectName("CS2103T\u0085Project"));
        assertFalse(Project.isValidProjectName("a".repeat(51)));

        assertTrue(Project.isValidProjectName("A"));
        assertTrue(Project.isValidProjectName("a".repeat(50)));
        assertTrue(Project.isValidProjectName("CS2103T-W14-2"));
        assertTrue(Project.isValidProjectName("Hack&Roll 2026"));
    }

    @Test
    public void equals_caseDifferences_returnsTrueWithSameHashCode() {
        Project uppercaseProject = new Project("CS2103T");
        Project lowercaseProject = new Project("cs2103t");

        assertEquals(uppercaseProject, lowercaseProject);
        assertEquals(uppercaseProject.hashCode(), lowercaseProject.hashCode());
    }

    @Test
    public void equals_differentProject_returnsFalse() {
        assertNotEquals(new Project("CS2103T"), new Project("Orbital"));
    }
}
