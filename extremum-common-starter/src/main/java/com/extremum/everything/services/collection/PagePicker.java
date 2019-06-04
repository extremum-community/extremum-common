package com.extremum.everything.services.collection;

import com.extremum.common.models.Model;
import com.extremum.everything.collection.CollectionFragment;
import com.extremum.everything.collection.Projection;

import java.util.Collection;

/**
 * @author rpuch
 */
interface PagePicker {
    CollectionFragment<Model> getModelsFromModelsCollection(Collection<?> nonEmptyCollection, Projection projection,
            Model host, String hostPropertyName);
}
