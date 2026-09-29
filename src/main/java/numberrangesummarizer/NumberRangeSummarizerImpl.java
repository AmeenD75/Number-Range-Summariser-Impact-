package numberrangesummarizer;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Default implementation of {@link NumberRangeSummarizer}.
 *
 * <p>Values are sorted and de-duplicated before they are summarized, so the
 * result depends only on the set of supplied numbers.</p>
 */
public final class NumberRangeSummarizerImpl implements NumberRangeSummarizer {

    @Override
    public Collection<Integer> collect(String input) {
        Objects.requireNonNull(input, "input must not be null");

        // A blank string contains no numbers, so it produces an empty collection.
        if (input.trim().isEmpty()) {
            return Collections.emptyList();
        }

        // Keep empty tokens when splitting so malformed input such as "1,2,"
        // is rejected by parseInteger instead of being accepted.
        return Arrays.stream(input.split(",", -1))
                .map(String::trim)
                .map(NumberRangeSummarizerImpl::parseInteger)
                .collect(Collectors.toList());
    }

    @Override
    public String summarizeCollection(Collection<Integer> input) {
        Objects.requireNonNull(input, "input collection must not be null");

        // Work on a new list so the caller's collection is never modified.
        // Sorting allows sequential values to be identified in one pass.
        List<Integer> sortedNumbers = input.stream()
                .map(number -> Objects.requireNonNull(
                        number, "input collection must not contain null values"))
                .distinct()
                .sorted()
                .collect(Collectors.toList());

        if (sortedNumbers.isEmpty()) {
            return "";
        }

        StringBuilder summary = new StringBuilder();
        int rangeStart = sortedNumbers.get(0);
        int previous = rangeStart;

        // Track the beginning and end of the current range. When a gap is
        // found, finish that range and begin another at the current number.
        for (int index = 1; index < sortedNumbers.size(); index++) {
            int current = sortedNumbers.get(index);

            // long arithmetic prevents Integer.MAX_VALUE + 1 from wrapping
            // around to Integer.MIN_VALUE.
            if ((long) previous + 1 != current) {
                appendRange(summary, rangeStart, previous);
                summary.append(", ");
                rangeStart = current;
            }

            previous = current;
        }

        appendRange(summary, rangeStart, previous);
        return summary.toString();
    }

    /**
     * Converts one trimmed input token into an integer.
     *
     * @param value token to parse
     * @return the parsed integer
     * @throws IllegalArgumentException when the token is empty or not a valid int
     */
    private static Integer parseInteger(String value) {
        if (value.isEmpty()) {
            throw new IllegalArgumentException("Input contains an empty value");
        }

        try {
            return Integer.valueOf(value);
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(
                    "Invalid integer value: '" + value + "'", exception);
        }
    }

    /**
     * Appends either one number or an inclusive range to the summary.
     * Examples: {@code 4} or {@code 4-7}.
     */
    private static void appendRange(StringBuilder summary, int start, int end) {
        summary.append(start);
        if (start != end) {
            summary.append('-').append(end);
        }
    }
}
