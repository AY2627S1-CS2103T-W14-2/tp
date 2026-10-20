package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.parser.CliSyntax.PREFIX_EMAIL;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NAME;
import static seedu.address.logic.parser.CliSyntax.PREFIX_PHONE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_TELEGRAM;
import static seedu.address.model.Model.PREDICATE_SHOW_ALL_PERSONS;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import seedu.address.commons.core.index.Index;
import seedu.address.commons.util.AppUtil;
import seedu.address.commons.util.CollectionUtil;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.person.Email;
import seedu.address.model.person.Name;
import seedu.address.model.person.Person;
import seedu.address.model.person.Phone;
import seedu.address.model.person.TelegramUsername;

/**
 * Edits the details of an existing person in the address book.
 */
public class EditCommand extends Command {

    public static final String COMMAND_WORD = "edit";

    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Edits the details of the contact identified "
            + "by the index number used in the displayed contact list. "
            + "Existing values will be overwritten by the input values.\n"
            + "Parameters: INDEX (must be a positive integer) "
            + "[" + PREFIX_NAME + "NAME] "
            + "[" + PREFIX_PHONE + "PHONE] "
            + "[" + PREFIX_EMAIL + "EMAIL] "
            + "[" + PREFIX_TELEGRAM + "TELEGRAM]\n"
            + "Example: " + COMMAND_WORD + " 1 "
            + PREFIX_PHONE + "+6591234567 "
            + PREFIX_EMAIL + "johndoe@example.com "
            + PREFIX_TELEGRAM + "@john_doe";

    public static final String MESSAGE_EDIT_PERSON_SUCCESS = "Edited contact: %1$s";
    public static final String MESSAGE_NOT_EDITED = "At least one field to edit must be provided.";
    public static final String MESSAGE_NO_CHANGES = "No changes made.";
    public static final String MESSAGE_PROJECT_NOT_EDITABLE =
            "Projects cannot be edited with edit. Use project INDEX pr/PROJECT to add an association.";

    private final Index index;
    private final EditPersonDescriptor editPersonDescriptor;

    /**
     * @param index of the person in the filtered person list to edit
     * @param editPersonDescriptor details to edit the person with
     */
    public EditCommand(Index index, EditPersonDescriptor editPersonDescriptor) {
        requireNonNull(index);
        requireNonNull(editPersonDescriptor);

        this.index = index;
        this.editPersonDescriptor = new EditPersonDescriptor(editPersonDescriptor);
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        List<Person> lastShownList = model.getFilteredPersonList();

        if (index.getZeroBased() >= lastShownList.size()) {
            throw new CommandException(Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
        }

        Person personToEdit = lastShownList.get(index.getZeroBased());
        Person editedPerson = createEditedPerson(personToEdit, editPersonDescriptor);

        // Unchanged values cannot conflict, since the edited contact is excluded from the check.
        if (hasSameStoredValues(personToEdit, editedPerson)) {
            throw new CommandException(MESSAGE_NO_CHANGES);
        }
        rejectIfConflicting(model.findConflict(editedPerson, personToEdit));
        model.setPerson(personToEdit, editedPerson);
        model.updateFilteredPersonList(PREDICATE_SHOW_ALL_PERSONS);
        return new CommandResult(String.format(MESSAGE_EDIT_PERSON_SUCCESS, Messages.format(editedPerson)));
    }

    /**
     * Creates and returns a {@code Person} with the details of {@code personToEdit}
     * edited with {@code editPersonDescriptor}. Projects are always kept as they are.
     */
    private static Person createEditedPerson(Person personToEdit, EditPersonDescriptor editPersonDescriptor) {
        assert personToEdit != null;

        Name updatedName = editPersonDescriptor.getName().orElse(personToEdit.getName());
        Phone updatedPhone = editPersonDescriptor.getPhone().orElse(personToEdit.getPhone());
        Email updatedEmail = editPersonDescriptor.getEmail().orElse(personToEdit.getEmail());
        TelegramUsername updatedTelegramUsername =
                editPersonDescriptor.getTelegramUsername().orElse(personToEdit.getTelegramUsername());

        return new Person(updatedName, updatedPhone, updatedEmail, updatedTelegramUsername,
                personToEdit.getProjects());
    }

    /**
     * Returns true if every editable field of {@code editedPerson} would be saved exactly as in {@code original}.
     * Saved strings are compared because {@code Email#equals} ignores case, so a legacy mixed-case email that is
     * re-entered in lowercase is a real change.
     */
    private static boolean hasSameStoredValues(Person original, Person editedPerson) {
        return original.getName().fullName.equals(editedPerson.getName().fullName)
                && original.getPhone().value.equals(editedPerson.getPhone().value)
                && original.getEmail().value.equals(editedPerson.getEmail().value)
                && original.getTelegramUsername().value.equals(editedPerson.getTelegramUsername().value);
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof EditCommand otherEditCommand)) {
            return false;
        }

