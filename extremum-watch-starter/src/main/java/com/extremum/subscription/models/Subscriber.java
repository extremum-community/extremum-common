package com.extremum.subscription.models;

/**
 * Interface to implement for all type of subscribers.
 */
//TODO add implementation with use of SecurityContext
public interface Subscriber {
    /**
     * Method that used to get identifier of subscriber.
     * Identifier used on Redis.
     */
    String getId();
}
