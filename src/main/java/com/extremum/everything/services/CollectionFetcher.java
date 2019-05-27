package com.extremum.everything.services;

import com.extremum.common.models.BasicModel;
import com.extremum.common.models.Model;
import com.extremum.everything.collection.Projection;

import java.util.List;

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

    List<E> fetchCollection(H host, Projection projection);
}
