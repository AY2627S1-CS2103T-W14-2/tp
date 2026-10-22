package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import seedu.address.commons.core.LogsCenter;
import seedu.address.model.Model;
import seedu.address.model.person.Person;
import seedu.address.model.project.Project;

/**
 * Lists every distinct project across all saved contacts, with the number of contacts in each.
 * The displayed contact list and its filter are left unchanged.
 */
public class ListProjectsCommand extends Command {

    public static final String COMMAND_WORD = "projects";

    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Lists every project saved across all contacts, with the number of contacts in each.\n"
            + "This command takes no parameters.\n"
            + "Example: " + COMMAND_WORD;
    public static final String MESSAGE_SUCCESS_HEADER = "Listed %1$d %2$s across all contacts:";
    public static final String MESSAGE_PROJECT_LINE = "- %1$s (%2$d %3$s)";
    public static final String MESSAGE_NO_PROJECTS = "No projects saved yet.";

    private static final Logger logger = LogsCenter.getLogger(ListProjectsCommand.class);

    @Override
    public CommandResult execute(Model model) {
        requireNonNull(model);
        // Project equality ignores case, so case variants share one key; the first spelling seen is kept.
        Map<Project, Integer> contactCounts = new LinkedHashMap<>();
        for (Person person : model.getAddressBook().getPersonList()) {
            for (Project project : person.getProjects()) {
                contactCounts.merge(project, 1, Integer::sum);
            }
        }

        if (contactCounts.isEmpty()) {
            logger.fine("Listed projects: none saved");
            return new CommandResult(MESSAGE_NO_PROJECTS);
        }

        String projectLines = contactCounts.entrySet().stream()
                .sorted(Comparator.comparing(entry -> entry.getKey().projectName.toLowerCase(Locale.ROOT)))
                .map(entry -> formatProjectLine(entry.getKey(), entry.getValue()))
                .collect(Collectors.joining("\n"));
        int projectCount = contactCounts.size();
        logger.fine("Listed " + projectCount + " distinct project(s)");
        return new CommandResult(String.format(Locale.ROOT, MESSAGE_SUCCESS_HEADER, projectCount,
                pluralise(projectCount, "project", "projects")) + "\n" + projectLines);
    }

    private static String formatProjectLine(Project project, int contactCount) {
        assert contactCount >= 1 : "A listed project must belong to at least one contact";
        return String.format(Locale.ROOT, MESSAGE_PROJECT_LINE, project.projectName, contactCount,
                pluralise(contactCount, "contact", "contacts"));
    }

    private static String pluralise(int count, String singular, String plural) {
        return count == 1 ? singular : plural;
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof ListProjectsCommand;
    }

    @Override
    public int hashCode() {
        return ListProjectsCommand.class.hashCode();
    }
}
