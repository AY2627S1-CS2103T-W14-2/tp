package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Region;
import javafx.stage.Stage;
import javafx.stage.Window;
import seedu.address.commons.core.GuiSettings;
import seedu.address.logic.LogicManager;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.person.Person;
import seedu.address.model.project.Project;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.FxTestUtil;
import seedu.address.testutil.PersonBuilder;

public class MainWindowTest {
    // Minimum stage size declared in MainWindow.fxml, and the default width from GuiSettings
    private static final double MIN_WINDOW_WIDTH = 450;
    private static final double MIN_WINDOW_HEIGHT = 600;
    private static final double DEFAULT_WINDOW_WIDTH = 740;
    private static final double DIVIDER_SHIFT = 0.2;
    private static final String LONGEST_NAME = "W".repeat(60);
    private static final String LONGEST_PROJECT_NAME = "W".repeat(Project.MAX_NAME_LENGTH);
    private static final String LONG_EMAIL = "a".repeat(60) + "@example.com";

    @TempDir
    public Path temporaryFolder;

    @Test
    public void commands_updateFeedbackAndHelpAndSaveWindowSettings() throws Exception {
        FxTestUtil.run(() -> {
            Model model = new ModelManager();
            model.addPerson(new PersonBuilder().withProjects("Orbital").build());
            model.setGuiSettings(new GuiSettings(800, 600, 100, 100));
            Stage stage = new Stage();
            MainWindow window = new MainWindow(stage, createLogic(model), temporaryFolder.resolve("contacts.json"));
            try {
                window.show();
                window.fillInnerParts();
                assertEquals(stage, window.getPrimaryStage());
                assertNotNull(window.getPersonListPanel());
                TextField input = (TextField) stage.getScene().lookup("#commandTextField");
                TextArea result = (TextArea) stage.getScene().lookup("#resultDisplay");
                enter(input, "list");
                assertEquals("Listed all contacts.", result.getText());
                enter(input, "unknown");
                assertEquals("Unknown command.", result.getText());
                assertEquals("unknown", input.getText());
                enter(input, "delete 99");
                assertEquals("Index must be a positive integer from the displayed contact list.", result.getText());
                enter(input, "help");
                assertEquals(2, Window.getWindows().size());
                window.handleHelp();
                assertEquals(2, Window.getWindows().size());
                input.fireEvent(new KeyEvent(KeyEvent.KEY_PRESSED, "", "", KeyCode.F1,
                        false, false, false, false));
                assertEquals(2, Window.getWindows().size());
                double width = stage.getWidth();
                enter(input, "exit");
                assertFalse(stage.isShowing());
                assertTrue(Window.getWindows().isEmpty());
                assertEquals(width, model.getGuiSettings().getWindowWidth());
            } finally {
                List.copyOf(Window.getWindows()).forEach(Window::hide);
            }
        });
    }

    @Test
    public void uiManager_startsWindowWithIconAndContactList() throws Exception {
        FxTestUtil.run(() -> {
            Stage stage = new Stage();
            try {
                new UiManager(createLogic(new ModelManager()), temporaryFolder.resolve("contacts.json")).start(stage);
                assertTrue(stage.isShowing());
                assertFalse(stage.getIcons().isEmpty());
                stage.getIcons().forEach(icon -> assertFalse(icon.isError()));
                assertNotNull(stage.getScene().lookup("#personListView"));
                assertNotNull(stage.getScene().lookup("#commandTextField"));
            } finally {
                List.copyOf(Window.getWindows()).forEach(Window::hide);
            }
        });
    }

    @Test
    public void uiManager_startupMessage_shownInResultBox() throws Exception {
        FxTestUtil.run(() -> {
            Stage stage = new Stage();
            try {
                new UiManager(createLogic(new ModelManager()), temporaryFolder.resolve("contacts.json"),
                        "Could not read the data file.").start(stage);
                TextArea result = (TextArea) stage.getScene().lookup("#resultDisplay");
                assertEquals("Could not read the data file.", result.getText());
                // Long lines wrap, so every part of the message stays readable in a narrow window
                assertTrue(result.isWrapText());
            } finally {
                List.copyOf(Window.getWindows()).forEach(Window::hide);
            }
        });
    }

