package seedu.address.logic.commands;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.person.Person;

/** Shared selection and update mechanics for batch project commands. */
final class ProjectContactSelection {
    private ProjectContactSelection() {
    }

    /** Returns an immutable, non-empty list of distinct indices in the supplied order. */
    static List<Index> validateIndices(List<Index> indices, String duplicateIndexMessage) {
        List<Index> copy = List.copyOf(indices);
        if (copy.isEmpty()) {
            throw new IllegalArgumentException("At least one contact index is required.");
        }
        for (int i = 0; i < copy.size(); i++) {
            if (copy.subList(0, i).contains(copy.get(i))) {
                throw new IllegalArgumentException(String.format(duplicateIndexMessage, copy.get(i).getOneBased()));
            }
        }
        return copy;
    }

    /** Resolves all indices against one displayed-list snapshot before any contact is changed. */
    static List<Person> selectContacts(Model model, List<Index> indices) throws CommandException {
        List<Person> displayed = new ArrayList<>(model.getFilteredPersonList());
        List<Person> selected = new ArrayList<>();
        for (Index index : indices) {
            if (index.getZeroBased() >= displayed.size()) {
                throw new CommandException(Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
            }
            selected.add(displayed.get(index.getZeroBased()));
        }
        return selected;
    }

    /** Formats matching contacts in selection order using their original displayed indices. */
    static List<String> describeContacts(List<Index> indices, List<Person> contacts, Predicate<Person> condition) {
        List<String> descriptions = new ArrayList<>();
        for (int i = 0; i < contacts.size(); i++) {
            Person contact = contacts.get(i);
            if (condition.test(contact)) {
                descriptions.add(indices.get(i).getOneBased() + ": " + contact.getName());
            }
        }
        return descriptions;
    }

    /** Builds every replacement before applying updates; callers must first validate their project rules. */
    static List<Person> updateContacts(Model model, List<Person> originals, UnaryOperator<Person> update) {
        List<Person> replacements = originals.stream().map(update).toList();
        for (int i = 0; i < originals.size(); i++) {
            model.setPerson(originals.get(i), replacements.get(i));
        }
        return replacements;
    }
}
