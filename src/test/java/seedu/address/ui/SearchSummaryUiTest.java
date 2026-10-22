package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.TypicalPersons.ALICE;

import java.nio.file.Path;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import javafx.stage.Stage;
import seedu.address.logic.Logic;
import seedu.address.logic.LogicManager;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.person.Person;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.FxTestUtil;
import seedu.address.testutil.PersonBuilder;

public class SearchSummaryUiTest {
    @TempDir
    public Path temporaryFolder;

    @Test
    public void searchSummary_bindsVisibilityAndStaysCompactAtMinimumWidth() throws Exception {
        FutureTask<Void> task = new FutureTask<>(() -> {
            Model model = new ModelManager();
            Person person = new PersonBuilder(ALICE).withName("Alexandria ".repeat(5).strip())
                    .withProjects("Orbital", "CS2103T").build();
            model.addPerson(person);
            Logic logic = new LogicManager(model, new StorageManager(
                    new JsonAddressBookStorage(temporaryFolder.resolve("contacts.json")),
                    new JsonUserPrefsStorage(temporaryFolder.resolve("prefs.json"))));
            Stage stage = new Stage();
            try {
                MainWindow window = new MainWindow(stage, logic, temporaryFolder.resolve("contacts.json"));
                window.fillInnerParts();
                Parent root = stage.getScene().getRoot();
                Region summary = (Region) root.lookup("#searchSummary");
                Region mostSearchedRow = (Region) root.lookup("#mostSearchedRow");
                Label name = (Label) root.lookup("#mostSearchedName");
                Label recent = (Label) root.lookup("#recentSearch");
                assertFalse(summary.isVisible());
                assertFalse(summary.isManaged());

                logic.execute("find missing");
                assertTrue(summary.isVisible());
                assertTrue(summary.isManaged());
                assertFalse(mostSearchedRow.isVisible());
                assertFalse(mostSearchedRow.isManaged());
                assertEquals("Name: missing", recent.getText());

                logic.execute("find Alexandria");
                assertEquals("Name: Alexandria", recent.getText());
                assertEquals("Name: Alexandria", recent.getTooltip().getText());
                root.resize(450, 600);
                root.applyCss();
                root.layout();
                assertTrue(mostSearchedRow.isVisible());
                assertTrue(mostSearchedRow.isManaged());
                String details = person.getName().fullName
                        + " | Projects: Orbital, CS2103T | Telegram: @alice_pauline";
                assertEquals(details, name.getText());
                assertEquals(details, name.getTooltip().getText());
                assertTrue(name.getWidth() > 0);
                assertTrue(name.getWidth() < name.prefWidth(-1));
                assertTrue(summary.getHeight() <= 70);
                assertTrue(mostSearchedRow.getBoundsInParent().getMaxY() <= recent.getParent().getLayoutY());

                logic.execute("edit 1 n/Alicia");
                String editedDetails = "Alicia | Projects: Orbital, CS2103T | Telegram: @alice_pauline";
                assertEquals(editedDetails, name.getText());
                assertEquals(editedDetails, name.getTooltip().getText());
                logic.execute("findproject Orbital");
                assertEquals(editedDetails, name.getText());
                assertEquals("Project: Orbital", recent.getText());
                logic.execute("clear");
                assertFalse(mostSearchedRow.isManaged());
                assertEquals("Project: Orbital", recent.getText());
                assertEquals("Project: Orbital", recent.getTooltip().getText());
            } finally {
                stage.close();
            }
            return null;
        });
        FxTestUtil.run(task);
        task.get(30, TimeUnit.SECONDS);
    }
}
