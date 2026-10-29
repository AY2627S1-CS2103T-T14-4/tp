package seedu.address.logic.parser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import seedu.address.logic.parser.exceptions.ParseException;

/**
 * Tokenizes arguments string of the form: {@code preamble <prefix>value <prefix>value ...}<br>
 *     e.g. {@code some preamble text t/ 11.00 t/12.00 k/ m/ July}  where prefixes are {@code t/ k/ m/}.<br>
 * 1. An argument's value can be an empty string e.g. the value of {@code k/} in the above example.<br>
 * 2. Leading and trailing whitespaces of an argument value will be discarded.<br>
 * 3. An argument may be repeated and all its values will be accumulated e.g. the value of {@code t/}
 *    in the above example.<br>
 */
public class ArgumentTokenizer {

    /**
     * Tokenizes an arguments string and returns an {@code ArgumentMultimap} object that maps prefixes to their
     * respective argument values. Only the given prefixes will be recognized in the arguments string.
     *
     * @param argsString Arguments string of the form: {@code preamble <prefix>value <prefix>value ...}
     * @param prefixes   Prefixes to tokenize the arguments string with
     * @return           ArgumentMultimap object that maps prefixes to their arguments
     */
    public static ArgumentMultimap tokenize(String argsString, Prefix... prefixes) {
        List<PrefixPosition> positions = findAllPrefixPositions(argsString, prefixes);
        return extractArguments(argsString, positions);
    }

    /**
     * Tokenizes arguments while preserving prefix-like text in a trailing free-text value.
     * If {@code textPrefix} occurs exactly once and every other prefix appears before it, the remainder
     * is its value. Otherwise, tokenizes normally to support other field orders and duplicate detection.
     *
     * @param argsString Arguments to tokenize
     * @param textPrefix Prefix identifying the free-text field
     * @param otherPrefixes Required prefixes that must precede the free-text field to preserve its value
     * @return The prefixes mapped to their argument values
     */
    public static ArgumentMultimap tokenizeWithTrailingText(String argsString, Prefix textPrefix,
            Prefix... otherPrefixes) {
        int textPosition = findPrefixPosition(argsString, textPrefix.getPrefix(), 0);
        boolean hasSingleTextPrefix = textPosition != -1
                && findPrefixPosition(argsString, textPrefix.getPrefix(), textPosition) == -1;
        if (hasSingleTextPrefix) {
            ArgumentMultimap arguments = tokenize(argsString.substring(0, textPosition), otherPrefixes);
            if (Arrays.stream(otherPrefixes).allMatch(prefix -> arguments.getValue(prefix).isPresent())) {
                arguments.put(textPrefix, argsString.substring(textPosition + textPrefix.getPrefix().length()).trim());
                return arguments;
            }
        }
        Prefix[] prefixes = Stream.concat(Arrays.stream(otherPrefixes), Stream.of(textPrefix)).toArray(Prefix[]::new);
        return tokenize(argsString, prefixes);
    }

    /**
     * Ensures that slash-containing values for the supplied prefixes are enclosed in matching quotes.
     */
    public static void validateSlashQuoting(String argsString, Prefix... prefixes) throws ParseException {
        List<PrefixPosition> positions = findAllPrefixPositions(argsString, prefixes);
        positions.sort((prefix1, prefix2) -> prefix1.getStartPosition() - prefix2.getStartPosition());
        for (int i = 0; i < positions.size(); i++) {
            PrefixPosition current = positions.get(i);
            int nextPosition = i + 1 < positions.size() ? positions.get(i + 1).getStartPosition()
                    : argsString.length();
            String value = argsString.substring(current.getStartPosition() + current.getPrefix().getPrefix().length(),
                    nextPosition).trim();
            boolean quoted = value.length() >= 2 && value.startsWith("\"") && value.endsWith("\"");
            if (value.contains("/") && !quoted) {
                throw new ParseException("Argument values containing '/' must be enclosed in quotes.");
            }
        }
    }

    /**
     * Finds all zero-based prefix positions in the given arguments string.
     *
     * @param argsString Arguments string of the form: {@code preamble <prefix>value <prefix>value ...}
     * @param prefixes   Prefixes to find in the arguments string
     * @return           List of zero-based prefix positions in the given arguments string
     */
    private static List<PrefixPosition> findAllPrefixPositions(String argsString, Prefix... prefixes) {
        return Arrays.stream(prefixes)
                .flatMap(prefix -> findPrefixPositions(argsString, prefix).stream())
                .collect(Collectors.toList());
    }

