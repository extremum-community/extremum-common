package com.extremum.common.service.impl;

import com.extremum.common.dao.MorphiaMongoCommonDao;
import com.extremum.common.exceptions.CommonException;
import com.extremum.common.exceptions.ModelNotFoundException;
import com.extremum.common.exceptions.WrongArgumentException;
import com.extremum.common.models.MongoCommonModel;
import com.extremum.common.response.Alert;
import com.extremum.common.service.MongoCommonService;
import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class MongoCommonServiceImpl<Model extends MongoCommonModel> implements MongoCommonService<Model> {
    
    protected final MorphiaMongoCommonDao<Model> dao;
    private final String modelTypeName;

    private final static Logger LOGGER = LoggerFactory.getLogger(MongoCommonServiceImpl.class);

    public MongoCommonServiceImpl(MorphiaMongoCommonDao<Model> dao) {
        this.dao = dao;
        modelTypeName = dao.getEntityClass().getSimpleName();
    }

    @Override
    public Model get(String id){
        return get(id, null);
    }

    @Override
    public Model get(String id, Collection<Alert> alerts){
        LOGGER.debug("Get mode {} with id {}", modelTypeName, id);

        if(!checkId(id, alerts)) {
            return null;
        }
        Model found = dao.get(new ObjectId(id));
        return getResultWithNullabilityCheck(found, id, alerts);
    }

    @Override
    public List<Model> list(){
        return list(null);
    }

    @Override
    public List<Model> list(Collection<Alert> alerts){
        LOGGER.debug("Get list of models of type {}", modelTypeName);
        return dao.findAll();
    }

    @Override
    public List<Model> listByParameters(Map<String, Object> parameters){
        return listByParameters(parameters, null);
    }

    @Override
    public List<Model> listByParameters(Map<String, Object> parameters, Collection<Alert> alerts) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Getting list of models of type {} by parameters {}", modelTypeName,
                    parameters != null ? parameters.entrySet().stream()
                            .map(entry -> entry.getKey() + ": " + entry.getValue())
                            .collect(Collectors.joining(", ")) : "-none-");
        }
        return dao.listByParameters(parameters);
    }

    @Override
    public List<Model> listByFieldValue(String fieldName, Object fieldValue){
        return listByFieldValue(fieldName, fieldValue, null);
    }

    @Override
    public List<Model> listByFieldValue(String fieldName, Object fieldValue, Collection<Alert> alerts){
        LOGGER.debug("Get list of models of type {} by field {} with value {}", modelTypeName, fieldName, fieldValue);

        if(!checkFieldNameAndValue(fieldName, fieldValue, alerts)) {
            return Collections.emptyList();
        }
        return dao.listByFieldValue(fieldName, fieldValue);
    }

    @Override
    public List<Model> listByFieldValue(String fieldName, Object fieldValue, int offset, int limit){
        return listByFieldValue(fieldName, fieldValue, offset, limit, null);
    }

    @Override
    public List<Model> listByFieldValue(String fieldName, Object fieldValue, int offset, int limit, Collection<Alert> alerts) {
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
    public Model getSelectedFieldsById(String id, String[] fieldNames) {
        return getSelectedFieldsById(id, null, fieldNames);
    }

    @Override
    public Model getSelectedFieldsById(String id, Collection<Alert> alerts, String... fieldNames) {
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
        Model found = dao.getSelectedFieldsById(new ObjectId(id), fieldNames);
        return getResultWithNullabilityCheck(found, id, alerts);
    }

    @Override
    public Model create(Model data){
        return create(data, null);
    }

    @Override
    public Model create(Model data, Collection<Alert> alerts){
        LOGGER.debug("Create model {}", data);

        if(data == null) {
            fillAlertsOrThrowException(alerts, new WrongArgumentException("Model can't be null"));
            return null;
        }
        return dao.create(data);
    }

    @Override
    public List<Model> create(List<Model> data){
        return create(data, null);
    }

    @Override
    public List<Model> create(List<Model> data, Collection<Alert> alerts){
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Create models {}", data != null ?
                    data.stream().map(Object::toString).collect(Collectors.joining(", ")) : "-none-");
        }
        if(data == null) {
            fillAlertsOrThrowException(alerts, new WrongArgumentException("Models can't be null"));
            return null;
        }
        return dao.create(data);
    }

    @Override
    public Model save(Model data){
        return save(data, null);
    }

    @Override
    public Model save(Model data, Collection<Alert> alerts){
        LOGGER.debug("Save model {}", modelTypeName);

        if(data == null) {
            fillAlertsOrThrowException(alerts, new WrongArgumentException("Model can't be null"));
            return null;
        }
        Model returned = null;

        if (data.getId() != null) {
            Model existed = dao.get(data.getId());
            if (existed != null) {
                copyServiceFields(existed, data);
                if (data.getUuid() == null) {
                        data.setUuid(existed.getUuid());
                    }
                returned = dao.merge(data);
            }
        }
        if (returned == null) {
            // Если у модели deleted=true, то get ничего не вернет. Но реально документ в БД есть.
            // Он будет здесь перезаписан
            returned = dao.create(data);
        }
        return returned;
    }

    private void copyServiceFields(Model from, Model to) {
        to.setUuid(from.getUuid());
        to.setVersion(from.getVersion());
        to.setDeleted(from.getDeleted());
        to.setCreated(from.getCreated());
        to.setModified(from.getModified());
    }

    @Override
    public Model delete(String id){
        return delete(id, null);
    }

    @Override
    public Model delete(String id, Collection<Alert> alerts){
        LOGGER.debug("Delete model {} with id {}", modelTypeName, id);

        if(!checkId(id, alerts)) {
            return null;
        }
        if (dao.remove(new ObjectId(id))) {
            Model found = dao.findById(new ObjectId(id));
            return getResultWithNullabilityCheck(found, id, alerts);
        } else {
            throw new ModelNotFoundException(dao.getEntityClass(), id);
        }
    }

    private boolean checkId(String id, Collection<Alert> alerts) {
        boolean valid = true;
        if(StringUtils.isBlank(id)) {
            fillAlertsOrThrowException(alerts, new WrongArgumentException("Model id can't be null"));
            valid = false;
        }
        return valid;
    }

    private Model getResultWithNullabilityCheck(Model result, String id, Collection<Alert> alerts) {
        if(result == null) {
            LOGGER.warn("Model {} with id {} wasn't found", modelTypeName, id);
            fillAlertsOrThrowException(alerts, new ModelNotFoundException(dao.getEntityClass(), id));
        }
        return result;
    }

    private void fillAlertsOrThrowException(Collection<Alert> alerts, CommonException ex) {
        if (alerts == null) {
            throw ex;
        } else {
            alerts.add(ex.getAlerts().get(0));
        }
    }
}
