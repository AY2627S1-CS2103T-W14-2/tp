package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.TypicalPersons.ALICE;

import java.nio.file.Path;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollBar;
import javafx.scene.layout.Region;
import javafx.stage.Stage;
import seedu.address.logic.Logic;
import seedu.address.logic.LogicManager;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.person.Person;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.FxTestUtil;
import seedu.address.testutil.PersonBuilder;

public class SearchSummaryUiTest {
    @TempDir
    public Path temporaryFolder;

    @Test
    public void caseOnlyProjectChangesAndUndo_refreshBothSearchPanels() throws Exception {
        FxTestUtil.run(() -> {
            Model model = new ModelManager();
            model.addPerson(new PersonBuilder(ALICE).withProjects("Orbital").build());
            Logic logic = new LogicManager(model, new StorageManager(
                    new JsonAddressBookStorage(temporaryFolder.resolve("contacts.json")),
                    new JsonUserPrefsStorage(temporaryFolder.resolve("prefs.json"))));
            Stage stage = new Stage();
            try {
                MainWindow window = new MainWindow(stage, logic, temporaryFolder.resolve("contacts.json"));
                window.fillInnerParts();
                window.show();
                Parent root = stage.getScene().getRoot();
                logic.execute("findproject Orbital");
                assertSearchProject(root, "Orbital");
                logic.execute("renameproject old/Orbital new/orbital");
                assertSearchProject(root, "orbital");
                logic.execute("undo");
                assertSearchProject(root, "Orbital");
                logic.execute("replaceproject 1 old/Orbital new/ORBITAL");
                assertSearchProject(root, "ORBITAL");
                logic.execute("undo");
                assertSearchProject(root, "Orbital");
            } catch (Exception e) {
                throw new AssertionError(e);
            } finally {
                stage.close();
            }
        });
    }

    private void assertSearchProject(Parent root, String project) {
        root.applyCss();
        root.layout();
        for (String panel : java.util.List.of("#mostSearchedDetails", "#recentFindListView")) {
            assertTrue(root.lookup(panel).lookupAll("#projects").stream()
                    .flatMap(node -> ((javafx.scene.layout.FlowPane) node).getChildren().stream())
                    .map(Label.class::cast).anyMatch(label -> ("Project: " + project).equals(label.getText())), panel);
        }
    }

    @Test
    public void searchActivity_sharesLatestSearchAndDistinguishesSessionFavouriteAtMinimumWidth() throws Exception {
        FutureTask<Void> task = new FutureTask<>(() -> {
            Model model = new ModelManager();
            Person person = new PersonBuilder(ALICE).withName("Alexandria ".repeat(5).strip())
                    .withProjects("Orbital", "CS2103T").build();
            model.addPerson(person);
            Logic logic = new LogicManager(model, new StorageManager(
                    new JsonAddressBookStorage(temporaryFolder.resolve("contacts.json")),
                    new JsonUserPrefsStorage(temporaryFolder.resolve("prefs.json"))));
            Stage stage = new Stage();
            try {
                MainWindow window = new MainWindow(stage, logic, temporaryFolder.resolve("contacts.json"));
                window.fillInnerParts();
                window.show();
                Parent root = stage.getScene().getRoot();
                root.applyCss();
                root.layout();
                Region activity = (Region) root.lookup("#recentFind");
                Region mostSearchedRow = (Region) root.lookup("#mostSearchedRow");
                Label recent = (Label) root.lookup("#recentFindSearchLabel");
                assertEquals("Latest search: No search yet", recent.getText());
                assertTrue(root.lookup("#mostSearchedDetails").lookupAll(".label").stream()
                        .anyMatch(node -> "No contacts searched yet.".equals(((Label) node).getText())));

                logic.execute("find missing");
                assertEquals("Latest search: Name: missing", recent.getText());
                assertNull(root.lookup("#mostSearchedName"));

                logic.execute("find Alexandria");
                assertEquals("Latest search: Name: Alexandria", recent.getText());
                assertEquals(recent.getText(), recent.getTooltip().getText());
                root.resize(450, 600);
                root.applyCss();
                root.layout();
                Label name = (Label) root.lookup("#mostSearchedName");
                String details = person.getName().fullName + "  |  @alice_pauline";
                assertEquals(details, name.getText());
                assertEquals(details, name.getTooltip().getText());
                assertTrue(name.getWidth() > 0);
                assertTrue(name.getWidth() < name.prefWidth(-1));
                assertTrue(activity.getHeight() <= 240);
                Region recentPanel = (Region) root.lookup("#recentFindPanelPlaceholder");
                assertTrue(mostSearchedRow.localToScene(mostSearchedRow.getWidth(), 0).getX()
                        < recentPanel.localToScene(0, 0).getX());
                assertEquals(mostSearchedRow.getWidth(), recentPanel.getWidth(), 1);
                assertEquals(root.lookup("#mostSearchedDetails").getParent().localToScene(0, 0).getY(),
                        root.lookup("#recentFindListView").localToScene(0, 0).getY(), 1);
                ScrollBar horizontalBar = root.lookup("#recentFindListView").lookupAll(".scroll-bar").stream()
                        .map(node -> (ScrollBar) node)
                        .filter(bar -> bar.getOrientation() == Orientation.HORIZONTAL)
                        .findFirst().orElseThrow();
                assertEquals(0, horizontalBar.getMaxHeight());
                assertEquals(0, horizontalBar.getOpacity());
                Region personList = (Region) root.lookup("#personListPanelPlaceholder");
                assertTrue(personList.getHeight() > 0);
                // Scene coordinates, because each resizable section has its own parent inside the split pane
                Node personListSection = root.lookup("#personList");
                assertTrue(activity.localToScene(activity.getLayoutBounds()).getMaxY()
                        <= personListSection.localToScene(personListSection.getLayoutBounds()).getMinY());

                logic.execute("edit 1 n/Alicia");
                String editedDetails = "Alicia  |  @alice_pauline";
                assertEquals(editedDetails, ((Label) root.lookup("#mostSearchedName")).getText());
                logic.execute("findproject Orbital");
                assertEquals("Latest search: Project: Orbital", recent.getText());

                // A newer result must not replace the contact with the higher session frequency.
                Person bob = PersonBuilder.named("Bob Tan").withoutTelegramUsername().build();
                model.addPerson(bob);
                logic.execute("find Bob");
                assertEquals("Latest search: Name: Bob", recent.getText());
                assertEquals(editedDetails, ((Label) root.lookup("#mostSearchedName")).getText());
                ListView<?> matches = (ListView<?>) root.lookup("#recentFindListView");
                assertEquals(java.util.List.of(bob), matches.getItems());
                logic.execute("find missing");
                assertTrue(matches.getItems().isEmpty());
                assertEquals(editedDetails, ((Label) root.lookup("#mostSearchedName")).getText());
                logic.execute("clear");
                assertNull(root.lookup("#mostSearchedName"));
                assertEquals("Latest search: Name: missing", recent.getText());
                assertEquals(recent.getText(), recent.getTooltip().getText());
            } finally {
                stage.close();
            }
            return null;
        });
        FxTestUtil.run(task);
        task.get(30, TimeUnit.SECONDS);
    }
}
