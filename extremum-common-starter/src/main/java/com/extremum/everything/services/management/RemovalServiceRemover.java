package com.extremum.everything.services.management;

import com.extremum.everything.services.RemovalService;
import com.extremum.everything.services.Remover;

/**
 * @author rpuch
 */
class RemovalServiceRemover implements Remover {
    private final RemovalService removalService;

    RemovalServiceRemover(RemovalService removalService) {
        this.removalService = removalService;
    }

    @Override
    public void remove(String id) {
        removalService.remove(id);
    }
}
