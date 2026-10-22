package seedu.address.ui;

import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import seedu.address.model.person.Person;

/** Compact contact details shared by latest search results and the most searched contact. */
public class SearchContactDetails extends VBox {
    /** Creates details without command indices, with wrapping project tags and a full contact tooltip. */
    public SearchContactDetails(Person person, String contactId) {
        super(2);
        String telegram = person.getTelegramUsername().isEmpty()
                ? "No Telegram Username" : person.getTelegramUsername().toString();
        Label contact = new Label(person.getName().fullName + "  |  " + telegram);
        contact.setId(contactId);
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
        setMinWidth(0);
        getChildren().addAll(contact, projects);
    }
}
