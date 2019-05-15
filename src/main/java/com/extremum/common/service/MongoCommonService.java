package com.extremum.common.service;

import com.extremum.common.models.MongoCommonModel;
import com.extremum.common.response.Alert;
import org.bson.types.ObjectId;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Common interface for mongo services
 */
public interface MongoCommonService<M extends MongoCommonModel> extends CommonService<ObjectId, M> {

    List<M> listByParameters(Map<String, Object> parameters);

    List<M> listByParameters(Map<String, Object> parameters, Collection<Alert> alerts);

    List<M> listByFieldValue(String fieldName, Object fieldValue);

    List<M> listByFieldValue(String fieldName, Object fieldValue, Collection<Alert> alerts);

    List<M> listByFieldValue(String fieldName, Object fieldValue, int offset, int limit);

    List<M> listByFieldValue(String fieldName, Object fieldValue, int offset, int limit, Collection<Alert> alerts);

    M getSelectedFieldsById(String id, String... fieldNames);

    M getSelectedFieldsById(String id, Collection<Alert> alerts, String... fieldNames);
}
