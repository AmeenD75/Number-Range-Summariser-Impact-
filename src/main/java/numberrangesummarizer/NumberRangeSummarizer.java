package numberrangesummarizer;

import java.util.Collection;

/**
 * Produces a comma-delimited list of numbers, grouping sequential numbers into
 * ranges.
 */
public interface NumberRangeSummarizer {

    /**
     * Parses a comma-delimited string of integers.
     *
     * @param input comma-delimited integers
     * @return the parsed integers
     */
    Collection<Integer> collect(String input);

    /**
     * Summarizes a collection of integers as individual values and ranges.
     *
     * @param input integers to summarize
     * @return the summarized representation
     */
    String summarizeCollection(Collection<Integer> input);
}