    @Test
    public void recentFindResults_updateAndRemainAboveLargerPersonList() throws Exception {
        FxTestUtil.run(() -> {
            Model model = new ModelManager();
            Person amy = new PersonBuilder().build();
            Person bob = new PersonBuilder().withName("Bob Tan").withPhone("87654321")
                    .withEmail("bob@example.com").withTelegramUsername("bob_tan").build();
            model.addPerson(amy);
            model.addPerson(bob);
            model.setGuiSettings(new GuiSettings(800, 600, 100, 100));
            Stage stage = new Stage();
            MainWindow window = new MainWindow(stage, createLogic(model), temporaryFolder.resolve("contacts.json"));
            try {
                window.fillInnerParts();
                window.show();
                stage.getScene().getRoot().applyCss();
                stage.getScene().getRoot().layout();
                ListView<?> recent = (ListView<?>) stage.getScene().lookup("#recentFindListView");
                ListView<?> persons = (ListView<?>) stage.getScene().lookup("#personListView");
                Label search = (Label) stage.getScene().lookup("#recentFindSearchLabel");
                assertEquals("Latest search: No search yet", search.getText());
                assertTrue(recent.getItems().isEmpty());
                assertTrue(recent.getHeight() < persons.getHeight());
                assertTrue(recent.localToScene(0, recent.getHeight()).getY()
                        <= persons.localToScene(0, 0).getY());

                TextField input = (TextField) stage.getScene().lookup("#commandTextField");
                enter(input, "find Amy");
                assertEquals(List.of(amy), recent.getItems());
                assertEquals("Latest search: Name: Amy", search.getText());
                enter(input, "list");
                assertEquals(List.of(amy), recent.getItems());
                assertEquals(List.of(amy, bob), persons.getItems());
                assertEquals("Latest search: Name: Amy", search.getText());
                enter(input, "findproject Team Alpha");
                assertEquals("Latest search: Project: Team Alpha", search.getText());
                enter(input, "find");
                assertEquals("Latest search: Project: Team Alpha", search.getText());
                enter(input, "find Bob");
                assertEquals(List.of(bob), recent.getItems());
                assertEquals("Latest search: Name: Bob", search.getText());
                enter(input, "find Nobody");
                assertTrue(recent.getItems().isEmpty());
                assertEquals("Latest search: Name: Nobody", search.getText());
                assertEquals(search.getText(), search.getTooltip().getText());
            } finally {
                List.copyOf(Window.getWindows()).forEach(Window::hide);
            }
        });
    }

    @Test
    public void minimumWindowSize_statusBarStaysInsideWindow() throws Exception {
        FxTestUtil.run(() -> {
            Stage stage = showMainWindow(new ModelManager(), MIN_WINDOW_WIDTH, MIN_WINDOW_HEIGHT);
            try {
                Node statusBar = stage.getScene().lookup("#statusbarPlaceholder");
                double statusBarBottom = statusBar.localToScene(statusBar.getLayoutBounds()).getMaxY();
                assertTrue(statusBarBottom <= stage.getScene().getHeight());
            } finally {
                List.copyOf(Window.getWindows()).forEach(Window::hide);
            }
        });
    }

    @Test
    public void longContactDetails_minimumWindowSize_stayInsideContactList() throws Exception {
        FxTestUtil.run(() -> {
            Model model = new ModelManager();
            model.addPerson(new PersonBuilder().withName(LONGEST_NAME).withEmail(LONG_EMAIL)
                    .withProjects(LONGEST_PROJECT_NAME).build());
            Stage stage = showMainWindow(model, MIN_WINDOW_WIDTH, MIN_WINDOW_HEIGHT);
            try {
                ListView<?> persons = (ListView<?>) stage.getScene().lookup("#personListView");
                // A label can hold its full text while part of it is drawn outside the list, so check its position.
                for (Node label : persons.lookupAll(".label")) {
                    assertTrue(rightEdge(label) <= rightEdge(persons), ((Label) label).getText() + " is cut off");
                }
                Label projectTag = (Label) persons.lookup("#projects").lookup(".label");
                assertEquals("Project: " + LONGEST_PROJECT_NAME, projectTag.getTooltip().getText());
            } finally {
                List.copyOf(Window.getWindows()).forEach(Window::hide);
            }
        });
    }

    @Test
    public void sectionDivider_moved_resizesResultBox() throws Exception {
        FxTestUtil.run(() -> {
            Stage stage = showMainWindow(new ModelManager(), DEFAULT_WINDOW_WIDTH, MIN_WINDOW_HEIGHT);
            try {
                Region resultBox = (Region) stage.getScene().lookup("#resultDisplayPlaceholder");
                double initialHeight = resultBox.getHeight();
                SplitPane sections = (SplitPane) stage.getScene().lookup("#mainSections");
                sections.setDividerPosition(0, sections.getDividerPositions()[0] + DIVIDER_SHIFT);
                layOut(stage);
                assertTrue(resultBox.getHeight() > initialHeight);
            } finally {
                List.copyOf(Window.getWindows()).forEach(Window::hide);
            }
        });
    }

    /** Shows a filled main window of the given size for {@code model} and lays it out. */
    private Stage showMainWindow(Model model, double width, double height) {
        model.setGuiSettings(new GuiSettings(width, height, 100, 100));
        Stage stage = new Stage();
        MainWindow window = new MainWindow(stage, createLogic(model), temporaryFolder.resolve("contacts.json"));
        window.fillInnerParts();
        window.show();
        layOut(stage);
        return stage;
    }

    private static double rightEdge(Node node) {
        return node.localToScene(node.getLayoutBounds()).getMaxX();
    }

    private void layOut(Stage stage) {
        stage.getScene().getRoot().applyCss();
        stage.getScene().getRoot().layout();
    }

    private LogicManager createLogic(Model model) {
        return new LogicManager(model, new StorageManager(
                new JsonAddressBookStorage(temporaryFolder.resolve("contacts.json")),
                new JsonUserPrefsStorage(temporaryFolder.resolve("preferences.json"))));
    }

    private void enter(TextField input, String command) {
        input.setText(command);
        input.fireEvent(new ActionEvent());
    }
}
