package com.extremum.watch.processor;

import com.extremum.watch.models.TextWatchEvent;

/**
 * @author rpuch
 */
public interface WatchEventConsumer {
    void consume(TextWatchEvent event);
}
