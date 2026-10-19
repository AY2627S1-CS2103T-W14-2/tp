package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.DESC_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_EMAIL_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_NAME_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_PHONE_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_TELEGRAM_USERNAME_BOB;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.logic.commands.CommandTestUtil.showPersonAtIndex;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_PERSON;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import java.util.List;
import java.util.Locale;
import java.util.function.BiConsumer;
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
import seedu.address.model.person.Email;
import seedu.address.model.person.Name;
import seedu.address.model.person.Person;
import seedu.address.model.person.Phone;
import seedu.address.model.person.TelegramUsername;
import seedu.address.testutil.EditPersonDescriptorBuilder;
import seedu.address.testutil.PersonBuilder;

/**
 * Contains integration tests (interaction with the Model) and unit tests for EditCommand.
 */
public class EditCommandTest {

    /**
     * One entry per editable contact detail that must be unique across contacts.
     * Add an entry when another unique detail becomes editable.
     */
    private static final List<UniqueFieldEdit> UNIQUE_FIELD_EDITS = List.of(
            new UniqueFieldEdit(Field.PHONE, Person::getPhone, (descriptor, source) ->
                    descriptor.setPhone(source.getPhone())),
            new UniqueFieldEdit(Field.EMAIL, Person::getEmail, (descriptor, source) ->
                    descriptor.setEmail(source.getEmail())),
            new UniqueFieldEdit(Field.TELEGRAM_USERNAME, Person::getTelegramUsername, (descriptor, source) ->
                    descriptor.setTelegramUsername(source.getTelegramUsername())));

    /** A contact saved by an older version: local phone, mixed-case email, no Telegram username. */
    private static final Person LEGACY = new PersonBuilder().withName("Legacy Lee").withPhone("91234567")
            .withStoredEmail("Legacy.Lee@Example.COM").withoutTelegramUsername()
            .withProjects("Orbital", "CS2103T").build();

