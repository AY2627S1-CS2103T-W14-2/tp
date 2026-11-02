package seedu.address.model.project;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

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
    // One or more consecutive zero-width non-joiners or joiners, handled as a whole so that cleaning is idempotent.
    // The run is matched only from its first joiner and without backtracking, so long runs take linear time.
    private static final String JOINER_RUN = "[\\u200C\\u200D]++";
    private static final String RUN_START = "(?<![\\u200C\\u200D])";
    // Characters that never need a joiner: Latin letters, digits, punctuation and spaces.
    private static final String PLAIN_CHARACTER = "[\\p{IsLatin}\\p{Nd}\\p{P}\\s\\p{Z}]";
    // A joiner run next to a plain character, or at either end, joins nothing visible, so it is removed. Joiners
    // between emoji or between letters of scripts such as Persian or Hindi are kept. \A and \z are used instead of
    // ^ and $, which would also match before a final line terminator such as U+0085.
    private static final Pattern STRAY_JOINER_PATTERN = Pattern.compile(RUN_START + "(?:(?<=" + PLAIN_CHARACTER
            + ")" + JOINER_RUN + "|" + JOINER_RUN + "(?=" + PLAIN_CHARACTER + ")|\\A" + JOINER_RUN + "|" + JOINER_RUN
            + "\\z)");
    // The single spaces left at either end after collapsing whitespace. Used instead of String#strip, which also
    // removes the separators U+001C..U+001F: those are control characters here and must stay so they are rejected.
    private static final Pattern EDGE_SPACE_PATTERN = Pattern.compile("\\A | \\z");

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
        checkArgument(isValidProjectName(projectName), MESSAGE_CONSTRAINTS);
        String normalizedProjectName = normalize(projectName);
        this.projectName = normalizedProjectName;
        // Compared letter by letter, as String#equalsIgnoreCase does. Lowercasing the whole name would turn the
        // sigma in "ΟΣ" into a final sigma, which differs from the sigma in "οσ".
        canonicalProjectName = normalizedProjectName.codePoints()
                .map(codePoint -> Character.toLowerCase(Character.toUpperCase(codePoint)))
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
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
        String collapsed = WHITESPACE_PATTERN.matcher(visibleProjectName).replaceAll(" ");
        return EDGE_SPACE_PATTERN.matcher(collapsed).replaceAll("");
    }

    /**
     * Returns true if the given string is a valid project name.
     */
    public static boolean isValidProjectName(String test) {
        requireNonNull(test);
        // Malformed text: removing a character between two unpaired surrogates would join them into a new one.
        if (test.codePoints().anyMatch(codePoint -> Character.getType(codePoint) == Character.SURROGATE)) {
            return false;
        }
        // Control characters such as tabs and line breaks are rejected before whitespace is collapsed.
        if (test.codePoints().anyMatch(Character::isISOControl)) {
            return false;
        }
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
