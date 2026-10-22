package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import java.util.List;
import java.util.Locale;

import org.junit.jupiter.api.Test;

import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.person.Person;
import seedu.address.model.person.ProjectContainsKeywordPredicate;
import seedu.address.testutil.PersonBuilder;

public class ListProjectsCommandTest {

    private final ListProjectsCommand command = new ListProjectsCommand();

    @Test
    public void execute_nullModel_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> command.execute(null));
    }

    @Test
    public void execute_emptyAddressBook_showsNoProjectsMessage() {
        Model model = new ModelManager();

        assertEquals(new CommandResult("No projects saved yet."), command.execute(model));
        assertTrue(model.getFilteredPersonList().isEmpty());
    }

    @Test
    public void execute_contactsWithoutProjects_showsNoProjectsMessage() {
        Model model = modelWith(PersonBuilder.named("Alice").withProjects().build(),
                PersonBuilder.named("Bob").withProjects().build());

        assertEquals(new CommandResult("No projects saved yet."), command.execute(model));
    }

    @Test
    public void execute_oneProjectOneContact_usesSingularWording() {
        Model model = modelWith(PersonBuilder.named("Alice").withProjects("Orbital").build());

        assertEquals(new CommandResult("Listed 1 project across all contacts:\n"
                + "- Orbital (1 contact)"), command.execute(model));
    }

    @Test
    public void execute_severalProjects_sortsAlphabeticallyIgnoringCaseWithCounts() {
        Model model = modelWith(
                PersonBuilder.named("Alice").withProjects("Orbital", "CS2101 OP").build(),
                PersonBuilder.named("Bob").withProjects("hackathon 2026", "Orbital").build(),
                PersonBuilder.named("Carl").withProjects("CS2101 OP", "Orbital").build(),
                PersonBuilder.named("Dana").withProjects().build());

        assertEquals(new CommandResult("Listed 3 projects across all contacts:\n"
                + "- CS2101 OP (2 contacts)\n"
                + "- hackathon 2026 (1 contact)\n"
                + "- Orbital (3 contacts)"), command.execute(model));
    }

    @Test
    public void execute_caseVariants_mergedUsingFirstSavedSpelling() {
        Model model = modelWith(
                PersonBuilder.named("Alice").withProjects("Orbital", "CS2103T tP").build(),
                PersonBuilder.named("Bob").withProjects("cs2103t TP").build(),
                PersonBuilder.named("Carl").withProjects("CS2103T TP").build());

        assertEquals(new CommandResult("Listed 2 projects across all contacts:\n"
                + "- CS2103T tP (3 contacts)\n"
                + "- Orbital (1 contact)"), command.execute(model));
    }

    @Test
    public void execute_firstSpellingFollowsSavedContactOrder() {
        Model model = modelWith(
                PersonBuilder.named("Bob").withProjects("cs2103t tp").build(),
                PersonBuilder.named("Alice").withProjects("CS2103T tP").build());

        assertEquals(new CommandResult("Listed 1 project across all contacts:\n"
                + "- cs2103t tp (2 contacts)"), command.execute(model));
    }

    @Test
    public void execute_filteredList_countsHiddenContactsAndKeepsFilter() {
        Person alice = PersonBuilder.named("Alice").withProjects("Orbital").build();
        Person bob = PersonBuilder.named("Bob").withProjects("CS2103T", "Orbital").build();
        Model model = modelWith(alice, bob);
        model.updateFilteredPersonList(new ProjectContainsKeywordPredicate("cs2103"));
        AddressBook original = new AddressBook(model.getAddressBook());

        CommandResult result = command.execute(model);

        assertEquals(new CommandResult("Listed 2 projects across all contacts:\n"
                + "- CS2103T (1 contact)\n"
                + "- Orbital (2 contacts)"), result);
        assertEquals(List.of(bob), model.getFilteredPersonList());
        assertEquals(original, model.getAddressBook());
    }

    @Test
    public void execute_symbolsAndUnicode_listedWithoutError() {
        Model model = modelWith(
                PersonBuilder.named("Alice").withProjects("Zeta", "数据 Project").build(),
                PersonBuilder.named("Bob").withProjects("#1 Team!").build());

        assertEquals(new CommandResult("Listed 3 projects across all contacts:\n"
                + "- #1 Team! (1 contact)\n"
                + "- Zeta (1 contact)\n"
                + "- 数据 Project (1 contact)"), command.execute(model));
    }

    @Test
    public void execute_whitespaceVariants_groupedAsOneProject() {
        Model model = modelWith(
                PersonBuilder.named("Alice").withProjects("Team Alpha").build(),
                PersonBuilder.named("Bob").withProjects(" team   alpha ").build());

        assertEquals(new CommandResult("Listed 1 project across all contacts:\n"
                + "- Team Alpha (2 contacts)"), command.execute(model));
    }

    @Test
    public void execute_nonLatinDigitFormatLocale_usesAsciiDigits() {
        Locale originalFormatLocale = Locale.getDefault(Locale.Category.FORMAT);
        try {
            Locale.setDefault(Locale.Category.FORMAT, Locale.forLanguageTag("ar-EG"));
            Model model = modelWith(PersonBuilder.named("Alice").withProjects("Orbital").build());

            assertEquals(new CommandResult("Listed 1 project across all contacts:\n"
                    + "- Orbital (1 contact)"), command.execute(model));
        } finally {
            Locale.setDefault(Locale.Category.FORMAT, originalFormatLocale);
        }
    }

    @Test
    public void isUndoable_returnsFalse() {
        assertFalse(command.isUndoable());
    }

    @Test
    public void equals() {
        assertTrue(command.equals(command));
        assertTrue(command.equals(new ListProjectsCommand()));
        assertEquals(command.hashCode(), new ListProjectsCommand().hashCode());
        assertFalse(command.equals(new ListCommand()));
        assertFalse(command.equals(null));
    }

    private static Model modelWith(Person... persons) {
        Model model = new ModelManager();
        for (Person person : persons) {
            model.addPerson(person);
        }
        return model;
    }
}
