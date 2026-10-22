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
import javafx.scene.layout.VBox;
import seedu.address.model.person.Person;

/**
 * Compact panel showing contacts matching the latest name or project search and the search text.
 */
public class RecentFindPanel extends UiPart<Region> {
    private static final String FXML = "RecentFindPanel.fxml";

    @FXML
    private ListView<Person> recentFindListView;

    @FXML
    private Label recentFindSearchLabel;

    /**
     * Creates a {@code RecentFindPanel} bound to the given results and search property.
     * Displays contact details and project tags without command indices, and provides a tooltip with the search text.
     *
     * @param recentFindResults Observable list of contacts matching the latest search.
     * @param search Read-only search property, containing an empty string before the first search.
     */
    public RecentFindPanel(ObservableList<Person> recentFindResults, ReadOnlyStringProperty search) {
        super(FXML);
        recentFindSearchLabel.textProperty().bind(Bindings.when(search.isEmpty())
                .then("Latest search: No search yet").otherwise(Bindings.concat("Latest search: ", search)));
        Tooltip searchTooltip = new Tooltip();
        searchTooltip.textProperty().bind(recentFindSearchLabel.textProperty());
        recentFindSearchLabel.setTooltip(searchTooltip);
        recentFindListView.setItems(recentFindResults);
        Label placeholder = new Label();
        placeholder.textProperty().bind(Bindings.when(search.isEmpty())
                .then("Run a name or project search to see matching contacts.")
                .otherwise("No contacts match the latest search."));
        placeholder.setWrapText(true);
        recentFindListView.setPlaceholder(placeholder);
        recentFindListView.setCellFactory(listView -> new ListCell<>() {
            /**
             * Displays the current person's details, clearing previous content when the cell becomes empty.
             */
            @Override
            protected void updateItem(Person person, boolean empty) {
                super.updateItem(person, empty);
                setText(null);
                setGraphic(null);
                if (!empty && person != null) {
                    VBox details = new SearchContactDetails(person, "recentFindContact");
                    // Reserve the cell's horizontal padding so project tags wrap within the available width.
                    details.prefWidthProperty().bind(Bindings.max(0, widthProperty().subtract(20)));
                    setGraphic(details);
                }
            }
        });
    }
}
