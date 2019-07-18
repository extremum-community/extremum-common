package com.extremum.everything.security;

/**
 * @author rpuch
 */
public interface CheckerContext {
    String getCurrentUserName();

    boolean currentUserHasOneOf(String ... roles);
}
