package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Region;
import javafx.stage.Stage;
import seedu.address.logic.Logic;
import seedu.address.logic.LogicManager;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.FxTestUtil;
import seedu.address.testutil.PersonBuilder;

public class RenameProjectUiTest {
    @TempDir
    public Path temporaryFolder;

    @Test
    public void rename_caseOnlyAndUndo_refreshDisplayedProjectLabels() throws Exception {
        FxTestUtil.run(() -> {
            Model model = new ModelManager();
            model.addPerson(PersonBuilder.named("Alice").withProjects("orbital").build());
            model.addPerson(PersonBuilder.named("Bob").withProjects("ORBITAL").build());
            Logic logic = new LogicManager(model, new StorageManager(
                    new JsonAddressBookStorage(temporaryFolder.resolve("contacts.json")),
                    new JsonUserPrefsStorage(temporaryFolder.resolve("preferences.json"))));
            Region root = new PersonListPanel(model.getFilteredPersonList()).getRoot();
            Stage stage = new Stage();
            stage.setScene(new Scene(root, 600, 450));
            try {
                stage.show();
                assertEquals(List.of("Project: ORBITAL", "Project: orbital"), projectLabels(root));
                logic.execute("renameproject old/orbital new/Orbital");
                assertEquals(List.of("Project: Orbital", "Project: Orbital"), projectLabels(root));
                logic.execute("undo");
                assertEquals(List.of("Project: ORBITAL", "Project: orbital"), projectLabels(root));
            } catch (Exception e) {
                throw new AssertionError(e);
            } finally {
                stage.close();
            }
        });
    }

    private List<String> projectLabels(Region root) {
        root.applyCss();
        root.layout();
        return root.lookupAll("#projects").stream().map(FlowPane.class::cast)
                .flatMap(pane -> pane.getChildren().stream()).map(Label.class::cast)
                .map(Label::getText).sorted().toList();
    }
}
