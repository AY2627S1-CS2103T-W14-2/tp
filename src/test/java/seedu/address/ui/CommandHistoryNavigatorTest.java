package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

public class CommandHistoryNavigatorTest {
    @Test
    public void navigation_stopsAtOldestAndRestoresDraft() {
        CommandHistoryNavigator navigator = new CommandHistoryNavigator(() -> List.of("list", "bad", "list"));
        assertEquals("draft", navigator.next("draft"));
        assertEquals("list", navigator.previous("draft"));
        assertEquals("bad", navigator.previous("list"));
        assertEquals("list", navigator.previous("bad"));
        assertEquals("list", navigator.previous("list"));
        assertEquals("bad", navigator.next("edited recalled command"));
        assertEquals("list", navigator.next("bad"));
        assertEquals("draft", navigator.next("list"));
        assertEquals("new draft", navigator.next("new draft"));
        assertEquals("list", navigator.previous("new draft"));
        assertEquals("new draft", navigator.next("list"));
    }

    @Test
    public void emptyHistoryAndReset_preserveDraftAndSeeNewSubmissions() {
        List<String> entries = new ArrayList<>();
        CommandHistoryNavigator navigator = new CommandHistoryNavigator(() -> entries);
        assertEquals("draft", navigator.previous("draft"));
        assertEquals("draft", navigator.next("draft"));
        entries.add("list");
        assertEquals("list", navigator.previous("draft"));
        entries.add("bad");
        navigator.reset();
        assertEquals("bad", navigator.previous("failed input"));
        assertEquals("failed input", navigator.next("bad"));
    }
}
