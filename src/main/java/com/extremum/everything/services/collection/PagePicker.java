package com.extremum.everything.services.collection;

import com.extremum.common.models.BasicModel;
import com.extremum.common.models.Model;
import com.extremum.everything.collection.Projection;

import java.util.Collection;
import java.util.List;

/**
 * @author rpuch
 */
interface PagePicker {
    List<Model> getModelsFromModelsCollection(Collection<?> nonEmptyCollection, Projection projection,
            Model host, String hostFieldName);
}
