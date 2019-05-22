package com.extremum.everything.services;

public interface RemovalService extends EverythingEverythingService {
    /**
     * Removes an object by ID. If object with passed ID doesn't exists false will be returned.
     * Otherwise return true
     *
     * @param id of removable object
     */
    void remove(String id);
}
