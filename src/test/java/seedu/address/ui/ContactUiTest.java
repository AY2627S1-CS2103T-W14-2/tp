package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.Person;
import seedu.address.testutil.FxTestUtil;
import seedu.address.testutil.PersonBuilder;

public class ContactUiTest {
    @Test
    public void contactCard_displaysDetailsAndOrderedProjects() throws Exception {
        FxTestUtil.run(() -> {
            Person person = new PersonBuilder().withProjects("Orbital", "CS2103T").build();
            PersonCard card = new PersonCard(person, 2);
            assertEquals("2. ", ((Label) card.getRoot().lookup("#id")).getText());
            assertEquals(person.getName().fullName, ((Label) card.getRoot().lookup("#name")).getText());
            assertEquals(person.getPhone().value, ((Label) card.getRoot().lookup("#phone")).getText());
            assertEquals(person.getEmail().value, ((Label) card.getRoot().lookup("#email")).getText());
            Label telegram = (Label) card.getRoot().lookup("#telegramUsername");
            assertEquals("@amy_bee", telegram.getText());
            assertTrue(telegram.isVisible());
            FlowPane projects = (FlowPane) card.getRoot().lookup("#projects");
            assertEquals(List.of("Project: Orbital", "Project: CS2103T"), projects.getChildren().stream()
                    .map(node -> ((Label) node).getText()).toList());

            PersonCard legacy = new PersonCard(new PersonBuilder().withoutTelegramUsername().build(), 1);
            Label missing = (Label) legacy.getRoot().lookup("#telegramUsername");
            assertFalse(missing.isVisible());
            assertFalse(missing.isManaged());
        });
    }

    @Test
    public void listCell_reuseClearsPreviousContact() throws Exception {
        FxTestUtil.run(() -> {
            Person person = new PersonBuilder().build();
            PersonListPanel panel = new PersonListPanel(FXCollections.observableArrayList(person));
            PersonListPanel.PersonListViewCell cell = panel.new PersonListViewCell();
            cell.updateItem(person, false);
            assertNotNull(cell.getGraphic());
            cell.updateItem(null, false);
            assertNull(cell.getGraphic());
            cell.updateItem(person, false);
            cell.updateItem(person, true);
            assertNull(cell.getGraphic());
            assertNull(cell.getText());
        });
    }

    @Test
    public void commandBox_failureKeepsInputAndEditingClearsError() throws Exception {
        FxTestUtil.run(() -> {
            List<String> executed = new ArrayList<>();
            CommandBox box = new CommandBox(command -> {
                executed.add(command);
                if (command.equals("bad")) {
                    throw new ParseException("Invalid command");
                }
                return new CommandResult("Done");
            });
            TextField input = (TextField) box.getRoot().lookup("#commandTextField");
            input.fireEvent(new ActionEvent());
            assertTrue(executed.isEmpty());
            input.setText("bad");
            input.fireEvent(new ActionEvent());
            input.fireEvent(new ActionEvent());
            assertEquals("bad", input.getText());
            assertEquals(1, input.getStyleClass().stream().filter("error"::equals).count());
            input.setText("list");
            assertFalse(input.getStyleClass().contains("error"));
            input.fireEvent(new ActionEvent());
            assertEquals("", input.getText());
            assertEquals(List.of("bad", "bad", "list"), executed);
        });
    }
}
