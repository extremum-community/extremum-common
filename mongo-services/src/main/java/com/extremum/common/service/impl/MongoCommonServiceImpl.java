package com.extremum.common.service.impl;

import com.extremum.common.dao.MongoCommonDao;
import com.extremum.common.descriptor.factory.impl.MongoDescriptorFactory;
import com.extremum.common.models.MongoCommonModel;
import com.extremum.common.response.Alert;
import com.extremum.common.service.MongoCommonService;
import com.extremum.common.exception.ModelNotFoundException;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.util.CollectionUtils;

import javax.annotation.Nullable;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class MongoCommonServiceImpl<Model extends MongoCommonModel> implements MongoCommonService<Model> {
    
    protected final MongoCommonDao<Model> dao;
    private final String modelTypeName;

    private final static Logger LOGGER = LoggerFactory.getLogger(MongoCommonServiceImpl.class);

    public MongoCommonServiceImpl(MongoCommonDao<Model> dao) {
        this.dao = dao;
        modelTypeName = dao.getEntityClass().getSimpleName();
    }

    @Override
    public Model get(String id){
        return get(id, null);
    }

    @Nullable
    @Override
    public Model get(String id, Collection<Alert> alerts){
        LOGGER.debug("Get mode {} with id {}", modelTypeName, id);

        Objects.requireNonNull(id, "id can't be null");

        Model found = dao.get(new ObjectId(id));

        if (found == null) {
            String descriptorId = getDescriptorId(id);

            LOGGER.warn("Model {} with id {} wasn't found", modelTypeName, descriptorId);

            if (alerts == null) {
                throw new ModelNotFoundException(dao.getEntityClass(), descriptorId);
            } else {
                alerts.add(Alert.errorAlert( "Model " + modelTypeName
                        + " with id " + descriptorId + " wasn't found", null, HttpStatus.NOT_FOUND.toString()));
            }
        }

        return found;
    }

    @Override
    public Model delete(String id){
        return delete(id, null);
    }

    @Override
    public Model delete(String id, Collection<Alert> alerts){
        LOGGER.debug("Delete model {} with id {}", modelTypeName, id);

        Objects.requireNonNull(id, "id can't be null");

        return dao.delete(new ObjectId(id));
    }

    @Override
    public List<Model> list(){
        return list(null);
    }

    @Override
    public List<Model> list(Collection<Alert> alerts){
        LOGGER.debug("Get list of models of type {}", modelTypeName);

        List<Model> returned = dao.listAll();

        if (returned == null) {
            returned = Collections.emptyList();
        }

        return returned;
    }

    @Override
    public List<Model> listByParameters(Map<String, Object> parameters){
        return listByParameters(parameters, null);
    }

    @Override
    public List<Model> listByParameters(Map<String, Object> parameters, Collection<Alert> alerts){
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Getting list of models of type {} by parameters {}", modelTypeName,
                    parameters.entrySet().stream().map(entry -> entry.getKey() + ": " + entry.getValue()));
        }

        if (CollectionUtils.isEmpty(parameters)) {
            return list(alerts);
        }

        for (Map.Entry<String, Object> entry : parameters.entrySet()) {
            String key = entry.getKey();
            Object valueObj = entry.getValue();

            if (key.equals("limit") || key.equals("offset")) {
                entry.setValue(String.valueOf(valueObj));
            }
        }

        return dao.listByParameters(parameters);
    }

    @Override
    public List<Model> listByFieldValue(String filed, Object value){
        return listByFieldValue(filed, value, null);
    }

    @Override
    public List<Model> listByFieldValue(String filed, Object value, Collection<Alert> alerts){
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Get list of models of type {} by field {} with value {}",
                    modelTypeName, filed, value);
        }

        Objects.requireNonNull(filed);
        Objects.requireNonNull(value);

        List<Model> result;
        result = dao.getByFieldValue(filed, value);

        if (result == null) {
            return Collections.emptyList();
        }

        return result;
    }

    @Override
    public List<Model> listByFieldValue(String filed, Object value, int offset, int limit){
        return listByFieldValue(filed, value, offset, limit, null);
    }

    @Override
    public List<Model> listByFieldValue(String filed, Object value, int offset, int limit, Collection<Alert> alerts)
           {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Get list of models of type {} by field {} with value {} using offset {}" +
                            "and limit {}", modelTypeName, filed, value, offset, limit);
        }

        Map<String, Object> params = new HashMap<>();
        params.put(filed, value);
        params.put("limit", limit);
        params.put("offset", offset);

        return listByParameters(params, alerts);
    }

    @Override
    public Model save(Model data){
        return save(data, null);
    }

    @Nullable
    @Override
    public Model save(Model data, Collection<Alert> alerts){
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Save model {}", modelTypeName);
        }

        Objects.requireNonNull(data);

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
    public Model create(Model data){
        return create(data, null);
    }

    @Nullable
    @Override
    public Model create(Model data, Collection<Alert> alerts){
        LOGGER.debug("Create model {}", data);
        Objects.requireNonNull(data);

        data.setCreated(ZonedDateTime.now());
        data.setVersion(0L);
        data.setDeleted(false);

        return dao.create(data);
    }

    @Override
    public List<Model> create(List<Model> data){
        return create(data, null);
    }

    @Override
    public List<Model> create(List<Model> data, Collection<Alert> alerts){
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Create models {}", data.stream().map(Object::toString).collect(Collectors.joining(", ")));
        }

        Objects.requireNonNull(data);

        return dao.create(data);
    }

    @Override
    public Model getSelectedFieldsById(String id, String[] fieldNames) {
        return getSelectedFieldsById(id, null, fieldNames);
    }

    @Override
    public Model getSelectedFieldsById(String id, Collection<Alert> alerts, String... fieldNames) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Get fields {} by id {} of model {}",
                    Stream.of(fieldNames).map(Object::toString).collect(Collectors.joining(", ")), id, modelTypeName);
        }

        Objects.requireNonNull(id, "id can't be null");
        return dao.getSelectedFieldsById(new ObjectId(id), fieldNames);
    }

    protected String getDescriptorId(String id) {
        return MongoDescriptorFactory.fromInternalId(id).getExternalId();
    }
}
