package com.extremum.everything.services;

import com.extremum.common.models.BasicModel;
import com.extremum.common.models.Model;
import com.extremum.everything.collection.CollectionFragment;
import com.extremum.everything.collection.Projection;

/**
 * A component that may be used to override the default 'collection fetching'
 * logic. If it is present in the application context and found by
 * getSupportedModel() + getHostFieldName(), it will be used to search for
 * the collection chunk instead of the default method.
 *
 * @author rpuch
 */
public interface CollectionFetcher<H extends BasicModel, E extends Model>
        extends EverythingEverythingService {
    /**
     * Returns the field name of the host object to which this collection
     * is mapped.
     *
     * @return host field name
     */
    String getHostFieldName();

    CollectionFragment<E> fetchCollection(H host, Projection projection);
}
