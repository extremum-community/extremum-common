package com.extremum.common.service.impl;

import com.extremum.common.dao.CommonDao;
import com.extremum.common.exceptions.CommonException;
import com.extremum.common.exceptions.ModelNotFoundException;
import com.extremum.common.exceptions.WrongArgumentException;
import com.extremum.common.models.BasicModel;
import com.extremum.common.service.CommonService;
import com.extremum.common.service.Problems;
import com.extremum.common.service.ThrowOnAlert;
import com.extremum.common.utils.StreamUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;


public abstract class CommonServiceImpl<ID extends Serializable, M extends BasicModel<ID>>
        implements CommonService<M> {

    private final CommonDao<M, ID> dao;
    private final Class<M> modelClass;
    final String modelTypeName;

    private final static Logger LOGGER = LoggerFactory.getLogger(CommonServiceImpl.class);

    public CommonServiceImpl(CommonDao<M, ID> dao) {
        this.dao = dao;
        modelClass = (Class<M>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
        modelTypeName = modelClass.getSimpleName();
    }

    protected abstract ID stringToId(String id);

    private void checkThatProblemsIsNotNull(Problems problems) {
        Objects.requireNonNull(problems, "Problems must not be null");
    }

    @Override
    public M get(String id) {
        return get(id, new ThrowOnAlert());
    }

    @Override
    public M get(String id, Problems problems) {
        checkThatProblemsIsNotNull(problems);
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
    public List<M> list(Problems problems) {
        checkThatProblemsIsNotNull(problems);
        LOGGER.debug("Get list of models of type {}", modelTypeName);
        return dao.findAll();
    }

    @Override
    public M create(M data) {
        return create(data, new ThrowOnAlert());
    }

    @Override
    public M create(M data, Problems problems) {
        checkThatProblemsIsNotNull(problems);
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
    public List<M> create(List<M> data, Problems problems) {
        checkThatProblemsIsNotNull(problems);
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
    public M save(M data, Problems problems) {
        checkThatProblemsIsNotNull(problems);
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
        from.copyServiceFieldsTo(to);
    }

    @Override
    public void delete(String id) {
        delete(id, new ThrowOnAlert());
    }

    @Override
    public void delete(String id, Problems problems) {
        checkThatProblemsIsNotNull(problems);
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
