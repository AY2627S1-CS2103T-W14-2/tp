package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.DESC_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_NAME_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_PHONE_BOB;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.logic.commands.CommandTestUtil.showPersonAtIndex;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_PERSON;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import java.util.List;
import java.util.Locale;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.EditCommand.EditPersonDescriptor;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.ContactConflict;
import seedu.address.model.person.ContactConflict.Field;
import seedu.address.model.person.Person;
import seedu.address.testutil.EditPersonDescriptorBuilder;
import seedu.address.testutil.PersonBuilder;

/**
 * Contains integration tests (interaction with the Model) and unit tests for EditCommand.
 */
public class EditCommandTest {

    /**
     * One entry per editable contact detail that must be unique across contacts.
     * Add an entry when another unique detail (e.g. the Telegram username) becomes editable.
     */
    private static final List<UniqueFieldEdit> UNIQUE_FIELD_EDITS = List.of(
            new UniqueFieldEdit(Field.PHONE, person -> person.getPhone().value,
                    EditPersonDescriptorBuilder::withPhone),
            new UniqueFieldEdit(Field.EMAIL, person -> person.getEmail().value,
                    EditPersonDescriptorBuilder::withEmail));

    private Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());

    /**
     * An edit that copies one unique contact detail from a source person.
     *
     * @param field The contact detail being edited.
     * @param valueOf Reads the detail's value, as the user would type it, from a person.
     * @param setter Sets the detail on a descriptor builder.
     */
    private record UniqueFieldEdit(Field field, Function<Person, String> valueOf,
            BiFunction<EditPersonDescriptorBuilder, String, EditPersonDescriptorBuilder> setter) {

        EditPersonDescriptor copyFrom(Person source) {
            return setter.apply(new EditPersonDescriptorBuilder(), valueOf.apply(source)).build();
        }

        String expectedMessage(Person existingPerson) {
            return new ContactConflict(field, valueOf.apply(existingPerson), existingPerson).getMessage();
        }
    }

    @Test
    public void execute_allFieldsSpecifiedUnfilteredList_success() {
        Person personToEdit = model.getFilteredPersonList().get(0);
        Person editedPerson = new PersonBuilder().withTelegramUsername(personToEdit.getTelegramUsername().value)
                .build();
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder(editedPerson).build();
        EditCommand editCommand = new EditCommand(INDEX_FIRST_PERSON, descriptor);

        String expectedMessage = String.format(EditCommand.MESSAGE_EDIT_PERSON_SUCCESS, Messages.format(editedPerson));

        Model expectedModel = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());
        expectedModel.setPerson(model.getFilteredPersonList().get(0), editedPerson);

        assertCommandSuccess(editCommand, model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_someFieldsSpecifiedUnfilteredList_success() {
        Index indexLastPerson = Index.fromOneBased(model.getFilteredPersonList().size());
        Person lastPerson = model.getFilteredPersonList().get(indexLastPerson.getZeroBased());

        PersonBuilder personInList = new PersonBuilder(lastPerson);
        Person editedPerson = personInList.withName(VALID_NAME_BOB).withPhone(VALID_PHONE_BOB)
                .build();

        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder().withName(VALID_NAME_BOB)
                .withPhone(VALID_PHONE_BOB).build();
        EditCommand editCommand = new EditCommand(indexLastPerson, descriptor);

        String expectedMessage = String.format(EditCommand.MESSAGE_EDIT_PERSON_SUCCESS, Messages.format(editedPerson));

        Model expectedModel = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());
        expectedModel.setPerson(lastPerson, editedPerson);

        assertCommandSuccess(editCommand, model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_editOtherField_preservesProjects() {
        Person personToEdit = model.getFilteredPersonList().get(INDEX_SECOND_PERSON.getZeroBased());
        Person editedPerson = new PersonBuilder(personToEdit).withPhone(VALID_PHONE_BOB).build();
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder().withPhone(VALID_PHONE_BOB).build();
        EditCommand editCommand = new EditCommand(INDEX_SECOND_PERSON, descriptor);

        String expectedMessage = String.format(EditCommand.MESSAGE_EDIT_PERSON_SUCCESS, Messages.format(editedPerson));
        Model expectedModel = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());
        expectedModel.setPerson(personToEdit, editedPerson);

        assertCommandSuccess(editCommand, model, expectedMessage, expectedModel);
        assertEquals(personToEdit.getProjects(), editedPerson.getProjects());
    }

    @Test
    public void execute_noFieldSpecifiedUnfilteredList_success() {
        EditCommand editCommand = new EditCommand(INDEX_FIRST_PERSON, new EditPersonDescriptor());
        Person editedPerson = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());

        String expectedMessage = String.format(EditCommand.MESSAGE_EDIT_PERSON_SUCCESS, Messages.format(editedPerson));

        Model expectedModel = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());

        assertCommandSuccess(editCommand, model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_filteredList_success() {
        showPersonAtIndex(model, INDEX_FIRST_PERSON);

        Person personInFilteredList = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        Person editedPerson = new PersonBuilder(personInFilteredList).withName(VALID_NAME_BOB).build();
        EditCommand editCommand = new EditCommand(INDEX_FIRST_PERSON,
                new EditPersonDescriptorBuilder().withName(VALID_NAME_BOB).build());

        String expectedMessage = String.format(EditCommand.MESSAGE_EDIT_PERSON_SUCCESS, Messages.format(editedPerson));

        Model expectedModel = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());
        expectedModel.setPerson(model.getFilteredPersonList().get(0), editedPerson);

        assertCommandSuccess(editCommand, model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_allFieldsOfAnotherPerson_failure() {
        Person firstPerson = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder(firstPerson).build();
        EditCommand editCommand = new EditCommand(INDEX_SECOND_PERSON, descriptor);

        // every detail clashes, so the phone number is reported first
        assertCommandFailure(editCommand, model,
                new ContactConflict(Field.PHONE, firstPerson.getPhone().value, firstPerson).getMessage());
    }

    @Test
    public void execute_uniqueFieldOfAnotherPerson_failure() {
        Person secondPerson = model.getFilteredPersonList().get(INDEX_SECOND_PERSON.getZeroBased());
        for (UniqueFieldEdit edit : UNIQUE_FIELD_EDITS) {
            EditCommand editCommand = new EditCommand(INDEX_FIRST_PERSON, edit.copyFrom(secondPerson));
            assertCommandFailure(editCommand, model, edit.expectedMessage(secondPerson));
        }
    }

    @Test
    public void execute_uniqueFieldOfHiddenPersonFilteredList_failure() {
        showPersonAtIndex(model, INDEX_FIRST_PERSON);
        Person hiddenPerson = model.getAddressBook().getPersonList().get(INDEX_SECOND_PERSON.getZeroBased());

        for (UniqueFieldEdit edit : UNIQUE_FIELD_EDITS) {
            EditCommand editCommand = new EditCommand(INDEX_FIRST_PERSON, edit.copyFrom(hiddenPerson));
            assertCommandFailure(editCommand, model, edit.expectedMessage(hiddenPerson));
        }
    }

    @Test
    public void execute_uniqueFieldSetToOwnValue_success() {
        Person firstPerson = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        String expectedMessage = String.format(EditCommand.MESSAGE_EDIT_PERSON_SUCCESS, Messages.format(firstPerson));
        Model expectedModel = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());

        for (UniqueFieldEdit edit : UNIQUE_FIELD_EDITS) {
            EditCommand editCommand = new EditCommand(INDEX_FIRST_PERSON, edit.copyFrom(firstPerson));
            assertCommandSuccess(editCommand, model, expectedMessage, expectedModel);
        }
    }

    @Test
    public void execute_emailOfAnotherPersonInDifferentCase_failure() {
        Person secondPerson = model.getFilteredPersonList().get(INDEX_SECOND_PERSON.getZeroBased());
        String upperCaseEmail = secondPerson.getEmail().value.toUpperCase(Locale.ROOT);
        EditCommand editCommand = new EditCommand(INDEX_FIRST_PERSON,
                new EditPersonDescriptorBuilder().withEmail(upperCaseEmail).build());

        assertCommandFailure(editCommand, model,
                new ContactConflict(Field.EMAIL, upperCaseEmail.toLowerCase(Locale.ROOT), secondPerson).getMessage());
    }

    @Test
    public void execute_nameOfAnotherPerson_success() {
        Person firstPerson = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        Person secondPerson = model.getFilteredPersonList().get(INDEX_SECOND_PERSON.getZeroBased());
        Person editedPerson = new PersonBuilder(secondPerson).withName(firstPerson.getName().fullName).build();
        EditCommand editCommand = new EditCommand(INDEX_SECOND_PERSON,
                new EditPersonDescriptorBuilder().withName(firstPerson.getName().fullName).build());

        String expectedMessage = String.format(EditCommand.MESSAGE_EDIT_PERSON_SUCCESS, Messages.format(editedPerson));
        Model expectedModel = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());
        expectedModel.setPerson(secondPerson, editedPerson);

        assertCommandSuccess(editCommand, model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_invalidPersonIndexUnfilteredList_failure() {
        Index outOfBoundIndex = Index.fromOneBased(model.getFilteredPersonList().size() + 1);
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder().withName(VALID_NAME_BOB).build();
        EditCommand editCommand = new EditCommand(outOfBoundIndex, descriptor);

        assertCommandFailure(editCommand, model, Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    /**
     * Edit filtered list where index is larger than size of filtered list,
     * but smaller than size of address book
     */
    @Test
    public void execute_invalidPersonIndexFilteredList_failure() {
        showPersonAtIndex(model, INDEX_FIRST_PERSON);
        Index outOfBoundIndex = INDEX_SECOND_PERSON;
        // ensures that outOfBoundIndex is still in bounds of address book list
        assertTrue(outOfBoundIndex.getZeroBased() < model.getAddressBook().getPersonList().size());

        EditCommand editCommand = new EditCommand(outOfBoundIndex,
                new EditPersonDescriptorBuilder().withName(VALID_NAME_BOB).build());

        assertCommandFailure(editCommand, model, Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    @Test
    public void equals() {
        final EditCommand standardCommand = new EditCommand(INDEX_FIRST_PERSON, DESC_AMY);

        // same values -> returns true
        EditPersonDescriptor copyDescriptor = new EditPersonDescriptor(DESC_AMY);
        EditCommand commandWithSameValues = new EditCommand(INDEX_FIRST_PERSON, copyDescriptor);
        assertTrue(standardCommand.equals(commandWithSameValues));

        // same object -> returns true
        assertTrue(standardCommand.equals(standardCommand));

        // null -> returns false
        assertFalse(standardCommand.equals(null));

        // different types -> returns false
        assertFalse(standardCommand.equals(new ClearCommand()));

        // different index -> returns false
        assertFalse(standardCommand.equals(new EditCommand(INDEX_SECOND_PERSON, DESC_AMY)));

        // different descriptor -> returns false
        assertFalse(standardCommand.equals(new EditCommand(INDEX_FIRST_PERSON, DESC_BOB)));
    }

    @Test
    public void toStringMethod() {
        Index index = Index.fromOneBased(1);
        EditPersonDescriptor editPersonDescriptor = new EditPersonDescriptor();
        EditCommand editCommand = new EditCommand(index, editPersonDescriptor);
        String expected = EditCommand.class.getCanonicalName() + "{index=" + index + ", editPersonDescriptor="
                + editPersonDescriptor + "}";
        assertEquals(expected, editCommand.toString());
    }

}
