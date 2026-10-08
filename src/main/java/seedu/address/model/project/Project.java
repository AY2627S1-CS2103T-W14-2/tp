package seedu.address.model.project;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Represents a project associated with a person.
 * Guarantees: immutable; name is valid as declared in {@link #isValidProjectName(String)}.
 */
public class Project {

    public static final int MAX_NAME_LENGTH = 40;
    public static final String MESSAGE_CONSTRAINTS = "Project names should contain 1 to 40 printable characters";
    private static final Pattern WHITESPACE_PATTERN = Pattern.compile("[\\s\\p{Z}]+");

    public final String projectName;
    private final String canonicalProjectName;

    /**
     * Constructs a {@code Project}.
     * Surrounding whitespace is removed and repeated internal whitespace is collapsed.
     *
     * @param projectName A valid project name.
     */
    public Project(String projectName) {
        requireNonNull(projectName);
        String normalizedProjectName = normalize(projectName);
        checkArgument(isValidProjectName(normalizedProjectName), MESSAGE_CONSTRAINTS);
        this.projectName = normalizedProjectName;
        canonicalProjectName = normalizedProjectName.toLowerCase(Locale.ROOT);
    }

    /**
     * Returns the whitespace-normalized form of the given project name.
     */
    public static String normalize(String projectName) {
        requireNonNull(projectName);
        return WHITESPACE_PATTERN.matcher(projectName).replaceAll(" ").strip();
    }

    /**
     * Returns true if the given string is a valid project name.
     */
    public static boolean isValidProjectName(String test) {
        requireNonNull(test);
        String normalizedProjectName = normalize(test);
        int nameLength = normalizedProjectName.codePointCount(0, normalizedProjectName.length());
        return nameLength >= 1
                && nameLength <= MAX_NAME_LENGTH
                && normalizedProjectName.codePoints().noneMatch(Character::isISOControl);
    }

    @Override
    public String toString() {
        return projectName;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        if (!(other instanceof Project otherProject)) {
            return false;
        }

        return canonicalProjectName.equals(otherProject.canonicalProjectName);
    }

    @Override
    public int hashCode() {
        return canonicalProjectName.hashCode();
    }
}
