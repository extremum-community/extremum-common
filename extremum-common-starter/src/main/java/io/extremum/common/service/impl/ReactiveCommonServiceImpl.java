package io.extremum.common.service.impl;

import io.extremum.common.dao.ReactiveCommonDao;
import io.extremum.common.exceptions.ModelNotFoundException;
import io.extremum.common.exceptions.WrongArgumentException;
import io.extremum.common.model.BasicModel;
import io.extremum.common.service.ReactiveCommonService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.util.List;
import java.util.stream.Collectors;


public abstract class ReactiveCommonServiceImpl<ID extends Serializable, M extends BasicModel<ID>>
        implements ReactiveCommonService<M> {

    private final ReactiveCommonDao<M, ID> dao;
    private final Class<M> modelClass;
    private final String modelTypeName;

    private final static Logger LOGGER = LoggerFactory.getLogger(ReactiveCommonServiceImpl.class);

    public ReactiveCommonServiceImpl(ReactiveCommonDao<M, ID> dao) {
        this.dao = dao;
        modelClass = (Class<M>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
        modelTypeName = modelClass.getSimpleName();
    }

    protected abstract ID stringToId(String id);

    @Override
    public Mono<M> get(String id) {
        LOGGER.debug("Get model {} with id {}", modelTypeName, id);

        if (StringUtils.isBlank(id)) {
            return Mono.error(new WrongArgumentException("Model id can't be null"));
        }

        return dao.findById(stringToId(id))
                .switchIfEmpty(Mono.error(new ModelNotFoundException(modelClass, id)));
    }

    @Override
    public Flux<M> list() {
        LOGGER.debug("Get list of models of type {}", modelTypeName);
        return dao.findAll();
    }

    @Override
    public Mono<M> create(M data) {
        LOGGER.debug("Create model {}", data);

        if (data == null) {
            return Mono.error(new WrongArgumentException("Model can't be null"));
        }

        return dao.save(data);
    }

    @Override
    public Flux<M> create(List<M> data) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Create models {}", data != null ?
                    data.stream().map(Object::toString).collect(Collectors.joining(", ")) : "-none-");
        }
        if (data == null) {
            return Flux.error(new WrongArgumentException("Models can't be null"));
        }
        return dao.saveAll(data);
    }

    @Override
    public Mono<M> save(M data) {
        LOGGER.debug("Save model {}", modelTypeName);

        if (data == null) {
            return Mono.error(new WrongArgumentException("Model can't be null"));
        }

        if (data.getId() != null) {
            return dao.findById(data.getId())
                    .flatMap(existing -> {
                        copyServiceFields(existing, data);
                        if (data.getUuid() == null) {
                            data.setUuid(existing.getUuid());
                        }
                        return dao.save(data);
                    })
                    .switchIfEmpty(dao.save(data));
        }

        return dao.save(data);
    }

    private void copyServiceFields(M from, M to) {
        from.copyServiceFieldsTo(to);
    }

    @Override
    public Mono<M> delete(String id) {
        LOGGER.debug("Delete model {} with id {}", modelTypeName, id);

        if (StringUtils.isBlank(id)) {
            return Mono.error(new WrongArgumentException("Model id can't be null"));
        }

        return dao.deleteByIdAndReturn(stringToId(id));
    }
}
