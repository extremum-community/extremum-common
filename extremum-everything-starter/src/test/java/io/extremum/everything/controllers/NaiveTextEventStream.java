package io.extremum.everything.controllers;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * Please note that this class implements a simplified (and, strictly speaking, incorrect)
 * but sufficient for our purposes parsing of a text/event-stream response.
 * 1. It ignores everything but data: blocks
 * 2. It treats each data: block as an independent message, although, if a message has more than
 * one line, this can be incorrect. In our case, each data block contains one single-line JSON,
 * so it is ok.
 */
class NaiveTextEventStream {
    private static final String DATA_PREFIX = "data:";

    private final String wholeContent;

    NaiveTextEventStream(String wholeContent) {
        Objects.requireNonNull(wholeContent, "wholeContent cannot be null");

        this.wholeContent = wholeContent;
    }

    Stream<String> dataStream() {
        return Arrays.stream(wholeContent.split("\n"))
                .filter(line -> line.startsWith(DATA_PREFIX))
                .map(line -> line.substring(DATA_PREFIX.length()));
    }
}
