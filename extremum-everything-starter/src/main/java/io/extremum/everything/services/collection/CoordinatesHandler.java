package io.extremum.everything.services.collection;

import io.extremum.common.collection.CollectionCoordinates;
import io.extremum.common.models.Model;
import io.extremum.everything.collection.CollectionFragment;
import io.extremum.everything.collection.Projection;
import reactor.core.publisher.Flux;

/**
 * @author rpuch
 */
public interface CoordinatesHandler {
    CollectionFragment<Model> fetchCollection(CollectionCoordinates coordinates, Projection projection);

    Flux<Model> streamCollection(CollectionCoordinates coordinates, Projection projection);
}
