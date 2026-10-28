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
        assertThrows(IllegalArgumentException.class, () -> new Project("a".repeat(41)));
    }

    @Test
    public void constructor_validProjectName_normalizesWhitespaceAndPreservesCase() {
        Project project = new Project("\u00A0CS2103T\u2003\u2003Team\tProject\u00A0");

        assertEquals("CS2103T Team Project", project.projectName);
        assertEquals("CS2103T Team Project", project.toString());
    }

    @Test
    public void constructor_invisibleFormatCharacters_removed() {
        assertEquals("AlphaBeta", new Project("Alpha\u200BBeta").projectName); // zero-width space (issue #177)
        assertEquals("CS2103T", new Project("\uFEFFCS2103T").projectName); // byte order mark
        assertEquals("TeamB", new Project("Team\u00ADB").projectName); // soft hyphen
        assertEquals("Alpha", new Project("\u202EAlpha").projectName); // right-to-left override
        assertEquals("Alpha Beta", new Project("Alpha \u200B Beta").projectName); // spaces still collapse
        assertEquals(new Project("AlphaBeta"), new Project("Alpha\u2060Beta")); // word joiner
    }

    @Test
    public void constructor_joinersNeededByEmojiAndScripts_preserved() {
        String technologist = "Team \uD83D\uDC68\u200D\uD83D\uDCBB"; // man, zero-width joiner, laptop
        String persian = "\u0645\u06CC\u200C\u062E\u0648\u0627\u0647\u0645"; // uses a zero-width non-joiner
        assertEquals(technologist, new Project(technologist).projectName);
        assertEquals(persian, new Project(persian).projectName);
    }

    @Test
    public void constructor_strayJoiners_removed() {
        assertEquals("AlphaBeta", new Project("Alpha\u200DBeta").projectName); // joiner between Latin letters
        assertEquals("AlphaBeta", new Project("Alpha\u200CBeta").projectName); // non-joiner between Latin letters
        assertEquals("Team 2", new Project("Team 2\u200D").projectName); // at the end
        assertEquals("Team", new Project("\u200DTeam").projectName); // at the start
        assertEquals("CS2103T-Team", new Project("CS2103T\u200D-Team").projectName); // next to punctuation
        assertEquals("Team 2", new Project("Team \u200D 2").projectName); // between spaces
        assertEquals(new Project("AlphaBeta"), new Project("Alpha\u200D\u200DBeta")); // repeated joiners
    }

    @Test
    public void constructor_joinersBetweenEmojiOrScriptLetters_preserved() {
        String family = "\uD83D\uDC68\u200D\uD83D\uDC69\u200D\uD83D\uDC67"; // man, woman, girl joined into one emoji
        String hindi = "\u0915\u094D\u200D\u0937"; // a Devanagari conjunct shaped by a joiner
        assertEquals(family, new Project(family).projectName);
        assertEquals(hindi, new Project(hindi).projectName);
    }

    @Test
    public void isValidProjectName_invisibleFormatCharacters_notCounted() {
        assertFalse(Project.isValidProjectName("\u200B")); // nothing visible is left
        assertFalse(Project.isValidProjectName("\u200B \uFEFF"));
        assertTrue(Project.isValidProjectName("a".repeat(40) + "\u200B")); // limit counts visible characters
    }

    @Test
    public void isValidProjectName() {
        assertFalse(Project.isValidProjectName(""));
        assertFalse(Project.isValidProjectName("   "));
        assertFalse(Project.isValidProjectName("\u00A0"));
        assertFalse(Project.isValidProjectName("CS2103T\u0085Project"));
        assertFalse(Project.isValidProjectName("a".repeat(41)));

        assertTrue(Project.isValidProjectName("A"));
        assertTrue(Project.isValidProjectName("a".repeat(40)));
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