    /**
     * @see #findAllPrefixPositions(String, Prefix...)
     */
    private static List<PrefixPosition> findPrefixPositions(String argsString, Prefix prefix) {
        List<PrefixPosition> positions = new ArrayList<>();

        int prefixPosition = findPrefixPosition(argsString, prefix.getPrefix(), 0);
        while (prefixPosition != -1) {
            PrefixPosition extendedPrefix = new PrefixPosition(prefix, prefixPosition);
            positions.add(extendedPrefix);
            prefixPosition = findPrefixPosition(argsString, prefix.getPrefix(), prefixPosition);
        }

        return positions;
    }

    /**
     * Returns the index of the first occurrence of {@code prefix} in
     * {@code argsString} starting from index {@code fromIndex}. An occurrence
     * is valid if there is a whitespace before {@code prefix}. Returns -1 if no
     * such occurrence can be found.
     *
     * E.g if {@code argsString} = "e/hip/900", {@code prefix} = "p/" and
     * {@code fromIndex} = 0, this method returns -1 as there are no valid
     * occurrences of "p/" with whitespace before it. However, if
     * {@code argsString} = "e/hi p/900", {@code prefix} = "p/" and
     * {@code fromIndex} = 0, this method returns 5.
     */
    private static int findPrefixPosition(String argsString, String prefix, int fromIndex) {
        int prefixIndex = argsString.indexOf(" " + prefix, fromIndex);
        while (prefixIndex != -1 && isInsideQuotes(argsString, prefixIndex + 1)) {
            prefixIndex = argsString.indexOf(" " + prefix, prefixIndex + 1);
        }
        return prefixIndex == -1 ? -1
                : prefixIndex + 1; // +1 as offset for whitespace
    }

    /**
     * Returns true if the given position is enclosed by an unmatched quote.
     */
    private static boolean isInsideQuotes(String argsString, int position) {
        boolean insideQuotes = false;
        for (int i = 0; i < position; i++) {
            if (argsString.charAt(i) == '"') {
                insideQuotes = !insideQuotes;
            }
        }
        return insideQuotes;
    }

    /**
     * Extracts prefixes and their argument values, and returns an {@code ArgumentMultimap} object that maps the
     * extracted prefixes to their respective arguments. Prefixes are extracted based on their zero-based positions in
     * {@code argsString}.
     *
     * @param argsString      Arguments string of the form: {@code preamble <prefix>value <prefix>value ...}
     * @param prefixPositions Zero-based positions of all prefixes in {@code argsString}
     * @return                ArgumentMultimap object that maps prefixes to their arguments
     */
    private static ArgumentMultimap extractArguments(String argsString, List<PrefixPosition> prefixPositions) {

        // Sort by start position
        prefixPositions.sort((prefix1, prefix2) -> prefix1.getStartPosition() - prefix2.getStartPosition());

        // Insert a PrefixPosition to represent the preamble
        PrefixPosition preambleMarker = new PrefixPosition(new Prefix(""), 0);
        prefixPositions.addFirst(preambleMarker);

        // Add a dummy PrefixPosition to represent the end of the string
        PrefixPosition endPositionMarker = new PrefixPosition(new Prefix(""), argsString.length());
        prefixPositions.add(endPositionMarker);

        // Map prefixes to their argument values (if any)
        ArgumentMultimap argMultimap = new ArgumentMultimap();
        for (int i = 0; i < prefixPositions.size() - 1; i++) {
            // Extract and store prefixes and their arguments
            Prefix argPrefix = prefixPositions.get(i).getPrefix();
            String argValue = extractArgumentValue(argsString, prefixPositions.get(i), prefixPositions.get(i + 1));
            argMultimap.put(argPrefix, argValue);
        }

        return argMultimap;
    }

    /**
     * Returns the trimmed value of the argument in the arguments string specified by {@code currentPrefixPosition}.
     * The end position of the value is determined by {@code nextPrefixPosition}.
     */
    private static String extractArgumentValue(String argsString,
                                        PrefixPosition currentPrefixPosition,
                                        PrefixPosition nextPrefixPosition) {
        Prefix prefix = currentPrefixPosition.getPrefix();

        int valueStartPos = currentPrefixPosition.getStartPosition() + prefix.getPrefix().length();
        String value = argsString.substring(valueStartPos, nextPrefixPosition.getStartPosition());

        String trimmedValue = value.trim();
        if (trimmedValue.length() >= 2
                && trimmedValue.startsWith("\"")
                && trimmedValue.endsWith("\"")) {
            return trimmedValue.substring(1, trimmedValue.length() - 1);
        }
        return trimmedValue;
    }

    /**
     * Represents a prefix's position in an arguments string.
     */
    private static class PrefixPosition {
        private int startPosition;
        private final Prefix prefix;

        PrefixPosition(Prefix prefix, int startPosition) {
            this.prefix = prefix;
            this.startPosition = startPosition;
        }

        int getStartPosition() {
            return startPosition;
        }

        Prefix getPrefix() {
            return prefix;
        }
    }

}
