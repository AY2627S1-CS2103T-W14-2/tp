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
    public static final String MESSAGE_CONSTRAINTS =
            "Project names should contain 1 to 40 characters and no line breaks or other control characters";
    private static final Pattern WHITESPACE_PATTERN = Pattern.compile("[\\s\\p{Z}]+");
    // Invisible format characters (e.g. zero-width space, byte order mark, direction marks), except the zero-width
    // non-joiner and joiner, which some scripts and emoji sequences such as a man technologist need.
    private static final Pattern INVISIBLE_FORMAT_PATTERN = Pattern.compile("[\\p{Cf}&&[^\\u200C\\u200D]]");
    private static final String JOINER = "[\\u200C\\u200D]";
    // Characters that never need a joiner: Latin letters, digits, punctuation and spaces.
    private static final String PLAIN_CHARACTER = "[\\p{IsLatin}\\p{Nd}\\p{P}\\s\\p{Z}]";
    // A joiner next to a plain character, or at either end, joins nothing visible, so it is removed. Joiners
    // between emoji or between letters of scripts such as Persian or Hindi are kept.
    private static final Pattern STRAY_JOINER_PATTERN = Pattern.compile("(?<=" + PLAIN_CHARACTER + ")" + JOINER
            + "|" + JOINER + "(?=" + PLAIN_CHARACTER + ")|^" + JOINER + "|" + JOINER + "$");

    public final String projectName;
    private final String canonicalProjectName;

    /**
     * Constructs a {@code Project}.
     * Invisible format characters (and zero-width joiners that join nothing visible) are removed, surrounding
     * whitespace is removed and repeated internal whitespace is collapsed.
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
     * Returns the normalized form of the given project name: invisible format characters that would make two
     * names look identical but compare differently are removed (zero-width joiners only where they join nothing
     * visible), then whitespace is collapsed and trimmed.
     */
    public static String normalize(String projectName) {
        requireNonNull(projectName);
        String withoutFormatCharacters = INVISIBLE_FORMAT_PATTERN.matcher(projectName).replaceAll("");
        String visibleProjectName = STRAY_JOINER_PATTERN.matcher(withoutFormatCharacters).replaceAll("");
        return WHITESPACE_PATTERN.matcher(visibleProjectName).replaceAll(" ").strip();
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
