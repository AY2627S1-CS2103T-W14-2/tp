package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.commands.FindProjectCommand;
import seedu.address.logic.commands.ProjectCommand;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Person;
import seedu.address.model.project.Project;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;

/**
 * Integration tests for issue #177: invisible format characters, such as a pasted zero-width space, must not create
 * a project that looks identical to another but is stored, compared and searched as a different one.
 */
public class ProjectInvisibleCharactersIntegrationTest {

    private static final String ZERO_WIDTH_SPACE = "\u200B";
    private static final String WORD_JOINER = "\u2060";

    @TempDir
    public Path temporaryFolder;

    private Model model;
    private Logic logic;
    private JsonAddressBookStorage storage;

    @BeforeEach
    public void setUp() {
        model = new ModelManager(getTypicalAddressBook(), new UserPrefs());
        storage = new JsonAddressBookStorage(temporaryFolder.resolve("contacts.json"));
        logic = new LogicManager(model, new StorageManager(storage,
                new JsonUserPrefsStorage(temporaryFolder.resolve("preferences.json"))));
    }

    @Test
    public void execute_pastedZeroWidthSpace_sameProjectAsTypedName() throws Exception {
        assertEquals(String.format(ProjectCommand.MESSAGE_SUCCESS, "AlphaBeta", ALICE.getName()),
                logic.execute("project 1 pr/Alpha" + ZERO_WIDTH_SPACE + "Beta").getFeedbackToUser());

        // The issue's second step: the visibly identical name is now the same project, not a second one.
        assertThrows(CommandException.class, () -> logic.execute("project 1 pr/AlphaBeta"));
        assertEquals(List.of("AlphaBeta"), projectNamesOfFirstContact());
        assertFalse(Files.readString(storage.getAddressBookFilePath()).contains(ZERO_WIDTH_SPACE));

        // The typed name reaches the project, so it can no longer get stuck.
        logic.execute("deleteproject 1 pr/AlphaBeta");
        assertEquals(List.of(), projectNamesOfFirstContact());
    }

    @Test
    public void execute_pastedJoinerBetweenLetters_sameProjectAsTypedName() throws Exception {
        String zeroWidthJoiner = String.valueOf((char) 0x200D);
        logic.execute("project 1 pr/AlphaBeta");

        assertThrows(CommandException.class, () -> logic.execute("project 1 pr/Alpha" + zeroWidthJoiner + "Beta"));
        assertEquals(List.of("AlphaBeta"), projectNamesOfFirstContact());
    }

    @Test
    public void execute_projectsAndSearch_treatInvisibleVariantsAsOneProject() throws Exception {
        logic.execute("project 1 pr/Alpha" + ZERO_WIDTH_SPACE + "Beta");
        logic.execute("project 2 pr/AlphaBeta");

        String listing = logic.execute("projects").getFeedbackToUser();
        assertEquals(1, listing.lines().filter(line -> line.contains("AlphaBeta")).count());
        assertTrue(listing.contains("- AlphaBeta (2 contacts)"));
        assertEquals(String.format(FindProjectCommand.MESSAGE_CONTACTS_FOUND, 2),
                logic.execute("findproject Alpha" + WORD_JOINER + "Beta").getFeedbackToUser());
    }

    @Test
    public void readAddressBook_savedNameWithInvisibleCharacter_loadsVisibleName() throws Exception {
        Path file = temporaryFolder.resolve("saved.json");
        Files.writeString(file, "{\"persons\":[{\"name\":\"Ann Lee\",\"phone\":\"+6591234567\","
                + "\"email\":\"ann@example.com\",\"telegramUsername\":\"ann_lee\","
                + "\"projects\":[\"Alpha" + ZERO_WIDTH_SPACE + "Beta\"]}]}");

        Person loaded = new JsonAddressBookStorage(file).readAddressBook().orElseThrow().getPersonList().get(0);

        assertEquals(new Project("AlphaBeta"), loaded.getProjects().get(0));
        assertEquals("AlphaBeta", loaded.getProjects().get(0).projectName);
    }

    private List<String> projectNamesOfFirstContact() {
        return model.getAddressBook().getPersonList().get(0).getProjects().stream()
                .map(project -> project.projectName)
                .toList();
    }
}
