package seedu.address.ui;

import java.util.logging.Logger;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;
import seedu.address.commons.core.LogsCenter;

/**
 * Controller for a help page
 */
public class HelpWindow extends UiPart<Stage> {

    public static final String USERGUIDE_URL = "https://ay2627s1-cs2103t-w14-2.github.io/tp/UserGuide.html";
    public static final String HELP_MESSAGE = "LinkUp command reference\n\n"
            + "COMMAND: add n/NAME p/PHONE e/EMAIL tele/TELEGRAM_USERNAME a/ADDRESS [pr/PROJECT] [t/TAG]\n"
            + "Save a contact with their phone, email, Telegram username and project.\n\n"
            + "COMMAND: list\n"
            + "Show all saved contacts and their projects.\n\n"
            + "COMMAND: delete INDEX\n"
            + "Remove the contact at the given index in the displayed list.\n\n"
            + "COMMAND: find NAME\n"
            + "Search contacts by all or part of a name (case-insensitive).\n\n"
            + "COMMAND: project INDEX pr/PROJECT\n"
            + "Associate the contact at the given index with another project.\n\n"
            + "COMMAND: findproject KEYWORD\n"
            + "Search contacts by all or part of a project name (case-insensitive).\n\n"
            + "Refer to the user guide: " + USERGUIDE_URL;

    private static final Logger logger = LogsCenter.getLogger(HelpWindow.class);
    private static final String FXML = "HelpWindow.fxml";

    @FXML
    private Button copyButton;

    @FXML
    private Label helpMessage;

    /**
     * Creates a new HelpWindow.
     *
     * @param root Stage to use as the root of the HelpWindow.
     */
    public HelpWindow(Stage root) {
        super(FXML, root);
        HBox container = (HBox) getRoot().getScene().getRoot();
        container.getChildren().clear();

        VBox content = new VBox(12);
        content.setPrefWidth(600);
        String[] sections = HELP_MESSAGE.split("\n\n");
        for (int i = 0; i < sections.length - 1; i++) {
            content.getChildren().add(createHelpSection(sections[i]));
        }

        helpMessage.setText(sections[sections.length - 1]);
        HBox guideRow = new HBox(10, helpMessage, copyButton);
        guideRow.setAlignment(Pos.CENTER_LEFT);
        content.getChildren().add(guideRow);
        container.getChildren().add(content);
    }

    /**
     * Creates a new HelpWindow.
     */
    public HelpWindow() {
        this(new Stage());
    }

    /**
     * Creates a help section with a bold command label where applicable.
     */
    private TextFlow createHelpSection(String section) {
        TextFlow flow = new TextFlow();
        if (section.startsWith("COMMAND:")) {
            Text commandLabel = new Text("COMMAND:");
            commandLabel.setFont(Font.font(helpMessage.getFont().getFamily(), FontWeight.BOLD,
                    helpMessage.getFont().getSize()));
            commandLabel.setFill(Color.WHITE);
            flow.getChildren().add(commandLabel);
            section = section.substring("COMMAND:".length());
        }
        Text text = new Text(section);
        text.setFont(helpMessage.getFont());
        text.setFill(Color.WHITE);
        flow.getChildren().add(text);
        return flow;
    }

    /**
     * Shows the help window.
     * @throws IllegalStateException
     *     <ul>
     *         <li>
     *             if this method is called on a thread other than the JavaFX Application Thread.
     *         </li>
     *         <li>
     *             if this method is called during animation or layout processing.
     *         </li>
     *         <li>
     *             if this method is called on the primary stage.
     *         </li>
     *         <li>
     *             if {@code dialogStage} is already showing.
     *         </li>
     *     </ul>
     */
    public void show() {
        logger.fine("Showing help page about the application.");
        getRoot().show();
        getRoot().centerOnScreen();
    }

    /**
     * Returns true if the help window is currently being shown.
     */
    public boolean isShowing() {
        return getRoot().isShowing();
    }

    /**
     * Hides the help window.
     */
    public void hide() {
        getRoot().hide();
    }

    /**
     * Focuses on the help window.
     */
    public void focus() {
        getRoot().requestFocus();
    }

    /**
     * Copies the URL to the user guide to the clipboard.
     */
    @FXML
    private void copyUrl() {
        final Clipboard clipboard = Clipboard.getSystemClipboard();
        final ClipboardContent url = new ClipboardContent();
        url.putString(USERGUIDE_URL);
        clipboard.setContent(url);
    }
}
