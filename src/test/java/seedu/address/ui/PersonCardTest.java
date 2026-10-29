package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import seedu.address.model.person.Person;
import seedu.address.testutil.FxTestUtil;
import seedu.address.testutil.PersonBuilder;

public class PersonCardTest {
    private static final double NARROW_CARD_WIDTH = 300;
    private static final double CARD_HEIGHT = 400;

    @Test
    public void longNameAndEmail_narrowCard_wrapWithoutTruncation() throws Exception {
        String longName = "W".repeat(60);
        String longEmail = "a".repeat(60) + "@example.com";
        Person person = new PersonBuilder().withName(longName).withEmail(longEmail).build();
        FxTestUtil.run(() -> {
            Parent card = layOutCard(new PersonCard(person, 1));
            Label name = (Label) card.lookup("#name");
            Label email = (Label) card.lookup("#email");
            assertDisplaysFullText(name, longName);
            assertDisplaysFullText(email, longEmail);
            Label index = (Label) card.lookup("#id");
            assertEquals("1. ", displayedText(index));
            assertTrue(name.getHeight() > index.getHeight());
        });
    }

    /** Places the card in a narrow scene and lays it out, as a short window would. */
    private Parent layOutCard(PersonCard personCard) {
        StackPane root = new StackPane(personCard.getRoot());
        new Scene(root, NARROW_CARD_WIDTH, CARD_HEIGHT);
        root.applyCss();
        root.layout();
        return root;
    }

    /** Asserts that {@code label} renders all of {@code expected}, wrapping instead of using an ellipsis. */
    private void assertDisplaysFullText(Label label, String expected) {
        assertEquals(expected, label.getText());
        assertEquals(expected, displayedText(label));
    }

    /** Returns the text JavaFX actually renders, which ends in an ellipsis when the label is truncated. */
    private String displayedText(Label label) {
        return ((Text) label.lookup(".text")).getText();
    }
}
