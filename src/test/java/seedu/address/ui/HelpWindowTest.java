package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import seedu.address.testutil.FxTestUtil;

public class HelpWindowTest {
    @Test
    public void help_smallWindowAllowsAccessToAllCommandsAndGuide() throws Exception {
        FxTestUtil.run(() -> {
            HelpWindow window = new HelpWindow();
            try {
                window.show();
                window.getRoot().setWidth(450);
                window.getRoot().setHeight(350);
                window.getRoot().getScene().getRoot().applyCss();
                window.getRoot().getScene().getRoot().layout();

                ScrollPane scroll = (ScrollPane) window.getRoot().getScene().lookup("#helpScrollPane");
                VBox content = (VBox) scroll.getContent();
                String displayedText = content.getChildren().stream()
                        .filter(TextFlow.class::isInstance).map(TextFlow.class::cast)
                        .flatMap(flow -> flow.getChildren().stream()).map(Text.class::cast)
                        .map(Text::getText).reduce("", String::concat);
                for (String command : List.of("add", "edit", "list", "delete", "find", "project", "findproject",
                        "deleteproject", "projects", "clear", "undo", "help", "exit")) {
                    assertTrue(displayedText.contains("COMMAND: " + command + " ")
                            || displayedText.contains("COMMAND: " + command + "\n"), command);
                }
                assertTrue(displayedText.contains("COMMAND: edit INDEX [n/NAME] [p/PHONE] [e/EMAIL] "
                        + "[tele/TELEGRAM_USERNAME]"));
                assertFalse(displayedText.contains("outside the MVP scope"));
                assertTrue(content.getBoundsInLocal().getHeight() > scroll.getViewportBounds().getHeight());

                scroll.setVvalue(scroll.getVmax());
                window.getRoot().getScene().getRoot().layout();
                Button copy = (Button) content.lookup("#copyButton");
                assertTrue(scroll.localToScene(scroll.getBoundsInLocal())
                        .contains(copy.localToScene(copy.getBoundsInLocal())));
            } finally {
                window.hide();
            }
        });
    }
}
