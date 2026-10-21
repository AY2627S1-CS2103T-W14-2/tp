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
            assertEquals("No search yet", label.getText());
            assertEquals("No search yet", label.getTooltip().getText());

            for (String command : List.of("find " + "x".repeat(60), "findproject " + "y".repeat(40))) {
                search.set(command);
                assertEquals("Search: " + command, label.getText());
                assertEquals("Search: " + command, label.getTooltip().getText());
            }
            search.set("");
            assertEquals("No search yet", label.getText());
            assertEquals("No search yet", label.getTooltip().getText());
        });
    }

    @Test
    public void resultRows_reusedCellShowsCurrentNameAndEmailAndClearsOldText() throws Exception {
        FxTestUtil.run(() -> {
            Person amy = new PersonBuilder().build();
            Person bob = PersonBuilder.named("Bob Tan").build();
            ObservableList<Person> results = FXCollections.observableArrayList(amy, bob);
            RecentFindPanel panel = new RecentFindPanel(results, new ReadOnlyStringWrapper("find Amy Bob"));
            ListView<Person> list = getListView(panel);
            ListCell<Person> cell = list.getCellFactory().call(list);
            cell.updateListView(list);

            cell.updateIndex(0);
            assertEquals("Amy Bee  |  amy@gmail.com", cell.getText());
            assertNull(cell.getGraphic());
            cell.updateIndex(1);
            assertEquals(bob.getName().fullName + "  |  " + bob.getEmail().value, cell.getText());
            assertFalse(cell.getText().contains(amy.getEmail().value));
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
                assertEquals("No recent find results.", placeholder.getText());
                assertTrue(placeholder.getParent().isVisible());

                results.add(new PersonBuilder().build());
                panel.getRoot().layout();
                assertFalse(placeholder.getParent().isVisible());
                assertTrue(list.lookupAll(".list-cell").stream()
                        .anyMatch(node -> node instanceof ListCell<?> cell
                                && "Amy Bee  |  amy@gmail.com".equals(cell.getText())));

                results.clear();
                panel.getRoot().layout();
                assertTrue(placeholder.getParent().isVisible());
                Label searchLabel = (Label) panel.getRoot().lookup("#recentFindSearchLabel");
                assertEquals("Search: find Amy", searchLabel.getText());
            } finally {
                stage.hide();
            }
        });
    }

    @SuppressWarnings("unchecked")
    private ListView<Person> getListView(RecentFindPanel panel) {
        return (ListView<Person>) panel.getRoot().lookup("#recentFindListView");
    }
}
