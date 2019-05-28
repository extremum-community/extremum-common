package com.extremum.everything.dao;

import com.extremum.everything.collection.CollectionFragment;
import com.extremum.everything.collection.Projection;

import java.util.List;

/**
 * @author rpuch
 */
public interface UniversalDao {
    <T> CollectionFragment<T> retrieveByIds(List<?> ids, Class<T> classOfElement, Projection projection);
}