    private Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());

    /**
     * An edit that copies one unique contact detail from a source person.
     *
     * @param field The contact detail being edited.
     * @param detailOf Reads the detail from a person.
     * @param copier Sets the detail of a source person on a descriptor.
     */
    private record UniqueFieldEdit(Field field, Function<Person, Object> detailOf,
            BiConsumer<EditPersonDescriptor, Person> copier) {

        EditPersonDescriptor copyFrom(Person source) {
            EditPersonDescriptor descriptor = new EditPersonDescriptor();
            copier.accept(descriptor, source);
            return descriptor;
        }

        String expectedMessage(Person existingPerson) {
            return new ContactConflict(field, detailOf.apply(existingPerson).toString(), existingPerson)
                    .getMessage();
        }
    }

    private static String successMessage(Person editedPerson) {
        return String.format(EditCommand.MESSAGE_EDIT_PERSON_SUCCESS, Messages.format(editedPerson));
    }

    private Model copyOf(Model source) {
        return new ModelManager(new AddressBook(source.getAddressBook()), new UserPrefs());
    }

    private Index addLegacyPerson() {
        model.addPerson(LEGACY);
        return Index.fromOneBased(model.getFilteredPersonList().size());
    }

    private static void assertStoredValues(Person person, String name, String phone, String email,
            String telegramUsername) {
        assertEquals(name, person.getName().fullName);
        assertEquals(phone, person.getPhone().value);
        assertEquals(email, person.getEmail().value);
        assertEquals(telegramUsername, person.getTelegramUsername().value);
    }

    @Test
    public void execute_allFieldsSpecifiedUnfilteredList_success() {
        Person personToEdit = model.getFilteredPersonList().get(0);
        Person editedPerson = new PersonBuilder(personToEdit).withName(VALID_NAME_BOB).withPhone(VALID_PHONE_BOB)
                .withEmail(VALID_EMAIL_BOB).withTelegramUsername(VALID_TELEGRAM_USERNAME_BOB).build();
        EditPersonDescriptor descriptor = new EditPersonDescriptor();
        descriptor.setName(new Name(VALID_NAME_BOB));
        descriptor.setPhone(new Phone(VALID_PHONE_BOB));
        descriptor.setEmail(new Email(VALID_EMAIL_BOB));
        descriptor.setTelegramUsername(new TelegramUsername(VALID_TELEGRAM_USERNAME_BOB));
        EditCommand editCommand = new EditCommand(INDEX_FIRST_PERSON, descriptor);

        Model expectedModel = copyOf(model);
        expectedModel.setPerson(personToEdit, editedPerson);

        assertCommandSuccess(editCommand, model, successMessage(editedPerson), expectedModel);
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

        Model expectedModel = copyOf(model);
        expectedModel.setPerson(lastPerson, editedPerson);

        assertCommandSuccess(editCommand, model, successMessage(editedPerson), expectedModel);
    }

    @Test
    public void execute_eachFieldAlone_keepsOtherFieldsAndProjectOrder() {
        Person benson = model.getFilteredPersonList().get(INDEX_SECOND_PERSON.getZeroBased());
        assertTrue(benson.getProjects().size() >= 2);

        record FieldCase(String name, EditPersonDescriptor descriptor, Person expected) {}

        EditPersonDescriptor nameOnly = new EditPersonDescriptor();
        nameOnly.setName(new Name(VALID_NAME_BOB));
        EditPersonDescriptor phoneOnly = new EditPersonDescriptor();
        phoneOnly.setPhone(new Phone(VALID_PHONE_BOB));
        EditPersonDescriptor emailOnly = new EditPersonDescriptor();
        emailOnly.setEmail(new Email(VALID_EMAIL_BOB));
        EditPersonDescriptor telegramOnly = new EditPersonDescriptor();
        telegramOnly.setTelegramUsername(new TelegramUsername(VALID_TELEGRAM_USERNAME_BOB));

        List<FieldCase> cases = List.of(
                new FieldCase("name", nameOnly, new PersonBuilder(benson).withName(VALID_NAME_BOB).build()),
                new FieldCase("phone", phoneOnly, new PersonBuilder(benson).withPhone(VALID_PHONE_BOB).build()),
                new FieldCase("email", emailOnly, new PersonBuilder(benson).withEmail(VALID_EMAIL_BOB).build()),
                new FieldCase("telegram", telegramOnly,
                        new PersonBuilder(benson).withTelegramUsername(VALID_TELEGRAM_USERNAME_BOB).build()));

        for (FieldCase fieldCase : cases) {
            model = new ModelManager(getTypicalAddressBook(), new UserPrefs());
            Model expectedModel = copyOf(model);
            expectedModel.setPerson(benson, fieldCase.expected());

            CommandResult result = assertDoesNotThrow(() ->
                    new EditCommand(INDEX_SECOND_PERSON, fieldCase.descriptor()).execute(model), fieldCase.name());
            assertEquals(successMessage(fieldCase.expected()), result.getFeedbackToUser(), fieldCase.name());
            assertEquals(expectedModel, model, fieldCase.name());
            Person saved = model.getFilteredPersonList().get(INDEX_SECOND_PERSON.getZeroBased());
            assertEquals(benson.getProjects(), saved.getProjects(), fieldCase.name());
        }
    }

    @Test
    public void execute_telegramReplaced_storesNormalisedValue() throws Exception {
        EditPersonDescriptor descriptor = new EditPersonDescriptor();
        descriptor.setTelegramUsername(new TelegramUsername(" @New_Handle "));

        new EditCommand(INDEX_FIRST_PERSON, descriptor).execute(model);

        Person saved = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        assertEquals("new_handle", saved.getTelegramUsername().value);
    }

    @Test
    public void execute_editOtherField_preservesProjects() {
        Person personToEdit = model.getFilteredPersonList().get(INDEX_SECOND_PERSON.getZeroBased());
        Person editedPerson = new PersonBuilder(personToEdit).withPhone(VALID_PHONE_BOB).build();
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder().withPhone(VALID_PHONE_BOB).build();
        EditCommand editCommand = new EditCommand(INDEX_SECOND_PERSON, descriptor);

        Model expectedModel = copyOf(model);
        expectedModel.setPerson(personToEdit, editedPerson);

        assertCommandSuccess(editCommand, model, successMessage(editedPerson), expectedModel);
        assertEquals(personToEdit.getProjects(), editedPerson.getProjects());
    }

    @Test
    public void execute_legacyContactOtherFieldEdited_untouchedValuesKeptExactly() throws Exception {
        Index legacyIndex = addLegacyPerson();
        EditPersonDescriptor descriptor = new EditPersonDescriptor();
        descriptor.setName(new Name("Legacy Tan"));

        new EditCommand(legacyIndex, descriptor).execute(model);

        Person saved = model.getFilteredPersonList().get(legacyIndex.getZeroBased());
        assertStoredValues(saved, "Legacy Tan", "91234567", "Legacy.Lee@Example.COM", "");
        assertEquals(LEGACY.getProjects(), saved.getProjects());
    }

    @Test
    public void execute_fillLegacyEmptyTelegram_success() throws Exception {
        Index legacyIndex = addLegacyPerson();
        EditPersonDescriptor descriptor = new EditPersonDescriptor();
        descriptor.setTelegramUsername(new TelegramUsername("@Legacy_Lee"));
        Person editedPerson = new PersonBuilder(LEGACY).withTelegramUsername("legacy_lee").build();

        CommandResult result = new EditCommand(legacyIndex, descriptor).execute(model);

        assertEquals(successMessage(editedPerson), result.getFeedbackToUser());
        Person saved = model.getFilteredPersonList().get(legacyIndex.getZeroBased());
        assertStoredValues(saved, "Legacy Lee", "91234567", "Legacy.Lee@Example.COM", "legacy_lee");
        assertEquals(LEGACY.getProjects(), saved.getProjects());
    }

    @Test
    public void execute_legacyMixedCaseEmailLowercased_isRealChange() throws Exception {
        Index legacyIndex = addLegacyPerson();
        EditPersonDescriptor descriptor = new EditPersonDescriptor();
        descriptor.setEmail(new Email("legacy.lee@example.com"));
        // Email equality ignores case, so Person#equals alone cannot show this change.
        assertEquals(LEGACY.getEmail(), descriptor.getEmail().get());

        new EditCommand(legacyIndex, descriptor).execute(model);

        Person saved = model.getFilteredPersonList().get(legacyIndex.getZeroBased());
        assertStoredValues(saved, "Legacy Lee", "91234567", "legacy.lee@example.com", "");
    }

    @Test
    public void execute_nameCaseOnlyChange_success() {
        Person firstPerson = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        String lowerCaseName = firstPerson.getName().fullName.toLowerCase(Locale.ROOT);
        Person editedPerson = new PersonBuilder(firstPerson).withName(lowerCaseName).build();
        EditCommand editCommand = new EditCommand(INDEX_FIRST_PERSON,
                new EditPersonDescriptorBuilder().withName(lowerCaseName).build());

        Model expectedModel = copyOf(model);
        expectedModel.setPerson(firstPerson, editedPerson);

        assertCommandSuccess(editCommand, model, successMessage(editedPerson), expectedModel);
    }

    @Test
    public void execute_successMessage_usesEditedContactText() throws Exception {
        EditCommand editCommand = new EditCommand(INDEX_SECOND_PERSON,
                new EditPersonDescriptorBuilder().withName(VALID_NAME_BOB).build());

        assertEquals("Edited contact: Bob Choo; Phone: +6598765432; Email: johnd@example.com; "
                + "Telegram: @benson_meier; Projects: CS2103T, Orbital",
                editCommand.execute(model).getFeedbackToUser());
    }

    @Test
    public void execute_noFieldSpecifiedUnfilteredList_noChangesFailure() {
        EditCommand editCommand = new EditCommand(INDEX_FIRST_PERSON, new EditPersonDescriptor());

        assertCommandFailure(editCommand, model, EditCommand.MESSAGE_NO_CHANGES);
    }

    @Test
    public void execute_allValuesUnchangedFilteredList_noChangesFailureKeepsFilter() {
        showPersonAtIndex(model, INDEX_SECOND_PERSON);
        Person benson = model.getFilteredPersonList().get(0);
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder(benson).build();
        descriptor.setTelegramUsername(benson.getTelegramUsername());

        assertCommandFailure(new EditCommand(INDEX_FIRST_PERSON, descriptor), model,
                EditCommand.MESSAGE_NO_CHANGES);
        assertEquals(List.of(benson), model.getFilteredPersonList());
    }

    @Test
    public void execute_telegramSameAfterNormalisation_noChangesFailure() {
        EditPersonDescriptor descriptor = new EditPersonDescriptor();
        descriptor.setTelegramUsername(new TelegramUsername("@ALICE_Pauline"));

        assertCommandFailure(new EditCommand(INDEX_FIRST_PERSON, descriptor), model,
                EditCommand.MESSAGE_NO_CHANGES);
    }

    @Test
    public void execute_uniqueFieldSetToOwnValue_noChangesFailure() {
        Person firstPerson = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());

        for (UniqueFieldEdit edit : UNIQUE_FIELD_EDITS) {
            assertCommandFailure(new EditCommand(INDEX_FIRST_PERSON, edit.copyFrom(firstPerson)), model,
                    EditCommand.MESSAGE_NO_CHANGES);
        }
    }

    @Test
    public void execute_uniqueFieldSetToOwnValueWithOtherChange_success() {
        for (UniqueFieldEdit edit : UNIQUE_FIELD_EDITS) {
            model = new ModelManager(getTypicalAddressBook(), new UserPrefs());
            Person firstPerson = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
            EditPersonDescriptor descriptor = edit.copyFrom(firstPerson);
            descriptor.setName(new Name(VALID_NAME_BOB));
            Person editedPerson = new PersonBuilder(firstPerson).withName(VALID_NAME_BOB).build();

            Model expectedModel = copyOf(model);
            expectedModel.setPerson(firstPerson, editedPerson);

            assertCommandSuccess(new EditCommand(INDEX_FIRST_PERSON, descriptor), model,
                    successMessage(editedPerson), expectedModel);
        }
    }

    @Test
    public void execute_filteredList_successShowsAllContacts() {
        showPersonAtIndex(model, INDEX_FIRST_PERSON);

        Person personInFilteredList = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        Person editedPerson = new PersonBuilder(personInFilteredList).withName(VALID_NAME_BOB).build();
        EditCommand editCommand = new EditCommand(INDEX_FIRST_PERSON,
                new EditPersonDescriptorBuilder().withName(VALID_NAME_BOB).build());

        Model expectedModel = copyOf(model);
        expectedModel.setPerson(model.getFilteredPersonList().get(0), editedPerson);

        assertCommandSuccess(editCommand, model, successMessage(editedPerson), expectedModel);
        assertEquals(model.getAddressBook().getPersonList(), model.getFilteredPersonList());
    }

    @Test
    public void execute_allFieldsOfAnotherPerson_failure() {
        Person firstPerson = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder(firstPerson).build();
        descriptor.setTelegramUsername(firstPerson.getTelegramUsername());
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
    public void execute_uniqueFieldOfAnotherPersonWithValidChange_noPartialEdit() {
        Person secondPerson = model.getFilteredPersonList().get(INDEX_SECOND_PERSON.getZeroBased());
        for (UniqueFieldEdit edit : UNIQUE_FIELD_EDITS) {
            EditPersonDescriptor descriptor = edit.copyFrom(secondPerson);
            descriptor.setName(new Name(VALID_NAME_BOB));
            assertCommandFailure(new EditCommand(INDEX_FIRST_PERSON, descriptor), model,
                    edit.expectedMessage(secondPerson));
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
        assertEquals(1, model.getFilteredPersonList().size());
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
    public void execute_emailOfHiddenLegacyPersonInDifferentCase_failure() {
        addLegacyPerson();
        showPersonAtIndex(model, INDEX_FIRST_PERSON);
        EditCommand editCommand = new EditCommand(INDEX_FIRST_PERSON,
                new EditPersonDescriptorBuilder().withEmail("legacy.lee@example.com").build());

        assertCommandFailure(editCommand, model,
                new ContactConflict(Field.EMAIL, "legacy.lee@example.com", LEGACY).getMessage());
    }

    @Test
    public void execute_telegramOfAnotherPersonWithDifferentCase_failure() {
        Person secondPerson = model.getFilteredPersonList().get(INDEX_SECOND_PERSON.getZeroBased());
        EditPersonDescriptor descriptor = new EditPersonDescriptor();
        descriptor.setTelegramUsername(new TelegramUsername("@BENSON_Meier"));

        assertCommandFailure(new EditCommand(INDEX_FIRST_PERSON, descriptor), model,
                new ContactConflict(Field.TELEGRAM_USERNAME, "@benson_meier", secondPerson).getMessage());
    }

    @Test
    public void execute_nameOfAnotherPerson_success() {
        Person firstPerson = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        Person secondPerson = model.getFilteredPersonList().get(INDEX_SECOND_PERSON.getZeroBased());
        Person editedPerson = new PersonBuilder(secondPerson).withName(firstPerson.getName().fullName).build();
        EditCommand editCommand = new EditCommand(INDEX_SECOND_PERSON,
                new EditPersonDescriptorBuilder().withName(firstPerson.getName().fullName).build());

        Model expectedModel = copyOf(model);
        expectedModel.setPerson(secondPerson, editedPerson);

        assertCommandSuccess(editCommand, model, successMessage(editedPerson), expectedModel);
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
    public void execute_invalidIndexAndNoChanges_reportsInvalidIndexFirst() {
        Index outOfBoundIndex = Index.fromOneBased(model.getFilteredPersonList().size() + 1);

        assertCommandFailure(new EditCommand(outOfBoundIndex, new EditPersonDescriptor()), model,
                Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
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

        // different Telegram username only -> returns false
        EditPersonDescriptor withTelegram = new EditPersonDescriptor(DESC_AMY);
        withTelegram.setTelegramUsername(new TelegramUsername(VALID_TELEGRAM_USERNAME_BOB));
        assertFalse(standardCommand.equals(new EditCommand(INDEX_FIRST_PERSON, withTelegram)));
    }

    @Test
    public void constructor_descriptorChangedLater_commandUnaffected() {
        EditPersonDescriptor descriptor = new EditPersonDescriptor(DESC_AMY);
        EditCommand editCommand = new EditCommand(INDEX_FIRST_PERSON, descriptor);
        descriptor.setTelegramUsername(new TelegramUsername(VALID_TELEGRAM_USERNAME_BOB));

        assertEquals(new EditCommand(INDEX_FIRST_PERSON, DESC_AMY), editCommand);
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
