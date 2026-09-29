# Number Range Summarizer

A Java 8 implementation of the supplied `NumberRangeSummarizer` interface.

Given:

```text
1,3,6,7,8,12,13,14,15,21,22,23,24,31
```

the implementation produces:

```text
1, 3, 6-8, 12-15, 21-24, 31
```

## Assumptions

The original requirements leave a few edge cases unspecified. This solution
makes the following choices, all documented by unit tests:

- Input values may contain surrounding whitespace.
- Blank string input represents an empty collection.
- Invalid values, empty values, null strings, null collections, and null
  collection elements are rejected rather than silently ignored.
- Values are sorted and duplicates are removed before summarization.
- Two sequential numbers are represented as a range (for example, `4-5`).
- Negative values and the full Java `int` range are supported.

## Design

`NumberRangeSummarizerImpl` implements the supplied interface. Java 8 streams
handle parsing and normalization. A single pass over the sorted values groups
them into ranges, and a small formatting method appends either a lone number or
a range to the result. The adjacency check uses `long` arithmetic to avoid
integer overflow.

## Build and test

The project requires Java 8 or newer and Maven 3.6 or newer.

```bash
mvn clean test
```
