package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import seedu.address.model.person.Person;
import seedu.address.testutil.FxTestUtil;
import seedu.address.testutil.PersonBuilder;

public class RecentFindPanelTest {
    @Test
    public void searchLabel_updatesWithoutResultChangesAndKeepsFullTooltip() throws Exception {
        FxTestUtil.run(() -> {
            ReadOnlyStringWrapper search = new ReadOnlyStringWrapper("");
            RecentFindPanel panel = new RecentFindPanel(FXCollections.observableArrayList(),
                    search.getReadOnlyProperty());
            Label label = (Label) panel.getRoot().lookup("#recentFindSearchLabel");
            assertEquals("Latest search: No search yet", label.getText());
            assertEquals("Latest search: No search yet", label.getTooltip().getText());

            for (String command : List.of("find " + "x".repeat(60), "findproject " + "y".repeat(40))) {
                search.set(command);
                assertEquals("Latest search: " + command, label.getText());
                assertEquals("Latest search: " + command, label.getTooltip().getText());
            }
            search.set("");
            assertEquals("Latest search: No search yet", label.getText());
            assertEquals("Latest search: No search yet", label.getTooltip().getText());
        });
    }

    @Test
    public void resultRows_reusedCellShowsCurrentDetailsAndClearsOldContent() throws Exception {
        FxTestUtil.run(() -> {
            Person amy = new PersonBuilder().withProjects("Orbital", "CS2103T").build();
            Person bob = PersonBuilder.named("Bob Tan").withoutTelegramUsername().build();
            ObservableList<Person> results = FXCollections.observableArrayList(amy, bob);
            RecentFindPanel panel = new RecentFindPanel(results, new ReadOnlyStringWrapper("find Amy Bob"));
            ListView<Person> list = getListView(panel);
            ListCell<Person> cell = list.getCellFactory().call(list);
            cell.updateListView(list);

            cell.updateIndex(0);
            Label firstContact = (Label) cell.getGraphic().lookup("#recentFindContact");
            assertEquals("Amy Bee  |  @amy_bee", firstContact.getText());
            assertEquals(firstContact.getText(), firstContact.getTooltip().getText());
            FlowPane projects = (FlowPane) cell.getGraphic().lookup("#projects");
            assertEquals(List.of("Project: Orbital", "Project: CS2103T"), projects.getChildren().stream()
                    .map(node -> ((Label) node).getText()).toList());
            cell.updateIndex(1);
            Label contact = (Label) cell.getGraphic().lookup("#recentFindContact");
            assertEquals("Bob Tan  |  No Telegram Username", contact.getText());
            assertEquals(contact.getText(), contact.getTooltip().getText());
            assertFalse(contact.getText().contains("@amy_bee"));
            assertNull(cell.getGraphic().lookup("#telegramUsername"));
            FlowPane missingProjects = (FlowPane) cell.getGraphic().lookup("#projects");
            assertTrue(missingProjects.getChildren().isEmpty());
            assertFalse(missingProjects.isManaged());
            cell.updateIndex(-1);
            assertNull(cell.getText());
            assertNull(cell.getGraphic());
            cell.updateIndex(0);
            results.clear();
            assertNull(cell.getText());
            assertNull(cell.getGraphic());
        });
    }

    @Test
    public void emptyResults_placeholderReturnsAfterClearingWithoutLosingSearchLabel() throws Exception {
        FxTestUtil.run(() -> {
            ObservableList<Person> results = FXCollections.observableArrayList();
            RecentFindPanel panel = new RecentFindPanel(results, new ReadOnlyStringWrapper("find Amy"));
            Stage stage = new Stage();
            try {
                stage.setScene(new Scene(panel.getRoot(), 450, 100));
                stage.show();
                panel.getRoot().applyCss();
                panel.getRoot().layout();
                ListView<Person> list = getListView(panel);
                Label placeholder = (Label) list.getPlaceholder();
                assertEquals("No contacts match the latest search.", placeholder.getText());
                assertTrue(placeholder.getParent().isVisible());

                results.add(new PersonBuilder().build());
                panel.getRoot().layout();
                assertFalse(placeholder.getParent().isVisible());
                assertTrue(list.lookupAll("#recentFindContact").stream()
                        .anyMatch(node -> node instanceof Label label
                                && "Amy Bee  |  @amy_bee".equals(label.getText())));

                results.clear();
                panel.getRoot().layout();
                assertTrue(placeholder.getParent().isVisible());
                Label searchLabel = (Label) panel.getRoot().lookup("#recentFindSearchLabel");
                assertEquals("Latest search: find Amy", searchLabel.getText());
            } finally {
                stage.hide();
            }
        });
    }

    @Test
    public void projectTags_matchPersonCardStylingAndWrapWithinResultWidth() throws Exception {
        FxTestUtil.run(() -> {
            Person person = new PersonBuilder().withProjects("Orbital", "CS2103T Team Alpha", "Team Beta").build();
            RecentFindPanel panel = new RecentFindPanel(FXCollections.observableArrayList(person),
                    new ReadOnlyStringWrapper("find Amy"));
            PersonCard personCard = new PersonCard(person, 1);
            VBox root = new VBox(panel.getRoot(), personCard.getRoot());
            Scene scene = new Scene(root, 300, 400);
            scene.getStylesheets().add(getClass().getResource("/view/DarkTheme.css").toExternalForm());
            Stage stage = new Stage();
            try {
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                root.layout();
                ListView<Person> list = getListView(panel);
                ListCell<?> cell = (ListCell<?>) list.lookupAll(".list-cell").stream()
                        .filter(node -> node instanceof ListCell<?> item && person.equals(item.getItem()))
                        .findFirst().orElseThrow();
                FlowPane recentProjects = (FlowPane) cell.getGraphic().lookup("#projects");
                FlowPane cardProjects = (FlowPane) personCard.getRoot().lookup("#projects");
                Label recentTag = (Label) recentProjects.getChildren().get(0);
                Label cardTag = (Label) cardProjects.getChildren().get(0);
                assertEquals(cardTag.getBackground(), recentTag.getBackground());
                assertEquals(cardTag.getTextFill(), recentTag.getTextFill());
                assertEquals(cardTag.getPadding(), recentTag.getPadding());
                assertEquals(cardTag.getFont(), recentTag.getFont());
                assertEquals(cardTag.getBorder(), recentTag.getBorder());
                assertEquals(cardProjects.getHgap(), recentProjects.getHgap());
                assertEquals(cardProjects.getVgap(), recentProjects.getVgap());
                assertTrue(recentProjects.getChildren().get(2).getLayoutY() > recentTag.getLayoutY());
                assertTrue(recentProjects.getWidth() <= list.getWidth());
            } finally {
                stage.hide();
            }
        });
    }

    /**
     * Returns the panel's list view for checking its cells and empty-state placeholder.
     */
    @SuppressWarnings("unchecked")
    private ListView<Person> getListView(RecentFindPanel panel) {
        return (ListView<Person>) panel.getRoot().lookup("#recentFindListView");
    }
}
