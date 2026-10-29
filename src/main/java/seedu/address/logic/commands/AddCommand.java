package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.parser.CliSyntax.PREFIX_EMAIL;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NAME;
import static seedu.address.logic.parser.CliSyntax.PREFIX_PHONE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_PROJECT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_TELEGRAM;

import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.person.Person;

/**
 * Adds a contact to LinkUp.
 */
public class AddCommand extends Command {

    public static final String COMMAND_WORD = "add";

    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Adds a contact to LinkUp.\n"
            + "Parameters: "
            + PREFIX_NAME + "NAME "
            + PREFIX_PHONE + "PHONE "
            + PREFIX_EMAIL + "EMAIL "
            + PREFIX_TELEGRAM + "TELEGRAM_USERNAME "
            + PREFIX_PROJECT + "PROJECT\n"
            + "Example: " + COMMAND_WORD + " "
            + PREFIX_NAME + "John Doe "
            + PREFIX_PHONE + "+6598765432 "
            + PREFIX_EMAIL + "johnd@example.com "
            + PREFIX_TELEGRAM + "@john_doe "
            + PREFIX_PROJECT + "CS2103T";

    public static final String MESSAGE_SUCCESS = "Added contact %1$d: %2$s%3$s";
    public static final String MESSAGE_PROJECT_REQUIRED = "Project is required. Use pr/PROJECT.";

    private final Person toAdd;

    /**
     * Creates an AddCommand to add the specified {@code Person}
     */
    public AddCommand(Person person) {
        requireNonNull(person);
        toAdd = person;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);

        rejectIfConflicting(model.findConflict(toAdd));
        model.addPerson(toAdd);
        // The index the user types in later commands: the contact's position in the list now displayed
        int displayedIndex = model.getFilteredPersonList().indexOf(toAdd) + 1;
        assert displayedIndex > 0 : "a newly added contact must be displayed, since add shows all contacts";
        String projectSuffix = toAdd.getProjects().isEmpty() ? "" : " [" + toAdd.getProjects().getFirst() + "]";
        return new CommandResult(String.format(MESSAGE_SUCCESS, displayedIndex, toAdd.getName(), projectSuffix));
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof AddCommand otherAddCommand)) {
            return false;
        }

        return toAdd.equals(otherAddCommand.toAdd);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("toAdd", toAdd)
                .toString();
    }

    /**
     * Returns true as this command changes the address book.
     */
    @Override
    public boolean isUndoable() {
        return true;
    }
}
