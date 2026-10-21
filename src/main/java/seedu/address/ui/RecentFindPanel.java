package seedu.address.ui;

import javafx.beans.binding.Bindings;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.Region;
import seedu.address.model.person.Person;

/**
 * Compact panel showing contacts matching the latest name or project search and the search command text.
 */
public class RecentFindPanel extends UiPart<Region> {
    private static final String FXML = "RecentFindPanel.fxml";

    @FXML
    private ListView<Person> recentFindListView;

    @FXML
    private Label recentFindSearchLabel;

    /**
     * Creates a {@code RecentFindPanel} bound to the given results and search command property.
     * Displays names and email addresses without command indices, and provides a tooltip with the full search text.
     *
     * @param recentFindResults Observable list of contacts matching the latest search.
     * @param search Read-only search command property, containing an empty string before the first search.
     */
    public RecentFindPanel(ObservableList<Person> recentFindResults, ReadOnlyStringProperty search) {
        super(FXML);
        recentFindSearchLabel.textProperty().bind(Bindings.when(search.isEmpty())
                .then("No search yet").otherwise(Bindings.concat("Search: ", search)));
        Tooltip searchTooltip = new Tooltip();
        searchTooltip.textProperty().bind(recentFindSearchLabel.textProperty());
        recentFindSearchLabel.setTooltip(searchTooltip);
        recentFindListView.setItems(recentFindResults);
        recentFindListView.setPlaceholder(new Label("No recent find results."));
        recentFindListView.setCellFactory(listView -> new ListCell<>() {
            /**
             * Displays the current person's name and email, clearing previous content when the cell becomes empty.
             */
            @Override
            protected void updateItem(Person person, boolean empty) {
                super.updateItem(person, empty);
                setGraphic(null);
                setText(empty || person == null ? null
                        : person.getName().fullName + "  |  " + person.getEmail().value);
            }
        });
    }
}
