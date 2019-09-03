package io.extremum.everything.services.collection;

import io.extremum.common.model.Model;
import io.extremum.everything.collection.CollectionFragment;
import io.extremum.everything.collection.Projection;
import io.extremum.sharedmodels.descriptor.CollectionCoordinates;
import reactor.core.publisher.Flux;

/**
 * @author rpuch
 */
public interface CoordinatesHandler {
    CollectionFragment<Model> fetchCollection(CollectionCoordinates coordinates, Projection projection);

    Flux<Model> streamCollection(CollectionCoordinates coordinates, Projection projection);
}
