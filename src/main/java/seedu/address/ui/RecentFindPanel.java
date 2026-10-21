package seedu.address.ui;

import javafx.beans.binding.Bindings;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
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
     * Displays contact details and project tags without command indices, and provides a tooltip with the search text.
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
             * Displays the current person's details, clearing previous content when the cell becomes empty.
             */
            @Override
            protected void updateItem(Person person, boolean empty) {
                super.updateItem(person, empty);
                setText(null);
                setGraphic(null);
                if (!empty && person != null) {
                    VBox details = createPersonDetails(person);
                    // Reserve the cell's horizontal padding so project tags wrap within the available width.
                    details.prefWidthProperty().bind(Bindings.max(0, widthProperty().subtract(20)));
                    setGraphic(details);
                }
            }
        });
    }

    /**
     * Creates compact contact details with the same project tag styling as a {@code PersonCard}.
     * Displays fallback text for missing Telegram usernames and hides empty project lists.
     */
    private VBox createPersonDetails(Person person) {
        String telegramHandle = person.getTelegramUsername().isEmpty()
                ? "No Telegram Username" : person.getTelegramUsername().toString();
        Label contact = new Label(person.getName().fullName + "  |  " + telegramHandle);
        contact.setId("recentFindContact");
        contact.getStyleClass().add("cell_small_label");
        contact.setMinWidth(0);
        contact.setMaxWidth(Double.MAX_VALUE);
        contact.setTooltip(new Tooltip(contact.getText()));

        FlowPane projects = new FlowPane();
        projects.setId("projects");
        projects.setMinWidth(0);
        person.getProjects().forEach(project ->
                projects.getChildren().add(new Label("Project: " + project.projectName)));
        projects.setVisible(!person.getProjects().isEmpty());
        projects.setManaged(projects.isVisible());

        VBox details = new VBox(2, contact, projects);
        details.setMinWidth(0);
        return details;
    }
}
