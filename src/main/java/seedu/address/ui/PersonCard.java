package seedu.address.ui;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import seedu.address.model.person.Person;
import seedu.address.model.project.Project;

/**
 * A UI component that displays information of a {@code Person}.
 */
public class PersonCard extends UiPart<Region> {

    private static final String FXML = "PersonListCard.fxml";

    /**
     * Note: Certain keywords such as "location" and "resources" are reserved keywords in JavaFX.
     * As a consequence, UI elements' variable names cannot be set to such keywords
     * or an exception will be thrown by JavaFX during runtime.
     *
     * @see <a href="https://github.com/se-edu/addressbook-level4/issues/336">The issue on AddressBook level 4</a>
     */

    public final Person person;

    @FXML
    private HBox cardPane;
    @FXML
    private Label name;
    @FXML
    private Label id;
    @FXML
    private Label phone;
    @FXML
    private Label email;
    @FXML
    private Label telegramUsername;
    @FXML
    private FlowPane projects;

    /**
     * Creates a {@code PersonCard} with the given {@code Person} and index to display.
     */
    public PersonCard(Person person, int displayedIndex) {
        super(FXML);
        this.person = person;
        id.setText(displayedIndex + ". ");
        name.setText(person.getName().fullName);
        phone.setText(person.getPhone().value);
        email.setText(person.getEmail().value);
        String telegramUsernameText = person.getTelegramUsername().toString();
        telegramUsername.setText(telegramUsernameText);
        telegramUsername.setManaged(!telegramUsernameText.isEmpty());
        telegramUsername.setVisible(!telegramUsernameText.isEmpty());
        person.getProjects().forEach(project -> projects.getChildren().add(createProjectTag(project)));
    }

    /**
     * Creates a tag for {@code project} that fits within the card. A project name too long for the card ends
     * in an ellipsis, and the tag's tooltip shows the full name.
     */
    private Label createProjectTag(Project project) {
        Label tag = new Label("Project: " + project.projectName);
        tag.setMinWidth(0);
        tag.maxWidthProperty().bind(projects.widthProperty());
        tag.setTooltip(new Tooltip(tag.getText()));
        return tag;
    }
}
