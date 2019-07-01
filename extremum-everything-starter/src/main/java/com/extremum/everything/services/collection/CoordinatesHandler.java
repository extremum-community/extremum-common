package com.extremum.everything.services.collection;

import com.extremum.common.collection.CollectionCoordinates;
import com.extremum.common.models.Model;
import com.extremum.everything.collection.CollectionFragment;
import com.extremum.everything.collection.Projection;

/**
 * @author rpuch
 */
public interface CoordinatesHandler {
    CollectionFragment<Model> fetchCollection(CollectionCoordinates coordinates, Projection projection);
}