        return index.equals(otherEditCommand.index)
                && editPersonDescriptor.equals(otherEditCommand.editPersonDescriptor);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("index", index)
                .add("editPersonDescriptor", editPersonDescriptor)
                .toString();
    }

    /**
     * Stores the details to edit the person with. Each non-empty field value will replace the
     * corresponding field value of the person.
     */
    public static class EditPersonDescriptor {
        public static final String MESSAGE_TELEGRAM_NOT_CLEARABLE =
                "A Telegram username cannot be cleared by editing.";

        private Name name;
        private Phone phone;
        private Email email;
        private TelegramUsername telegramUsername;

        public EditPersonDescriptor() {}

        /**
         * Copy constructor.
         */
        public EditPersonDescriptor(EditPersonDescriptor toCopy) {
            setName(toCopy.name);
            setPhone(toCopy.phone);
            setEmail(toCopy.email);
            setTelegramUsername(toCopy.telegramUsername);
        }

        /**
         * Returns true if at least one field is edited.
         */
        public boolean isAnyFieldEdited() {
            return CollectionUtil.isAnyNonNull(name, phone, email, telegramUsername);
        }

        public void setName(Name name) {
            this.name = name;
        }

        public Optional<Name> getName() {
            return Optional.ofNullable(name);
        }

        public void setPhone(Phone phone) {
            this.phone = phone;
        }

        public Optional<Phone> getPhone() {
            return Optional.ofNullable(phone);
        }

        public void setEmail(Email email) {
            this.email = email;
        }

        public Optional<Email> getEmail() {
            return Optional.ofNullable(email);
        }

        /**
         * Sets the Telegram username to replace the existing one, or {@code null} to keep the existing one.
         *
         * @throws IllegalArgumentException if {@code telegramUsername} is empty, because a Telegram username
         *         cannot be cleared.
         */
        public void setTelegramUsername(TelegramUsername telegramUsername) {
            AppUtil.checkArgument(telegramUsername == null || !telegramUsername.isEmpty(),
                    MESSAGE_TELEGRAM_NOT_CLEARABLE);
            this.telegramUsername = telegramUsername;
        }

        public Optional<TelegramUsername> getTelegramUsername() {
            return Optional.ofNullable(telegramUsername);
        }

        @Override
        public boolean equals(Object other) {
            if (other == this) {
                return true;
            }

            // instanceof handles nulls
            if (!(other instanceof EditPersonDescriptor otherEditPersonDescriptor)) {
                return false;
            }

            return Objects.equals(name, otherEditPersonDescriptor.name)
                    && Objects.equals(phone, otherEditPersonDescriptor.phone)
                    && Objects.equals(email, otherEditPersonDescriptor.email)
                    && Objects.equals(telegramUsername, otherEditPersonDescriptor.telegramUsername);
        }

        @Override
        public String toString() {
            return new ToStringBuilder(this)
                    .add("name", name)
                    .add("phone", phone)
                    .add("email", email)
                    .add("telegramUsername", telegramUsername)
                    .toString();
        }
    }

    /**
     * Returns true as this command changes the address book.
     */
    @Override
    public boolean isUndoable() {
        return true;
    }
}
