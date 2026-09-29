package numberrangesummarizer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests the public behavior of {@link NumberRangeSummarizerImpl}, including
 * parsing valid input, rejecting invalid input, normalizing collections,
 * formatting individual numbers and ranges, boundary values, null handling,
 * and preserving the caller's collection.
 *
 * <p>The tests also document the assumptions that blank input is allowed,
 * duplicate values are removed during summarization, unordered collections are
 * sorted, and two sequential values are formatted as a range.</p>
 */
class NumberRangeSummarizerImplTest {

    private NumberRangeSummarizer summarizer;

    @BeforeEach
    void setUp() {
        summarizer = new NumberRangeSummarizerImpl();
    }

    // collect(...) — successful parsing behavior

    @Test
    void collectParsesCommaDelimitedIntegersAndAllowsWhitespace() {
        Collection<Integer> result = summarizer.collect(" 1, 3, -5, +8 ");

        assertIterableEquals(Arrays.asList(1, 3, -5, 8), result);
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "   ", "\t"})
    void collectTreatsEmptyOrBlankInputAsAnEmptyCollection(String input) {
        assertEquals(Collections.emptyList(), summarizer.collect(input));
    }

    @Test
    void collectPreservesOrderAndDuplicateValues() {
        assertIterableEquals(Arrays.asList(3, 1, 3, 2),
                summarizer.collect("3,1,3,2"));
    }

    @Test
    void collectSupportsTheFullIntegerRange() {
        assertIterableEquals(Arrays.asList(Integer.MIN_VALUE, Integer.MAX_VALUE),
                summarizer.collect("-2147483648,2147483647"));
    }

    // collect(...) — malformed and null input handling

    @ParameterizedTest
    @ValueSource(strings = {"1,,3", ",1,2", "1,2,", "1, ,3"})
    void collectRejectsEmptyValuesAnywhereInTheInput(String input) {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> summarizer.collect(input));

        assertEquals("Input contains an empty value", exception.getMessage());
    }

    @ParameterizedTest
    @ValueSource(strings = {"1,two,3", "1,2.5,3", "1,1 2,3", "2147483648"})
    void collectRejectsInvalidOrOutOfRangeIntegers(String input) {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> summarizer.collect(input));

        assertTrue(exception.getMessage().startsWith("Invalid integer value:"));
        assertTrue(exception.getCause() instanceof NumberFormatException);
    }

    @Test
    void collectRejectsNullInput() {
        assertThrows(NullPointerException.class,
                () -> summarizer.collect(null));
    }

    // summarizeCollection(...) — expected range formatting

    @Test
    void summarizeCollectionProducesTheGivenExample() {
        Collection<Integer> input = summarizer.collect(
                "1,3,6,7,8,12,13,14,15,21,22,23,24,31");

        assertEquals("1, 3, 6-8, 12-15, 21-24, 31",
                summarizer.summarizeCollection(input));
    }

    @Test
    void summarizeCollectionSortsAndRemovesDuplicateValues() {
        assertEquals("1-3, 7",
                summarizer.summarizeCollection(Arrays.asList(3, 1, 2, 2, 7, 1)));
    }

    @Test
    void summarizeCollectionFormatsASingleValueWithoutARange() {
        assertEquals("42", summarizer.summarizeCollection(Collections.singletonList(42)));
    }

    @Test
    void summarizeCollectionUsesARangeForTwoSequentialValues() {
        assertEquals("4-5", summarizer.summarizeCollection(Arrays.asList(4, 5)));
    }

    @Test
    void summarizeCollectionLeavesNonSequentialValuesSeparate() {
        assertEquals("1, 3, 5, 7",
                summarizer.summarizeCollection(Arrays.asList(7, 1, 5, 3)));
    }

    @Test
    void summarizeCollectionHandlesRangesAtTheBeginningMiddleAndEnd() {
        assertEquals("1-3, 5, 7-9, 12-13",
                summarizer.summarizeCollection(Arrays.asList(
                        1, 2, 3, 5, 7, 8, 9, 12, 13)));
    }

    // summarizeCollection(...) — negative values and integer boundaries

    @Test
    void summarizeCollectionSupportsNegativeNumbersAndZero() {
        assertEquals("-3-0, 2",
                summarizer.summarizeCollection(Arrays.asList(-3, -2, -1, 0, 2)));
    }

    @Test
    void summarizeCollectionSupportsIntegerBoundariesWithoutOverflow() {
        assertEquals("-2147483648--2147483647, 2147483646-2147483647",
                summarizer.summarizeCollection(Arrays.asList(
                        Integer.MAX_VALUE,
                        Integer.MIN_VALUE + 1,
                        Integer.MAX_VALUE - 1,
                        Integer.MIN_VALUE)));
    }

    // summarizeCollection(...) — empty input, immutability, and null handling

    @Test
    void summarizeCollectionReturnsAnEmptyStringForNoValues() {
        assertEquals("", summarizer.summarizeCollection(Collections.emptyList()));
    }

    @Test
    void summarizeCollectionDoesNotModifyTheInputCollection() {
        List<Integer> input = new ArrayList<>(Arrays.asList(3, 2, 2, 1));
        List<Integer> original = new ArrayList<>(input);

        assertEquals("1-3", summarizer.summarizeCollection(input));
        assertEquals(original, input);
    }

    @Test
    void summarizeCollectionRejectsNullCollection() {
        NullPointerException exception = assertThrows(NullPointerException.class,
                () -> summarizer.summarizeCollection(null));

        assertEquals("input collection must not be null", exception.getMessage());
    }

    @Test
    void summarizeCollectionRejectsNullElements() {
        NullPointerException exception = assertThrows(NullPointerException.class,
                () -> summarizer.summarizeCollection(Arrays.asList(1, null, 3)));

        assertEquals("input collection must not contain null values",
                exception.getMessage());
    }
}
