package com.extremum.common.repository.mongo;

import com.extremum.common.dao.MongoCommonDao;
import com.extremum.common.models.MongoCommonModel;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.repository.query.MongoEntityInformation;
import org.springframework.data.mongodb.repository.support.SimpleMongoRepository;

/**
 * @author rpuch
 */
abstract class BaseMongoRepository<T extends MongoCommonModel> extends SimpleMongoRepository<T, ObjectId>
        implements MongoCommonDao<T> {
    BaseMongoRepository(MongoEntityInformation<T, ObjectId> metadata,
            MongoOperations mongoOperations) {
        super(metadata, mongoOperations);
    }

    @Override
    public final void deleteAll() {
        throw new UnsupportedOperationException("We don't allow to delete all the documents in one go");
    }
}
