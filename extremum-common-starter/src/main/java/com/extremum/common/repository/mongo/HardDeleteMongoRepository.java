package com.extremum.common.repository.mongo;

import com.extremum.common.dao.MongoCommonDao;
import com.extremum.common.models.MongoCommonModel;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.repository.query.MongoEntityInformation;

/**
 * @author rpuch
 */
public class HardDeleteMongoRepository<T extends MongoCommonModel> extends BaseMongoRepository<T>
        implements MongoCommonDao<T> {

    public HardDeleteMongoRepository(MongoEntityInformation<T, ObjectId> metadata,
            MongoOperations mongoOperations) {
        super(metadata, mongoOperations);
    }

    @Override
    Query notDeletedQueryWith(Criteria criteria) {
        return new Query(criteria);
    }

    @Override
    Query notDeletedQuery() {
        return new Query();
    }

}
