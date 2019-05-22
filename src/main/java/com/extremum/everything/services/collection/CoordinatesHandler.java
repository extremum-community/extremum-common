package com.extremum.everything.services.collection;

import com.extremum.common.collection.CollectionCoordinates;
import com.extremum.common.models.Model;
import com.extremum.everything.collection.Projection;

import java.util.List;

/**
 * @author rpuch
 */
public interface CoordinatesHandler {
    List<Model> fetchCollection(CollectionCoordinates coordinates, Projection projection);
}
