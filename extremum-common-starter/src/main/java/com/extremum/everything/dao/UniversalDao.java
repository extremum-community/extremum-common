package com.extremum.everything.dao;

import com.extremum.everything.collection.Projection;

import java.util.List;

/**
 * @author rpuch
 */
public interface UniversalDao {
    <T> List<T> retrieveByIds(List<?> ids, Class<T> classOfElement, Projection projection);
}
