package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NEW_PROJECT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_OLD_PROJECT;

import java.util.HashMap;
import java.util.Map;

import seedu.address.logic.commands.RenameProjectCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/**
 * Parses two project names, with optional double quotes and quote/backslash escapes.
 * Quoting is local to this command and does not change the syntax of existing commands.
 */
public class RenameProjectCommandParser implements Parser<RenameProjectCommand> {
    @Override
    public RenameProjectCommand parse(String args) throws ParseException {
        requireNonNull(args);
        Map<String, String> values = new HashMap<>();
        int cursor = skipWhitespace(args, 0);
        while (cursor < args.length()) {
            String prefix = prefixAt(args, cursor);
            if (prefix == null || values.containsKey(prefix)) {
                throw invalidFormat();
            }
            cursor = skipWhitespace(args, cursor + prefix.length());
            ParsedValue value = parseValue(args, cursor);
            values.put(prefix, value.value);
            cursor = skipWhitespace(args, value.end);
        }
        String oldName = values.get(PREFIX_OLD_PROJECT.getPrefix());
        String newName = values.get(PREFIX_NEW_PROJECT.getPrefix());
        if (oldName == null || newName == null) {
            throw invalidFormat();
        }
        return new RenameProjectCommand(ParserUtil.parseProject(oldName), ParserUtil.parseProject(newName));
    }

    private ParsedValue parseValue(String args, int start) throws ParseException {
        if (start < args.length() && args.charAt(start) == '"') {
            return parseQuotedValue(args, start);
        }
        int end = start;
        while (end < args.length()) {
            if ((end == start || isWhitespace(args.charAt(end - 1))) && prefixAt(args, end) != null) {
                break;
            }
            end++;
        }
        return new ParsedValue(args.substring(start, end), end);
    }

    private ParsedValue parseQuotedValue(String args, int start) throws ParseException {
        StringBuilder value = new StringBuilder();
        int cursor = start + 1;
        while (cursor < args.length()) {
            char current = args.charAt(cursor++);
            if (current == '"') {
                if (cursor < args.length() && !isWhitespace(args.charAt(cursor))) {
                    throw invalidFormat();
                }
                return new ParsedValue(value.toString(), cursor);
            }
            if (current == '\\') {
                if (cursor == args.length()) {
                    throw invalidFormat();
                }
                current = args.charAt(cursor++);
                if (current != '"' && current != '\\') {
                    throw invalidFormat();
                }
            }
            value.append(current);
        }
        throw invalidFormat();
    }

    private String prefixAt(String args, int cursor) {
        for (String prefix : new String[] {PREFIX_OLD_PROJECT.getPrefix(), PREFIX_NEW_PROJECT.getPrefix()}) {
            if (args.startsWith(prefix, cursor)) {
                return prefix;
            }
        }
        return null;
    }

    private int skipWhitespace(String args, int cursor) {
        while (cursor < args.length() && isWhitespace(args.charAt(cursor))) {
            cursor++;
        }
        return cursor;
    }

    private boolean isWhitespace(char character) {
        return Character.isWhitespace(character) || Character.isSpaceChar(character);
    }

    private ParseException invalidFormat() {
        return new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, RenameProjectCommand.MESSAGE_USAGE));
    }

    /**
     * A decoded argument and the next unconsumed input position.
     */
    private static class ParsedValue {
        private final String value;
        private final int end;

        private ParsedValue(String value, int end) {
            this.value = value;
            this.end = end;
        }
    }
}
