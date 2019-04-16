package com.extremum.common.service;

import com.extremum.common.models.MongoCommonModel;
import com.extremum.common.response.Alert;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Common interface for mongo services
 */
public interface MongoCommonService<Model extends MongoCommonModel> {

    Model get(String id);

    Model get(String id, Collection<Alert> alerts);

    Model delete(String id);

    Model delete(String id, Collection<Alert> alerts);

    List<Model> list();

    List<Model> list(Collection<Alert> alerts);

    List<Model> listByParameters(Map<String, Object> parameters);

    List<Model> listByParameters(Map<String, Object> parameters, Collection<Alert> alerts);

    List<Model> listByFieldValue(String fieldName, Object fieldValue);

    List<Model> listByFieldValue(String fieldName, Object fieldValue, Collection<Alert> alerts);

    List<Model> listByFieldValue(String fieldName, Object fieldValue, int offset, int limit);

    List<Model> listByFieldValue(String fieldName, Object fieldValue, int offset, int limit, Collection<Alert> alerts);

    Model save(Model data);

    Model save(Model data, Collection<Alert> alerts);

    Model create(Model data);

    Model create(Model data, Collection<Alert> alerts);

    List<Model> create(List<Model> data);

    List<Model> create(List<Model> data, Collection<Alert> alerts);

    Model getSelectedFieldsById(String id, String... fieldNames);

    Model getSelectedFieldsById(String id, Collection<Alert> alerts, String... fieldNames);
}