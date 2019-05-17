package com.extremum.common.service.impl;

import com.extremum.common.dao.CommonDao;
import com.extremum.common.exceptions.CommonException;
import com.extremum.common.exceptions.ModelNotFoundException;
import com.extremum.common.exceptions.WrongArgumentException;
import com.extremum.common.models.PersistableCommonModel;
import com.extremum.common.response.Alert;
import com.extremum.common.service.CommonService;
import com.extremum.common.utils.StreamUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;


abstract class CommonServiceImpl<ID extends Serializable, M extends PersistableCommonModel<ID>>
        implements CommonService<ID, M> {

    private final CommonDao<M, ID> dao;
    private final Class<M> modelClass;
    final String modelTypeName;

    private final static Logger LOGGER = LoggerFactory.getLogger(CommonServiceImpl.class);

    CommonServiceImpl(CommonDao<M, ID> dao) {
        this.dao = dao;
        modelClass = (Class<M>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
        modelTypeName = modelClass.getSimpleName();
    }

    protected abstract ID stringToId(String id);

    @Override
    public M get(String id) {
        return get(id, new ThrowOnAlert());
    }

    @Override
    public M get(String id, Collection<Alert> alerts) {
        checkThatAlertsIsNotNull(alerts);
        return get(id, new AddAlert(alerts));
    }

    private void checkThatAlertsIsNotNull(Collection<Alert> alerts) {
        Objects.requireNonNull(alerts, "Alerts collection must not be null");
    }

    private M get(String id, Problems problems) {
        LOGGER.debug("Get model {} with id {}", modelTypeName, id);

        if (!checkId(id, problems)) {
            return null;
        }
        M found = dao.findById(stringToId(id)).orElse(null);
        return getResultWithNullabilityCheck(found, id, problems);
    }

    @Override
    public List<M> list() {
        return list(new ThrowOnAlert());
    }

    @Override
    public List<M> list(Collection<Alert> alerts) {
        checkThatAlertsIsNotNull(alerts);
        return list(new AddAlert(alerts));
    }

    private List<M> list(Problems problems) {
        LOGGER.debug("Get list of models of type {}", modelTypeName);
        return dao.findAll();
    }

    @Override
    public M create(M data) {
        return create(data, new ThrowOnAlert());
    }

    @Override
    public M create(M data, Collection<Alert> alerts) {
        checkThatAlertsIsNotNull(alerts);
        return create(data, new AddAlert(alerts));
    }

    private M create(M data, Problems problems) {
        LOGGER.debug("Create model {}", data);

        if(data == null) {
            fillAlertsOrThrowException(problems, new WrongArgumentException("Model can't be null"));
            return null;
        }
        return dao.save(data);
    }

    @Override
    public List<M> create(List<M> data) {
        return create(data, new ThrowOnAlert());
    }

    @Override
    public List<M> create(List<M> data, Collection<Alert> alerts) {
        checkThatAlertsIsNotNull(alerts);
        return create(data, new AddAlert(alerts));
    }
    
    private List<M> create(List<M> data, Problems problems) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Create models {}", data != null ?
                    data.stream().map(Object::toString).collect(Collectors.joining(", ")) : "-none-");
        }
        if(data == null) {
            fillAlertsOrThrowException(problems, new WrongArgumentException("Models can't be null"));
            return null;
        }
        Iterable<M> savedModelsIterable = dao.saveAll(data);

        return StreamUtils.fromIterable(savedModelsIterable).collect(Collectors.toList());
    }

    @Override
    public M save(M data) {
        return save(data, new ThrowOnAlert());
    }

    @Override
    public M save(M data, Collection<Alert> alerts) {
        checkThatAlertsIsNotNull(alerts);
        return save(data, new AddAlert(alerts));
    }

    private M save(M data, Problems problems) {
        LOGGER.debug("Save model {}", modelTypeName);

        if (data == null) {
            fillAlertsOrThrowException(problems, new WrongArgumentException("Model can't be null"));
            return null;
        }
        M returned = null;

        if (data.getId() != null) {
            Optional<M> existedOpt = dao.findById(data.getId());
            if (existedOpt.isPresent()) {
                M existed = existedOpt.get();
                copyServiceFields(existed, data);
                if (data.getUuid() == null) {
                    data.setUuid(existed.getUuid());
                }
                returned = dao.save(data);
            }
        }
        if (returned == null) {
            // Если у модели deleted=true, то get ничего не вернет. Но реально документ в БД есть.
            // Он будет здесь перезаписан
            returned = dao.save(data);
        }
        return returned;
    }

    private void copyServiceFields(M from, M to) {
        to.setUuid(from.getUuid());
        to.setVersion(from.getVersion());
        to.setDeleted(from.getDeleted());
        to.setCreated(from.getCreated());
        to.setModified(from.getModified());
    }

    @Override
    public void delete(String id) {
        delete(id, new ThrowOnAlert());
    }

    @Override
    public void delete(String id, Collection<Alert> alerts) {
        checkThatAlertsIsNotNull(alerts);
        delete(id, new AddAlert(alerts));
    }
    
    private void delete(String id, Problems problems) {
        LOGGER.debug("Delete model {} with id {}", modelTypeName, id);

        if (!checkId(id, problems)) {
            return;
        }

        dao.deleteById(stringToId(id));
    }

    protected final boolean checkId(String id, Problems problems) {
        boolean valid = true;
        if (StringUtils.isBlank(id)) {
            fillAlertsOrThrowException(problems, new WrongArgumentException("Model id can't be null"));
            valid = false;
        }
        return valid;
    }

    protected final M getResultWithNullabilityCheck(M result, String id, Problems problems) {
        if(result == null) {
            LOGGER.warn("Model {} with id {} wasn't found", modelTypeName, id);
            fillAlertsOrThrowException(problems, new ModelNotFoundException(modelClass, id));
        }
        return result;
    }

    protected final void fillAlertsOrThrowException(Problems problems, CommonException ex) {
        problems.accept(ex);
    }
}
