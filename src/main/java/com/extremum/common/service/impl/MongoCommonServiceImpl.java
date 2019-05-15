package com.extremum.common.service.impl;

import com.extremum.common.dao.MongoCommonDao;
import com.extremum.common.exceptions.WrongArgumentException;
import com.extremum.common.models.MongoCommonModel;
import com.extremum.common.response.Alert;
import com.extremum.common.service.MongoCommonService;
import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class MongoCommonServiceImpl<M extends MongoCommonModel> extends CommonServiceImpl<ObjectId, M>
        implements MongoCommonService<M> {
    
    private final MongoCommonDao<M> dao;

    private final static Logger LOGGER = LoggerFactory.getLogger(MongoCommonServiceImpl.class);

    public MongoCommonServiceImpl(MongoCommonDao<M> dao) {
        super(dao);
        this.dao = dao;
    }

    @Override
    protected ObjectId stringToId(String id) {
        return new ObjectId(id);
    }

    @Override
    public List<M> listByParameters(Map<String, Object> parameters){
        return listByParameters(parameters, null);
    }

    @Override
    public List<M> listByParameters(Map<String, Object> parameters, Collection<Alert> alerts) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Getting list of models of type {} by parameters {}", modelTypeName,
                    parameters != null ? parameters.entrySet().stream()
                            .map(entry -> entry.getKey() + ": " + entry.getValue())
                            .collect(Collectors.joining(", ")) : "-none-");
        }
        return dao.listByParameters(parameters);
    }

    @Override
    public List<M> listByFieldValue(String fieldName, Object fieldValue){
        return listByFieldValue(fieldName, fieldValue, null);
    }

    @Override
    public List<M> listByFieldValue(String fieldName, Object fieldValue, Collection<Alert> alerts){
        LOGGER.debug("Get list of models of type {} by field {} with value {}", modelTypeName, fieldName, fieldValue);

        if(!checkFieldNameAndValue(fieldName, fieldValue, alerts)) {
            return Collections.emptyList();
        }
        return dao.listByFieldValue(fieldName, fieldValue);
    }

    @Override
    public List<M> listByFieldValue(String fieldName, Object fieldValue, int offset, int limit){
        return listByFieldValue(fieldName, fieldValue, offset, limit, null);
    }

    @Override
    public List<M> listByFieldValue(String fieldName, Object fieldValue, int offset, int limit, Collection<Alert> alerts) {
        LOGGER.debug("Get list of models of type {} by field {} with value {} using offset {} and limit {}",
                modelTypeName, fieldName, fieldValue, offset, limit);

        if(!checkFieldNameAndValue(fieldName, fieldValue, alerts)) {
            return Collections.emptyList();
        }
        Map<String, Object> params = new HashMap<>();
        params.put(fieldName, fieldValue);
        params.put("limit", limit);
        params.put("offset", offset);

        return listByParameters(params, alerts);
    }

    private boolean checkFieldNameAndValue(String fieldName, Object fieldValue, Collection<Alert> alerts) {
        boolean valid = true;
        if(StringUtils.isBlank(fieldName)) {
            fillAlertsOrThrowException(alerts, new WrongArgumentException("Field name can't be blank"));
            valid = false;
        }
        if(fieldValue == null) {
            fillAlertsOrThrowException(alerts, new WrongArgumentException("Field value can't be null"));
            valid = false;
        }
        return valid;
    }

    @Override
    public M getSelectedFieldsById(String id, String... fieldNames) {
        return getSelectedFieldsById(id, null, fieldNames);
    }

    @Override
    public M getSelectedFieldsById(String id, Collection<Alert> alerts, String... fieldNames) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Get fields {} by id {} of model {}", Stream.of(fieldNames).map(Object::toString)
                            .collect(Collectors.joining(", ")), id, modelTypeName);
        }
        boolean valid = checkId(id, alerts);
        if (fieldNames == null || fieldNames.length == 0) {
            fillAlertsOrThrowException(alerts, new WrongArgumentException("Field names can't be null"));
            valid = false;
        }
        if(!valid) {
            return null;
        }
        M found = dao.getSelectedFieldsById(new ObjectId(id), fieldNames).orElse(null);
        return getResultWithNullabilityCheck(found, id, alerts);
    }
}
