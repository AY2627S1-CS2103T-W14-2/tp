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
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import javafx.stage.Window;
import seedu.address.commons.core.GuiSettings;
import seedu.address.logic.LogicManager;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.person.Person;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.FxTestUtil;
import seedu.address.testutil.PersonBuilder;

public class MainWindowTest {
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
