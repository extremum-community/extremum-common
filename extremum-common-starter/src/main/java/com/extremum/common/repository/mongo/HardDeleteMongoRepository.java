package com.extremum.common.repository.mongo;

import com.extremum.common.dao.MongoCommonDao;
import com.extremum.common.exceptions.ModelNotFoundException;
import com.extremum.common.models.MongoCommonModel;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.repository.query.MongoEntityInformation;

import java.util.Optional;

/**
 * @author rpuch
 */
public class HardDeleteMongoRepository<T extends MongoCommonModel> extends BaseMongoRepository<T>
        implements MongoCommonDao<T> {
    private final MongoEntityInformation<T, ObjectId> metadata;

    public HardDeleteMongoRepository(MongoEntityInformation<T, ObjectId> metadata,
            MongoOperations mongoOperations) {
        super(metadata, mongoOperations);

        this.metadata = metadata;
    }

    @Override
    Query notDeletedQueryWith(Criteria criteria) {
        return new Query(criteria);
    }

    @Override
    Query notDeletedQuery() {
        return new Query();
    }

    @Override
    public T deleteByIdAndReturn(ObjectId id) {
        T model = findById(id).orElseThrow(() -> new ModelNotFoundException(metadata.getJavaType(), id.toString()));

        deleteById(id);

        return model;
    }
}
